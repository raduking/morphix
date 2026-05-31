package org.morphix.lang.retry;

import java.util.concurrent.TimeUnit;

import org.morphix.reflection.Constructors;

/**
 * Strategy for computing consecutive delay values.
 * <p>
 * Attempt indexes are 1-based.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface DelayStrategy {

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
	 * @param attempt retry attempt number (1-based)
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
	 * Returns the delay for the first attempt.
	 *
	 * @return delay for attempt 1
	 */
	default long delay() {
		return delay(1);
	}

	/**
	 * Returns the delay for the first attempt converted to the provided unit.
	 *
	 * @param timeUnit target time unit
	 * @return converted delay for attempt 1
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
}
