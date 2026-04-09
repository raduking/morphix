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
package org.morphix.lang.retry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

import org.morphix.lang.thread.Threads;
import org.morphix.reflection.Constructors;

/**
 * Wait interface.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface Wait {

	/**
	 * Default values name space.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class Default {

		/**
		 * Default sleep action used for waiting.
		 */
		public static final BiConsumer<Long, TimeUnit> SLEEP_ACTION = Threads::safeSleep;

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
	 * Waits, it is not called wait because of java object restriction.
	 */
	default void now() {
		sleepAction().accept(interval(), timeUnit());
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
	default Wait copy() {
		return this;
	}

	/**
	 * Returns the sleep action.
	 *
	 * @return the sleep action
	 */
	default BiConsumer<Long, TimeUnit> sleepAction() {
		return Default.SLEEP_ACTION;
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
	 * @return true if the condition was met within the timeout, false otherwise
	 */
	public static boolean until(final BooleanSupplier condition, final Duration timeout, final Duration pollInterval) {
		boolean conditionMet = condition.getAsBoolean();
		if (conditionMet || timeout.isNegative()) {
			return conditionMet;
		}
		long deadline = System.nanoTime() + timeout.toNanos();
		while (timeout.isZero() || System.nanoTime() < deadline) {
			conditionMet = condition.getAsBoolean();
			if (conditionMet || Threads.isCurrentInterrupted()) {
				break;
			}
			Threads.safeSleep(pollInterval);
		}
		return conditionMet;
	}

	/**
	 * Waits until the given condition is true or the timeout is reached. The condition is checked at intervals defined by
	 * the poll interval. If the timeout is zero, it will wait indefinitely until the condition is true. If the timeout is
	 * negative, it will return immediately.
	 *
	 * @param condition condition to check
	 * @param timeout maximum time to wait for the condition to be true
	 * @return true if the condition was met within the timeout, false otherwise
	 */
	public static boolean until(final BooleanSupplier condition, final Duration timeout) {
		return until(condition, timeout, Default.POLL_INTERVAL);
	}

	/**
	 * Waits until the given condition is true. The condition is checked at intervals defined by the poll interval.
	 *
	 * @param condition condition to check
	 * @return true if the condition was met, false if the thread was interrupted while waiting
	 */
	public static boolean until(final BooleanSupplier condition) {
		return until(condition, Duration.ZERO, Default.POLL_INTERVAL);
	}
}
