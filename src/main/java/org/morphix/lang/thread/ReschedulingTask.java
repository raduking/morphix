/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.lang.thread;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.morphix.lang.Comparables;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.LoggerAdapter;
import org.morphix.lang.resource.ScopedResource;
import org.morphix.lang.retry.Retry;
import org.morphix.reflection.Constructors;

/**
 * A reusable component that allows a task to reschedule itself based on a dynamic delay. The task will execute a
 * provided {@link Runnable} and then reschedule itself using a delay supplied by a {@link Supplier}.
 * <p>
 * The task can be enabled or disabled, and it will manage its own scheduling and cancellation to ensure that only one
 * instance of the task is scheduled at any time. The scheduling is done after the current task executions finishes, so
 * if the task takes longer than the delay, it will not cause overlapping executions.
 * <p>
 * Useful for token refreshers, cache refreshers, heartbeat mechanisms, etc.
 * <ul>
 * <li>TODO: add tests for the exact logging messages</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class ReschedulingTask implements AutoCloseable {

	/**
	 * Name space class for default values.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Default {

		/**
		 * Default minimum delay between executions - 500 milliseconds (safeguard against negative/zero delays).
		 */
		public static final Duration MIN_DELAY = Duration.ofMillis(500);

		/**
		 * Default behavior for task cancellation - do not interrupt running tasks.
		 */
		public static final boolean INTERRUPT_ON_CANCEL = false;

		/**
		 * Hide constructor.
		 */
		private Default() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * Name of the task, used for logging and identification.
	 */
	private final String name;

	/**
	 * Logger for logging task events and errors.
	 */
	private final LoggerAdapter logger;

	/**
	 * Scheduler for executing the task. The task will manage its own scheduling and cancellation.
	 */
	private final ScopedResource<ScheduledExecutorService> scheduler;

	/**
	 * The task to be executed and rescheduled.
	 */
	private final Runnable refreshTask;

	/**
	 * Supplier that provides the delay until the next execution after each run. The delay is recalculated after each
	 * execution.
	 */
	private final Supplier<Duration> nextDelaySupplier;

	/**
	 * Minimum allowed delay between executions. This is a safeguard against negative or zero delays that could cause rapid
	 * rescheduling.
	 */
	private final Duration minDelay;

	/**
	 * Retry strategy for task cancellation. Used when trying to cancel the currently scheduled task before scheduling a new
	 * one.
	 */
	private final Retry taskCancelRetry;

	/**
	 * Whether to interrupt the task thread when canceling. This is used in the cancellation logic when trying to cancel the
	 * currently scheduled task before scheduling a new one. Optional, defaults to false (do not interrupt).
	 */
	private final boolean interruptOnCancel;

	/**
	 * Atomic flag to indicate whether the task is enabled or disabled. When disabled, the task will not execute or
	 * reschedule itself.
	 */
	private final AtomicBoolean enabled = new AtomicBoolean(false);

	/**
	 * Atomic reference to the currently scheduled task. This allows for safe cancellation and rescheduling across threads.
	 */
	private final AtomicReference<ScheduledFuture<?>> scheduledTask = new AtomicReference<>();

	/**
	 * Constructor with builder.
	 *
	 * @param builder the builder
	 */
	private ReschedulingTask(final Builder builder) {
		this.name = Objects.requireNonNull(builder.name, "name must not be null");
		this.scheduler = Objects.requireNonNull(builder.scheduler, "scheduler must not be null");
		this.refreshTask = Objects.requireNonNull(builder.refreshTask, "refreshTask must not be null");
		this.nextDelaySupplier = Objects.requireNonNull(builder.nextDelaySupplier, "nextDelaySupplier must not be null");

		this.logger = Nullables.nonNullOrDefault(builder.logger, LoggerAdapter::none);
		this.minDelay = Nullables.nonNullOrDefault(builder.minDelay, () -> Default.MIN_DELAY);
		if (minDelay.isNegative() || minDelay.isZero()) {
			throw new IllegalArgumentException("minDelay must be positive");
		}
		this.taskCancelRetry = Nullables.nonNullOrDefault(builder.taskCancelRetry, Retry::noRetry);
		this.interruptOnCancel = builder.interruptOnCancel;
	}

	/**
	 * Creates a new builder for {@link ReschedulingTask}.
	 *
	 * @return the builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns the name of the task.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Closes the task, ensuring that any scheduled executions are cancelled and resources are released.
	 * <p>
	 * Note: the scheduled task (future) and the scheduler will only be closed if the scheduler is managed by this instance.
	 *
	 * @throws Exception if an error occurs during closing
	 */
	@Override
	public void close() throws Exception {
		enabled.set(false);
		scheduler.closeIfManaged(this::closeRefreshScheduler);
	}

	/**
	 * Closes the refresh scheduler by attempting to cancel any currently scheduled task. If cancellation fails, it forces a
	 * shutdown of the scheduler and logs remaining tasks count.
	 */
	@SuppressWarnings("resource")
	private void closeRefreshScheduler() {
		ScheduledExecutorService executor = scheduler.unwrap();
		boolean cancelled = cancelScheduledTask();
		if (cancelled) {
			executor.close();
		} else {
			List<Runnable> remaining = executor.shutdownNow();
			logger.warn("[{}] Scheduler closed with {} remaining tasks.", name, remaining.size());
		}
	}

	/**
	 * Attempts to cancel the currently scheduled task, if any. It uses the provided retry strategy to handle cancellation
	 * failures. If there is no task to cancel or if the task is already done, it returns true. Otherwise, it returns the
	 * result of the cancellation attempt.
	 *
	 * @return true if the task was successfully cancelled or there was no task to cancel, false if cancellation failed
	 */
	private boolean cancelScheduledTask() {
		ScheduledFuture<?> task = scheduledTask.getAndSet(null);
		return cancel(task);
	}

	/**
	 * Attempts to cancel the given scheduled task if it is not null and not already done. Uses the provided retry strategy
	 * to handle transient cancellation failures.
	 *
	 * @param task the task to cancel
	 * @return true if the task was successfully cancelled or was already done, false if cancellation failed
	 */
	private boolean cancel(final ScheduledFuture<?> task) {
		if (isDone(task)) {
			return true;
		}
		return taskCancelRetry.until(() -> task.cancel(interruptOnCancel), Boolean::booleanValue);
	}

	/**
	 * Executes the refresh task and schedules the next execution. If the task is disabled, it will skip execution and
	 * rescheduling.
	 */
	private void execute() {
		if (isDisabled()) {
			logger.debug("[{}] Scheduler is disabled, skipping execution.", name);
			return;
		}
		try {
			refreshTask.run();
		} catch (Exception e) {
			logger.error("[{}] Error during task execution.", name, e);
		} finally {
			scheduleNext();
		}
	}

	/**
	 * Schedules the next execution based on the delay provided by the {@link #nextDelaySupplier}.
	 */
	@SuppressWarnings("resource")
	private void scheduleNext() {
		if (isDisabled()) {
			logger.debug("[{}] Scheduler is disabled, skipping scheduling next execution.", name);
			return;
		}
		Duration delay = Comparables.max(nextDelaySupplier.get(), minDelay);
		logger.debug("[{}] Scheduling next execution in {}ms.", name, delay.toMillis());

		ScheduledFuture<?> newTask = scheduler.unwrap().schedule(this::execute, delay.toMillis(), TimeUnit.MILLISECONDS);
		ScheduledFuture<?> oldTask = scheduledTask.getAndSet(newTask);
		cancel(oldTask);
	}

	/**
	 * Starts the self-rescheduling task (if not already running).
	 *
	 * @return true if the task was started, false if it was already enabled
	 */
	public boolean enable() {
		if (!enabled.compareAndSet(false, true)) {
			return false;
		}
		logger.debug("[{}] Enabling rescheduling task.", name);
		execute();
		return true;
	}

	/**
	 * Stops the self-rescheduling task (if running).
	 *
	 * @return true if the task was disabled, false if it was already disabled
	 */
	public boolean disable() {
		if (!enabled.compareAndSet(true, false)) {
			return false;
		}
		logger.debug("[{}] Disabling rescheduling task.", name);
		cancelScheduledTask();
		return true;
	}

	/**
	 * Returns true if the task is currently enabled, false otherwise.
	 *
	 * @return true if enabled, false if disabled
	 */
	public boolean isEnabled() {
		return enabled.get();
	}

	/**
	 * Returns true if the task is currently disabled, false otherwise.
	 *
	 * @return true if disabled, false if enabled
	 */
	public boolean isDisabled() {
		return !isEnabled();
	}

	/**
	 * Returns true if there is a currently scheduled task that has not yet completed, false otherwise.
	 *
	 * @return true if a task is scheduled and not done, false otherwise
	 */
	public boolean isScheduled() {
		return isNotDone(scheduledTask.get());
	}

	/**
	 * Returns true if the given task is currently scheduled (not null and not done), false otherwise.
	 *
	 * @param task the task to check
	 * @return true if the task is scheduled, false if it is null or already done
	 */
	public static boolean isNotDone(final ScheduledFuture<?> task) {
		return !isDone(task);
	}

	/**
	 * Returns true if the given task is not currently scheduled (null or already done), false otherwise.
	 *
	 * @param task the task to check
	 * @return true if the task is not scheduled, false if it is scheduled and not done
	 */
	public static boolean isDone(final ScheduledFuture<?> task) {
		return task == null || task.isDone();
	}

	/**
	 * Returns the logger used by this task.
	 *
	 * @return the logger
	 */
	protected LoggerAdapter getLogger() {
		return logger;
	}

	/**
	 * Returns the scheduler used for executing the task.
	 *
	 * @return the scheduler
	 */
	protected ScopedResource<ScheduledExecutorService> getScheduler() {
		return scheduler;
	}

	/**
	 * Returns the refresh task that is executed and rescheduled.
	 *
	 * @return the refresh task
	 */
	protected Runnable getRefreshTask() {
		return refreshTask;
	}

	/**
	 * Returns the supplier that provides the delay until the next execution after each run.
	 *
	 * @return the next delay supplier
	 */
	protected Supplier<Duration> getNextDelaySupplier() {
		return nextDelaySupplier;
	}

	/**
	 * Returns the delay until the next execution after each run by invoking the {@link #nextDelaySupplier}.
	 *
	 * @return the next delay
	 */
	protected Duration getNextDelay() {
		return getNextDelaySupplier().get();
	}

	/**
	 * Returns the minimum allowed delay between executions.
	 *
	 * @return the minimum delay
	 */
	protected Duration getMinDelay() {
		return minDelay;
	}

	/**
	 * Returns the retry strategy used for task cancellation.
	 *
	 * @return the task cancellation retry strategy
	 */
	protected Retry getTaskCancelRetry() {
		return taskCancelRetry;
	}

	/**
	 * Returns whether to interrupt the task thread when canceling.
	 *
	 * @return true if the task should be interrupted on cancel, false otherwise
	 */
	protected boolean isInterruptOnCancel() {
		return interruptOnCancel;
	}

	/**
	 * Builder for {@link ReschedulingTask}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Builder {

		/**
		 * Name of the task, used for logging and identification. Must not be null.
		 */
		private String name;

		/**
		 * Scheduler for executing the task. The task will manage its own scheduling and cancellation. Must not be null.
		 */
		private ScopedResource<ScheduledExecutorService> scheduler;

		/**
		 * The task to be executed and rescheduled. Must not be null.
		 */
		private Runnable refreshTask;

		/**
		 * Supplier that provides the delay until the next execution after each run. The delay is recalculated after each
		 * execution. Must not be null.
		 */
		private Supplier<Duration> nextDelaySupplier;

		/**
		 * Minimum allowed delay between executions. This is a safeguard against negative or zero delays that could cause rapid
		 * rescheduling. Optional, defaults to {@link Default#MIN_DELAY}.
		 */
		private Duration minDelay;

		/**
		 * Retry strategy for task cancellation. Used when trying to cancel the currently scheduled task before scheduling a new
		 * one. Optional, defaults to no retry.
		 */
		private Retry taskCancelRetry;

		/**
		 * Whether to interrupt the task thread when cancelling. This is used in the cancellation logic when trying to cancel
		 * the currently scheduled task before scheduling a new one. Optional, defaults to false (do not interrupt).
		 */
		private boolean interruptOnCancel = Default.INTERRUPT_ON_CANCEL;

		/**
		 * Logger for logging task events and errors. Optional, defaults to no-op logger.
		 */
		private LoggerAdapter logger;

		/**
		 * Hide constructor to enforce usage of builder pattern.
		 */
		private Builder() {
			// empty
		}

		/**
		 * Builds the {@link ReschedulingTask} instance with the provided configuration.
		 *
		 * @return a new instance of {@link ReschedulingTask}
		 */
		public ReschedulingTask build() {
			return new ReschedulingTask(this);
		}

		/**
		 * Sets the name of the task.
		 *
		 * @param name the name of the task, must not be null
		 * @return this builder for chaining
		 */
		public Builder name(final String name) {
			this.name = Objects.requireNonNull(name, "name must not be null");
			return this;
		}

		/**
		 * Sets the scheduler for executing the task.
		 *
		 * @param scheduler the scheduler, must not be null
		 * @return this builder for chaining
		 */
		public Builder scheduler(final ScopedResource<ScheduledExecutorService> scheduler) {
			this.scheduler = Objects.requireNonNull(scheduler, "scheduler must not be null");
			return this;
		}

		/**
		 * Sets the task to be executed and rescheduled.
		 *
		 * @param refreshTask the task to execute, must not be null
		 * @return this builder for chaining
		 */
		public Builder task(final Runnable refreshTask) {
			this.refreshTask = Objects.requireNonNull(refreshTask, "refreshTask must not be null");
			return this;
		}

		/**
		 * Sets the supplier that provides the delay until the next execution after each run.
		 *
		 * @param nextDelaySupplier the supplier for next delay, must not be null
		 * @return this builder for chaining
		 */
		public Builder nextDelay(final Supplier<Duration> nextDelaySupplier) {
			this.nextDelaySupplier = Objects.requireNonNull(nextDelaySupplier, "nextDelaySupplier must not be null");
			return this;
		}

		/**
		 * Sets a fixed delay for the next execution after each run.
		 *
		 * @param fixedDelay the fixed delay, must not be null
		 * @return this builder for chaining
		 */
		public Builder nextDelay(final Duration fixedDelay) {
			return nextDelay(() -> fixedDelay);
		}

		/**
		 * Sets the retry strategy for task cancellation.
		 *
		 * @param taskCancellationRetry the retry strategy for task cancellation, optional, defaults to no retry
		 * @return this builder for chaining
		 */
		public Builder taskCancelRetry(final Retry taskCancellationRetry) {
			this.taskCancelRetry = taskCancellationRetry;
			return this;
		}

		/**
		 * Minimum allowed delay between executions (safeguard against negative/zero delays).
		 *
		 * @param minDelay the minimum delay, optional, defaults to {@link Default#MIN_DELAY}
		 * @return this builder for chaining
		 */
		public Builder minDelay(final Duration minDelay) {
			this.minDelay = minDelay;
			return this;
		}

		/**
		 * Sets whether to interrupt the task thread when canceling.
		 *
		 * @param interruptOnCancel whether to interrupt on cancel, optional, defaults to false (do not interrupt)
		 * @return this builder for chaining
		 */
		public Builder interruptOnCancel(final boolean interruptOnCancel) {
			this.interruptOnCancel = interruptOnCancel;
			return this;
		}

		/**
		 * Sets the logger for logging task events and errors.
		 *
		 * @param logger the logger, optional, defaults to no-op logger
		 * @return this builder for chaining
		 */
		public Builder logger(final LoggerAdapter logger) {
			this.logger = logger;
			return this;
		}
	}
}
