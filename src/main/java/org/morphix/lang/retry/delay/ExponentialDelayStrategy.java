package org.morphix.lang.retry.delay;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.retry.DelayStrategy;

/**
 * Exponential backoff delay strategy.
 * <p>
 * Delay is computed using:
 *
 * <pre>
 * minDelay * multiplier ^ (attempt - 1)
 * </pre>
 *
 * and capped at {@code maxDelay}.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ExponentialDelayStrategy implements DelayStrategy {

	private final long minDelay;
	private final long maxDelay;
	private final TimeUnit timeUnit;
	private final double multiplier;

	/**
	 * Creates a new exponential backoff strategy.
	 *
	 * @param minDelay initial delay
	 * @param maxDelay maximum delay
	 * @param multiplier exponential multiplier (must be greater than 1)
	 */
	public ExponentialDelayStrategy(final long minDelay, final long maxDelay, final TimeUnit timeUnit, final double multiplier) {
		this.minDelay = Objects.requireNonNull(minDelay, "Minimum delay cannot be null");
		this.maxDelay = Objects.requireNonNull(maxDelay, "Maximum delay cannot be null");
		this.timeUnit = Objects.requireNonNull(timeUnit, "Time unit cannot be null");
		if (maxDelay < minDelay) {
			throw new IllegalArgumentException("Maximum delay cannot be smaller than minimum delay");
		}
		if (multiplier <= 1d) {
			throw new IllegalArgumentException("Multiplier must be greater than 1");
		}
		this.multiplier = multiplier;
	}

	@Override
	public TimeUnit timeUnit() {
		return timeUnit;
	}

	@Override
	public long delay(final long attempt) {
		if (attempt <= 0) {
			throw new IllegalArgumentException("Attempt must be greater than 0");
		}
		double delay = minDelay * Math.pow(multiplier, attempt - 1d);
		return Math.min((long) Math.ceil(delay), maxDelay);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ExponentialDelayStrategy that = (ExponentialDelayStrategy) o;
		return minDelay == that.minDelay
				&& maxDelay == that.maxDelay
				&& timeUnit == that.timeUnit
				&& Double.compare(that.multiplier, multiplier) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(minDelay, maxDelay, timeUnit, multiplier);
	}
}
