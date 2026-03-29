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

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

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
}
