/*
 * Copyright 2025 the original author or authors.
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
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.morphix.lang.Unchecked;
import org.morphix.lang.function.Runnables;

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
		 * @param executor executor service (can be null)
		 */
		void apply(List<? extends Runnable> runnables, ExecutorService executor);
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
		 * Will run each runnable in parallel using standard Java threads.
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
		 * executor is {@code null} a single thread executor will be used by default.
		 * <p>
		 * By default, when using: {@code Threads.execute(runnables, ExecutionType.EXECUTOR)} the single thread executor will be
		 * used.
		 * <p>
		 * If you want the runnables to run on a specific executor use:
		 *
		 * <pre>
		 * Threads.execute(runnables, executor)
		 * </pre>
		 */
		EXECUTOR((runnables, executor) -> {
			if (null == executor) {
				try (ExecutorService actualExecutor = Executors.newSingleThreadExecutor()) {
					Threads.execute(runnables, actualExecutor);
				}
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
		public void apply(final List<? extends Runnable> runnables, final ExecutorService executor) {
			this.execution.apply(runnables, executor);
		}

	}

	/**
	 * Private constructor.
	 */
	private Threads() {
		// empty
	}

	/**
	 * Puts the current thread to sleep for the given interval with the given time unit. See {@link TimeUnit} for more
	 * details. If the interval is zero no sleep will be done.
	 *
	 * @param interval interval
	 * @param timeUnit time unit
	 */
	public static void safeSleep(final long interval, final TimeUnit timeUnit) {
		if (0 == interval) {
			return;
		}
		try {
			timeUnit.sleep(interval);
		} catch (InterruptedException e) {
			handleInterruptedException();
		}
	}

	/**
	 * Puts the current thread to sleep for the given duration.
	 *
	 * @param duration sleep duration
	 */
	public static void safeSleep(final Duration duration) {
		safeSleep(duration.toMillis(), TimeUnit.MILLISECONDS);
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
		}
		return false;
	}

	/**
	 * Helper method that calls {@link Thread#join()} on the given thread, with {@link InterruptedException} handling.
	 *
	 * @param thread thread object
	 */
	public static void safeJoin(final Thread thread) {
		try {
			thread.join();
		} catch (InterruptedException e) {
			handleInterruptedException();
		}
	}

	/**
	 * Runs each runnable in the list with the given execution type. It only returns after all threads have completed.<br/>
	 * By default, when using: {@code Threads.execute(runnables, ExecutionType.EXECUTOR)} the single thread executor will be
	 * used.
	 * <p>
	 * If you want the runnables to run on a specific executor use: {@link #execute(List, ExecutorService)}
	 *
	 * @param <T> runnable type
	 *
	 * @param runnables list of runnables
	 * @param executionType execution type
	 */
	public static <T extends Runnable> void execute(final List<T> runnables, final ExecutionType executionType) {
		executionType.apply(runnables, null);
	}

	/**
	 * Runs all runnables in the list in different threads. It only returns after all threads have completed.
	 *
	 * @param <T> runnable type
	 *
	 * @param runnables list of runnables
	 * @param executor the executor
	 */
	public static <T extends Runnable> void execute(final List<T> runnables, final ExecutorService executor) {
		ExecutionType.EXECUTOR.apply(runnables, executor);
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
	public static <T> void executeForEachIn(final List<T> list, final Consumer<T> task, final ExecutorService executor) {
		execute(tasksForEachIn(list, task), executor);
	}

	/**
	 * Returns the value from the given supplier within the given timeout. If the timeout passes before the supplier returns
	 * the value a {@link TimeoutException} is thrown.
	 * <p>
	 * Note: the implementation intentionally uses no try with resources because closing the executor too early would block
	 * to cancel the task, and we want to make sure the executor is closed in the end after the manual task cancellation to
	 * keep the {@link TimeoutException} behavior.
	 *
	 * @param <T> the type of results supplied by the provided supplier
	 *
	 * @param timeout timeout
	 * @param valueSupplier value supplier
	 * @return supplier value if computed within the timeout
	 */
	public static <T> T execute(final Duration timeout, final Supplier<T> valueSupplier) {
		Future<T> task = null;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			task = executor.submit(valueSupplier::get);
			return task.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (Exception e) { // NOSONAR the exception is re-thrown as is
			if (null != task) {
				task.cancel(true);
			}
			return Unchecked.Undeclared.reThrow(e);
		} finally {
			executor.close();
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
}
