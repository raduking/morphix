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
package org.morphix.lang.retry.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.Copyable;
import org.morphix.lang.thread.Threads;
import org.morphix.reflection.Constructors;

/**
 * Asynchronous wait interface. Provides non-blocking wait strategies that complete a {@link CompletableFuture} once the
 * wait is over.
 *
 * @author Radu Sebastian LAZIN
 */
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
		public static final long INTERVAL = 50;

		/**
		 * Default time unit for waiting.
		 */
		public static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

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
		executor().execute(() -> {
			try {
				Thread.sleep(TimeUnit.MILLISECONDS.convert(interval(), timeUnit()));
				future.complete(null);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				future.completeExceptionally(e);
			}
		});
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
}
