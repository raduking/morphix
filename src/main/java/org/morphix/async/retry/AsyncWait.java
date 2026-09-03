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
package org.morphix.async.retry;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.morphix.lang.Copyable;
import org.morphix.lang.thread.Threads;
import org.morphix.reflection.Constructors;

/**
 * Asynchronous wait interface. Provides non-blocking wait strategies that complete a {@link CompletableFuture} once the
 * wait is over.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface AsyncWait extends Copyable {

	/**
	 * Default values name space.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class Default {

		/**
		 * Default interval for waiting.
		 */
		public static final long INTERVAL = 1;

		/**
		 * Default time unit for waiting.
		 */
		public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

		/**
		 * Default poll interval for waiting (50 milliseconds).
		 */
		public static final Duration POLL_INTERVAL = Duration.ofMillis(50);

		/**
		 * Private constructor.
		 */
		private Default() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * Returns true if we should keep waiting, false otherwise.
	 *
	 * @return true if we should keep waiting, false otherwise
	 */
	boolean keepWaiting();

	/**
	 * Defers the wait asynchronously and returns a {@link CompletableFuture} that completes once the wait is over.
	 * <p>
	 * By default it schedules a sleep for {@link #interval()} in {@link #timeUnit()} on the {@link #executor()}.
	 * Implementations may override {@link #interval()}, {@link #timeUnit()} or {@link #executor()} to customize the wait.
	 *
	 * @return a future that completes once the wait is over
	 */
	default CompletableFuture<Void> defer() {
		CompletableFuture<Void> future = new CompletableFuture<>();
		try {
			CompletableFuture.delayedExecutor(interval(), timeUnit(), executor())
					.execute(() -> future.complete(null));
		} catch (Exception e) {
			future.completeExceptionally(e);
		}
		return future;
	}

	/**
	 * Starts the waiting process.
	 */
	default void start() {
		// empty
	}

	/**
	 * Returns a copy of the current object.
	 * <p>
	 * This is needed for thread safety. By default, it doesn't create a copy, so any class that doesn't implement it is not
	 * thread safe.
	 *
	 * @return a copy of the current object
	 */
	@Override
	default AsyncWait copy() {
		return this;
	}

	/**
	 * Returns the executor used to schedule delays.
	 *
	 * @return the executor used to schedule delays
	 */
	default Executor executor() {
		return Threads.sharedVirtualThreadPerTaskExecutor();
	}

	/**
	 * Returns the sleep interval.
	 *
	 * @return the sleep interval
	 */
	default long interval() {
		return Default.INTERVAL;
	}

	/**
	 * Returns the sleep time unit.
	 *
	 * @return the sleep time unit
	 */
	default TimeUnit timeUnit() {
		return Default.TIME_UNIT;
	}

	/**
	 * Waits until the given condition is true or the timeout is reached. The condition is checked at intervals defined by
	 * the poll interval. If the timeout is zero, it will wait indefinitely until the condition is true. If the timeout is
	 * negative, it will return immediately.
	 *
	 * @param condition condition to check
	 * @param timeout maximum time to wait for the condition to be true
	 * @param pollInterval interval between condition checks
	 * @return a future that completes with true if the condition was met within the timeout, false otherwise
	 */
	static CompletableFuture<Boolean> until(final BooleanSupplier condition, final Duration timeout,
			final Duration pollInterval) {
		boolean conditionMet = condition.getAsBoolean();
		if (conditionMet || timeout.isNegative()) {
			return CompletableFuture.completedFuture(conditionMet);
		}
		return pollUntil(condition, timeout, pollInterval, System.nanoTime() + timeout.toNanos());
	}

	/**
	 * Waits until the given condition is true or the timeout is reached. The condition is checked at intervals defined by
	 * the poll interval. If the timeout is zero, it will wait indefinitely until the condition is true. If the timeout is
	 * negative, it will return immediately.
	 *
	 * @param condition condition to check
	 * @param timeout maximum time to wait for the condition to be true
	 * @return a future that completes with true if the condition was met within the timeout, false otherwise
	 */
	static CompletableFuture<Boolean> until(final BooleanSupplier condition, final Duration timeout) {
		return until(condition, timeout, Default.POLL_INTERVAL);
	}

	/**
	 * Waits until the given condition is true. The condition is checked at intervals defined by the poll interval.
	 *
	 * @param condition condition to check
	 * @return a future that completes with true if the condition was met, false if the thread was interrupted while waiting
	 */
	static CompletableFuture<Boolean> until(final BooleanSupplier condition) {
		return until(condition, Duration.ZERO, Default.POLL_INTERVAL);
	}

	/**
	 * Polls the condition until it is met, the timeout is reached or the current thread is interrupted.
	 *
	 * @param condition condition to check
	 * @param timeout maximum time to wait for the condition to be true
	 * @param pollInterval interval between condition checks
	 * @param deadline deadline in nanoseconds
	 * @return a future that completes with the condition result
	 */
	private static CompletableFuture<Boolean> pollUntil(final BooleanSupplier condition, final Duration timeout,
			final Duration pollInterval, final long deadline) {
		boolean conditionMet = condition.getAsBoolean();
		if (conditionMet || Threads.isCurrentInterrupted()) {
			return CompletableFuture.completedFuture(conditionMet);
		}
		if (!timeout.isZero() && System.nanoTime() >= deadline) {
			return CompletableFuture.completedFuture(false);
		}
		CompletableFuture<Void> delay = new CompletableFuture<>();
		try {
			CompletableFuture.delayedExecutor(pollInterval.toNanos(), TimeUnit.NANOSECONDS,
					Threads.sharedVirtualThreadPerTaskExecutor()).execute(() -> delay.complete(null));
		} catch (Exception e) {
			delay.completeExceptionally(e);
		}
		return delay.thenCompose(v -> pollUntil(condition, timeout, pollInterval, deadline));
	}
}
