package org.morphix.lang.retry.delay;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.retry.DelayStrategy;

/**
 * Fixed delay strategy.
 * <p>
 * Returns the same delay for every retry attempt.
 *
 * @author Radu Sebastian LAZIN
 */
public class FixedDelayStrategy implements DelayStrategy {

	/**
	 * Fixed delay value returned for each attempt.
	 */
	private final long delay;

	/**
	 * Time unit for the configured delay.
	 */
	private final TimeUnit timeUnit;

	/**
	 * Creates a new fixed delay strategy.
	 *
	 * @param delay fixed delay value
	 * @param timeUnit time unit for the delay
	 */
	public FixedDelayStrategy(final long delay, final TimeUnit timeUnit) {
		this.delay = delay;
		this.timeUnit = timeUnit;
	}

	/**
	 * Factory method for a fixed delay strategy.
	 *
	 * @param delay fixed delay value
	 * @param timeUnit time unit for the delay
	 * @return a new fixed delay strategy
	 */
	public static FixedDelayStrategy of(final long delay, final TimeUnit timeUnit) {
		return new FixedDelayStrategy(delay, timeUnit);
	}

	/**
	 * @see DelayStrategy#timeUnit()
	 */
	@Override
	public TimeUnit timeUnit() {
		return timeUnit;
	}

	/**
	 * @see DelayStrategy#delay(int)
	 *
	 * @throws IllegalArgumentException if {@code attempt <= 0}
	 */
	@Override
	public long delay(final int attempt) {
		if (attempt <= 0) {
			throw new IllegalArgumentException("Attempt must be greater than 0");
		}
		return delay;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final FixedDelayStrategy that = (FixedDelayStrategy) o;
		return this.delay == that.delay
				&& this.timeUnit == that.timeUnit;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(delay, timeUnit);
	}
}
