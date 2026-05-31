package org.morphix.lang.retry.delay;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.retry.DelayStrategy;

/**
 * Exponential delay strategy.
 * <p>
 * Delay is computed using:
 *
 * <pre>
 * minDelay * multiplier ^ (attempt - 1)
 * </pre>
 *
 * and capped at {@code maxDelay}. Attempt indexes are 1-based, so for attempt {@code 1} the delay is {@code minDelay}.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ExponentialDelayStrategy implements DelayStrategy {

	/**
	 * Initial delay used for the first attempt.
	 */
	private final long minDelay;

	/**
	 * Maximum delay cap applied to computed delays.
	 */
	private final long maxDelay;

	/**
	 * Time unit used for all delay values.
	 */
	private final TimeUnit timeUnit;

	/**
	 * Exponential growth factor applied between attempts.
	 */
	private final double multiplier;

	/**
	 * Creates a new exponential strategy.
	 *
	 * @param minDelay initial delay
	 * @param maxDelay maximum delay
	 * @param timeUnit time unit for delay values
	 * @param multiplier exponential multiplier (must be greater than 1)
	 * @throws IllegalArgumentException if {@code maxDelay < minDelay} or if {@code multiplier <= 1}
	 */
	public ExponentialDelayStrategy(final long minDelay, final long maxDelay, final TimeUnit timeUnit, final double multiplier) {
		this.minDelay = minDelay;
		this.maxDelay = maxDelay;
		this.timeUnit = Objects.requireNonNull(timeUnit, "Time unit cannot be null");
		if (maxDelay < minDelay) {
			throw new IllegalArgumentException("Maximum delay cannot be smaller than minimum delay");
		}
		if (multiplier <= 1d) {
			throw new IllegalArgumentException("Multiplier must be greater than 1");
		}
		this.multiplier = multiplier;
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
		long exponent = Math.max(0, attempt - 1);
		double delay = minDelay * Math.pow(multiplier, exponent);
		return Math.min((long) Math.ceil(delay), maxDelay);
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
		final ExponentialDelayStrategy that = (ExponentialDelayStrategy) o;
		return this.minDelay == that.minDelay
				&& this.maxDelay == that.maxDelay
				&& this.timeUnit == that.timeUnit
				&& Double.compare(this.multiplier, that.multiplier) == 0;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(minDelay, maxDelay, timeUnit, multiplier);
	}
}
