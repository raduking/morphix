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

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.Copyable;
import org.morphix.reflection.Constructors;

/**
 * Strategy for computing consecutive delay values.
 * <p>
 * Attempt indexes are 0-based.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface DelayStrategy extends Copyable {

	/**
	 * Default values namespace.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Default {

		/**
		 * Default time unit used for delay strategies: milliseconds.
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
	 * Returns the delay for the provided attempt.
	 *
	 * @param attempt retry attempt number (0-based)
	 * @return delay value for the attempt
	 */
	long delay(int attempt);

	/**
	 * Returns the time unit of delays produced by this strategy.
	 *
	 * @return delay time unit
	 */
	default TimeUnit timeUnit() {
		return Default.TIME_UNIT;
	}

	/**
	 * Returns the corresponding {@link ChronoUnit} for the time unit of this strategy.
	 *
	 * @return chrono unit corresponding to the time unit of this strategy
	 */
	default ChronoUnit chronoUnit() {
		return timeUnit().toChronoUnit();
	}

	/**
	 * Returns the delay for the first attempt (0-based). It is equivalent to {@code delay(0)}.
	 *
	 * @return delay for the first attempt
	 */
	default long delay() {
		return delay(0);
	}

	/**
	 * Returns the delay for the first attempt converted to the provided unit.
	 *
	 * @param timeUnit target time unit
	 * @return converted delay for the first attempt
	 * @throws NullPointerException if {@code timeUnit} is null
	 */
	default long delay(final TimeUnit timeUnit) {
		return timeUnit.convert(delay(), timeUnit());
	}

	/**
	 * Returns the delay for the provided attempt converted to the provided unit.
	 *
	 * @param attempt retry attempt number (1-based)
	 * @param timeUnit target time unit
	 * @return converted delay for the attempt
	 * @throws NullPointerException if {@code timeUnit} is null
	 */
	default long delay(final int attempt, final TimeUnit timeUnit) {
		return timeUnit.convert(delay(attempt), timeUnit());
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
	default DelayStrategy copy() {
		return this;
	}
}
