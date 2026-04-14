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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.morphix.lang.Unchecked;
import org.morphix.lang.function.Runnables;
import org.morphix.lang.retry.Wait;

/**
 * Utility methods for working with threads.
 *
 * @author Radu Sebastian LAZIN
 */
public class Threads {

	/**
	 * Threads execution functional interface.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@FunctionalInterface
	public interface Execution {

		/**
		 * Applies this function to the given arguments.
		 *
		 * @param runnables list of Runnable elements
		 * @param executor executor (can be null)
		 */
		void run(List<? extends Runnable> runnables, Executor executor);

		/**
		 * Applies this function to the given list of runnables, with a null executor.
		 *
		 * @param runnables list of Runnable elements
		 */
		default void run(final List<? extends Runnable> runnables) {
			run(runnables, null);
		}
	}

	/**
	 * Simple enum to handle different threads execution types.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public enum ExecutionType implements Execution {

		/**
		 * Will run each runnable sequentially.
		 */
		SEQUENTIAL((runnables, executor) -> {
			for (Runnable runnable : runnables) {
				runnable.run();
			}
		}),

		/**
		 * Will run each runnable in parallel using standard Java threads (platform threads).
		 */
		PARALLEL((runnables, executor) -> {
			List<Thread> threads = runnables.stream()
					.map(runnable -> Thread.ofPlatform().start(runnable))
					.toList();
			threads.forEach(Threads::safeJoin);
		}),

		/**
		 * Will run each runnable using {@link CompletableFuture#runAsync(Runnable)}.
		 */
		ASYNC((runnables, executor) -> {
			List<CompletableFuture<Void>> futures = runnables.stream()
					.map(CompletableFuture::runAsync)
					.toList();
			futures.forEach(CompletableFuture::join);
		}),

		/**
		 * Will run each runnable using {@link CompletableFuture#runAsync(Runnable, Executor)} and the executor supplied. If the
		 * executor is {@code null} then the tasks will be executed on virtual threads.
		 * <p>
		 * By default, when using: {@code Threads.execute(runnables, ExecutionType.EXECUTOR)} since no executor is supplied, the
		 * default execution type will be used.
		 * <p>
		 * If you want the runnables to run on a specific executor use:
		 *
		 * <pre>
		 * Threads.execute(runnables, executor)
		 * </pre>
		 */
		EXECUTOR((runnables, executor) -> {
			if (null == executor) {
				Threads.execute(runnables, Threads.defaultExecutionType());
			} else {
				List<CompletableFuture<Void>> futures = runnables.stream()
						.map(runnable -> CompletableFuture.runAsync(runnable, executor))
						.toList();
				futures.forEach(CompletableFuture::join);
			}
		}),

		/**
		 * Will run each runnable in parallel using virtual threads.
		 */
		VIRTUAL((runnables, executor) -> {
			List<Thread> threads = runnables.stream()
					.map(runnable -> Thread.ofVirtual().start(runnable))
					.toList();
			threads.forEach(Threads::safeJoin);
		});

		/**
		 * Holds the way the runnables will execute.
		 */
		private final Execution execution;

		/**
		 * Constructor.
		 *
		 * @param execution the way the runnables will execute
		 */
		ExecutionType(final Execution execution) {
			this.execution = execution;
		}

		/**
		 * Main execution method.
		 *
		 * @param runnables list of Runnable elements
		 * @param executor executor service (can be null)
		 */
		@Override
		public void run(final List<? extends Runnable> runnables, final Executor executor) {
			this.execution.run(runnables, executor);
		}
	}

	/**
	 * Private constructor.
	 */
	private Threads() {
		// empty
	}

	/**
	 * Puts the current thread to sleep for the given interval with the given time unit. See {@link TimeUnit#sleep(long)}
	 * for more details. If the interval is zero no sleep will be done.
	 * <p>
	 * Note: it is recommended to set interrupt status for the current thread in case {@link InterruptedException} is
	 * caught, so the method will do it by default. You can check the interrupt status with {@link #isCurrentInterrupted()}
	 * after calling this method to know if the sleep was interrupted or not.
	 *
	 * @param interval interval
	 * @param timeUnit time unit
	 * @return true if the sleep completed successfully, false if it was interrupted
	 */
	public static boolean safeSleep(final long interval, final TimeUnit timeUnit) {
		if (interval <= 0) {
			return true;
		}
		try {
			timeUnit.sleep(interval);
			return true;
		} catch (InterruptedException e) {
			handleInterruptedException();
			return false;
		}
	}

	/**
	 * Puts the current thread to sleep for the given duration.
	 *
	 * @param duration sleep duration
	 * @return true if the sleep completed successfully, false if it was interrupted
	 */
	public static boolean safeSleep(final Duration duration) {
		return safeSleep(duration.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Helper method that calls {@link CountDownLatch#await()} on the given latch, with {@link InterruptedException}
	 * handling.
	 *
	 * @param latch latch
	 */
	public static void safeWait(final CountDownLatch latch) {
		try {
			latch.await();
		} catch (InterruptedException e) {
			handleInterruptedException();
		}
	}

	/**
	 * Helper method that calls {@link CountDownLatch#await(long, TimeUnit)} on the given latch and timeout, with
	 * {@link InterruptedException} handling.
	 *
	 * @param latch latch
	 * @param duration timeout
	 * @return true if successful
	 */
	public static boolean safeWait(final CountDownLatch latch, final Duration duration) {
		try {
			return latch.await(duration.toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			handleInterruptedException();
			return false;
		}
	}

	/**
	 * Helper method that calls {@link Thread#join()} on the given thread, with {@link InterruptedException} handling.
	 *
	 * @param thread thread object
	 * @return true if successful, false if the join was interrupted
	 */
	public static boolean safeJoin(final Thread thread) {
		try {
			thread.join();
			return true;
		} catch (InterruptedException e) {
			handleInterruptedException();
			return false;
		}
	}

	/**
	 * Runs each runnable in the list with the given execution type. It only returns after all threads have completed.<br/>
	 * By default, when using: {@code Threads.execute(runnables, ExecutionType.EXECUTOR)} the single thread executor will be
	 * used.
	 * <p>
	 * If you want the runnables to run on a specific executor use: {@link #execute(List, Executor)}
	 *
	 * @param <T> runnable type
	 *
	 * @param runnables list of runnables
	 * @param executionType execution type
	 */
	public static <T extends Runnable> void execute(final List<T> runnables, final ExecutionType executionType) {
		executionType.run(runnables);
	}

	/**
	 * Runs all runnables in the list in different threads. It only returns after all threads have completed.
	 *
	 * @param <T> runnable type
	 *
	 * @param runnables list of runnables
	 * @param executor the executor
	 */
	public static <T extends Runnable> void execute(final List<T> runnables, final Executor executor) {
		ExecutionType.EXECUTOR.run(runnables, executor);
	}

	/**
	 * It is recommended to set interrupt status for the current thread in case {@link InterruptedException} is caught.
	 *
	 * @param <T> generic throwable
	 *
	 * @param throwable caught exception
	 */
	public static <T extends Throwable> void handleInterruptedException(final T throwable) {
		if (throwable instanceof InterruptedException) {
			handleInterruptedException();
		}
	}

	/**
	 * It is recommended to set interrupt status for the current thread in case {@link InterruptedException} is caught.
	 */
	public static void handleInterruptedException() {
		Thread.currentThread().interrupt();
	}

	/**
	 * Creates a list of {@link Runnable} tasks for each element in the given list. The consumer determines what will be
	 * executed for each element. The resulting can then be run with any of the {@code execute(...)} methods.
	 *
	 * @param <T> element type
	 *
	 * @param list input list
	 * @param task task to execute for each element
	 * @return list of tasks as {@link Runnable} objects
	 */
	public static <T> List<Runnable> tasksForEachIn(final List<T> list, final Consumer<T> task) {
		return list.stream().map(t -> (Runnable) () -> task.accept(t)).toList();
	}

	/**
	 * Executes the consumer for each element in the list using the given execution type.
	 *
	 * @param <T> element type
	 *
	 * @param list input list
	 * @param task task to execute for each element
	 * @param executionType execution type
	 */
	public static <T> void executeForEachIn(final List<T> list, final Consumer<T> task, final ExecutionType executionType) {
		execute(tasksForEachIn(list, task), executionType);
	}

	/**
	 * Executes the consumer for each element in the list using the given execution type.
	 *
	 * @param <T> element type
	 *
	 * @param list input list
	 * @param task task to execute for each element
	 * @param executor task executor
	 */
	public static <T> void executeForEachIn(final List<T> list, final Consumer<T> task, final Executor executor) {
		execute(tasksForEachIn(list, task), executor);
	}

	/**
	 * Returns the value from the given supplier within the given timeout. If the timeout passes before the supplier returns
	 * the value a {@link TimeoutException} is thrown.
	 * <p>
	 * Note: the implementation intentionally uses no try with resources because closing the executor too early would block
	 * to cancel the task, and we want to make sure the executor is closed in the end after the manual task cancellation to
	 * keep the {@link TimeoutException} behavior.
	 * <p>
	 *
	 * <b>Important Limitations:</b>
	 * <ul>
	 * <li>This method uses thread interruption for cancellation, which requires the executing task to be responsive to
	 * interrupts. Tasks that do not check {@link Thread#isInterrupted()} or handle {@link InterruptedException} may
	 * continue running indefinitely even after the timeout occurs.</li>
	 * <li>CPU-bound tasks with infinite loops that do not check their interrupt status <b>cannot be stopped</b> by this
	 * method. The thread will continue running and the executor will hang during shutdown, causing this method to block
	 * indefinitely.</li>
	 * <li>Tasks that perform blocking I/O, sleep, or wait operations will generally respond to interrupts and be cancelled
	 * properly.</li>
	 * <li>For tasks that must be forcibly terminated, consider running them in a separate process using {@link Process} and
	 * {@link Process#destroyForcibly()} instead.</li>
	 * </ul>
	 *
	 * <b>Cooperative Cancellation Example:</b>
	 *
	 * <pre>
	 * // Good: Task checks interrupt status
	 * Runnable responsive = () -> {
	 * 	while (!Thread.currentThread().isInterrupted()) {
	 * 		// perform work
	 * 	}
	 * };
	 *
	 * // Bad: Task ignores interrupts and will cause hanging
	 * Runnable unresponsive = () -> {
	 * 	while (true) {
	 * 		// perform work - cannot be stopped!
	 * 	}
	 * };
	 * </pre>
	 *
	 * @param <T> the type of results supplied by the provided supplier
	 *
	 * @param timeout timeout
	 * @param valueSupplier value supplier
	 * @return supplier value if computed within the timeout
	 */
	@SuppressWarnings("resource")
	public static <T> T execute(final Duration timeout, final Supplier<T> valueSupplier) {
		Future<T> task = null;
		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
		try {
			task = executor.submit(valueSupplier::get);
			return task.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (Exception e) { // NOSONAR the exception is re-thrown as is
			if (null != task) {
				task.cancel(true);
			}
			return Unchecked.Undeclared.reThrow(e);
		} finally {
			executor.shutdownNow();
			try {
				// one second should be enough for the task to respond to the interrupt
				executor.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Runs the given runnable within the given timeout. If the timeout passes before the runnable finished execution a
	 * {@link TimeoutException} is thrown.
	 *
	 * @param timeout timeout
	 * @param runnable code to run
	 */
	public static void execute(final Duration timeout, final Runnable runnable) {
		execute(timeout, Runnables.toSupplier(runnable));
	}

	/**
	 * Checks if the current thread is interrupted.
	 *
	 * @return true if the current thread is interrupted, false otherwise
	 */
	public static boolean isCurrentInterrupted() {
		return Thread.currentThread().isInterrupted();
	}

	/**
	 * Waits until the given condition is true or the timeout is reached. The condition is checked at intervals defined by
	 * the poll interval. If the timeout is zero, it will wait indefinitely until the condition is true. If the timeout is
	 * negative, it will return immediately.
	 * <p>
	 * This method delegates to {@link Wait#until(BooleanSupplier, Duration, Duration)} for the actual waiting logic, so it
	 * behaves the same as that method. The only difference is that it is exposed here for convenience when working with
	 * threads, so there is no need to import the {@link Wait} class directly when a wait for a condition in a
	 * thread-related context is needed.
	 *
	 * @param condition condition to check
	 * @param timeout maximum time to wait for the condition to be true
	 * @param pollInterval interval between condition checks
	 * @return true if the condition was met within the timeout, false otherwise
	 */
	public static boolean waitUntil(final BooleanSupplier condition, final Duration timeout, final Duration pollInterval) {
		return Wait.until(condition, timeout, pollInterval);
	}

	/**
	 * Waits until the given condition is true or the timeout is reached. The condition is checked at intervals defined by
	 * the poll interval. If the timeout is zero, it will wait indefinitely until the condition is true. If the timeout is
	 * negative, it will return immediately.
	 * <p>
	 * This method delegates to {@link Wait#until(BooleanSupplier, Duration)} for the actual waiting logic, so it behaves
	 * the same as that method. The only difference is that it is exposed here for convenience when working with threads, so
	 * there is no need to import the {@link Wait} class directly when a wait for a condition in a thread-related context is
	 * needed.
	 *
	 * @param condition condition to check
	 * @param timeout maximum time to wait for the condition to be true
	 * @return true if the condition was met within the timeout, false otherwise
	 */
	public static boolean waitUntil(final BooleanSupplier condition, final Duration timeout) {
		return Wait.until(condition, timeout);
	}

	/**
	 * Waits until the given condition is true. The condition is checked at intervals defined by the poll interval.
	 * <p>
	 * This method delegates to {@link Wait#until(BooleanSupplier)} for the actual waiting logic and behaves the same as
	 * that method. The only difference is that it is exposed here for convenience when working with threads, so there is no
	 * need to import the {@link Wait} class directly when a wait for a condition in a thread-related context is needed.
	 *
	 * @param condition condition to check
	 * @return true if the condition was met, false if the thread was interrupted while waiting
	 */
	public static boolean waitUntil(final BooleanSupplier condition) {
		return Wait.until(condition);
	}

	/**
	 * Returns the default execution type for the {@link #execute(List, ExecutionType)} method. By default it is set to
	 * {@link ExecutionType#VIRTUAL} since virtual threads are more efficient for most use cases, but it can be changed in
	 * the future if needed without breaking backward compatibility.
	 *
	 * @return default executor
	 */
	public static ExecutionType defaultExecutionType() {
		return ExecutionType.VIRTUAL;
	}
}
