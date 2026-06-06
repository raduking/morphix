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
package org.morphix.lang.retry.delay;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.Temporals;
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
	protected ExponentialDelayStrategy(final long minDelay, final long maxDelay, final TimeUnit timeUnit, final double multiplier) {
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
	 * Factory method for an exponential delay strategy.
	 *
	 * @param minDelay initial delay
	 * @param maxDelay maximum delay
	 * @param timeUnit time unit for delay values
	 * @param multiplier exponential multiplier (must be greater than 1)
	 * @return a new exponential delay strategy
	 * @throws IllegalArgumentException if {@code maxDelay < minDelay} or if {@code multiplier <= 1}
	 */
	public static ExponentialDelayStrategy of(final long minDelay, final long maxDelay, final TimeUnit timeUnit, final double multiplier) {
		return new ExponentialDelayStrategy(minDelay, maxDelay, timeUnit, multiplier);
	}

	/**
	 * Factory method for an exponential delay strategy.
	 *
	 * @param minDelay initial delay
	 * @param maxDelay maximum delay
	 * @param timeUnit time unit for delay values
	 * @param multiplier exponential multiplier (must be greater than 1)
	 * @return a new exponential delay strategy
	 */
	public static ExponentialDelayStrategy of(final Duration minDelay, final Duration maxDelay, final TimeUnit timeUnit, final double multiplier) {
		return of(Temporals.toLong(minDelay, timeUnit), Temporals.toLong(maxDelay, timeUnit), timeUnit, multiplier);
	}

	/**
	 * Factory method for an exponential delay strategy. The time unit for delay values is set to the default value defined
	 * in {@link DelayStrategy.Default#TIME_UNIT}.
	 *
	 * @param minDelay initial delay
	 * @param maxDelay maximum delay
	 * @param multiplier exponential multiplier (must be greater than 1)
	 * @return a new exponential delay strategy
	 */
	public static ExponentialDelayStrategy of(final Duration minDelay, final Duration maxDelay, final double multiplier) {
		return of(minDelay, maxDelay, Default.TIME_UNIT, multiplier);
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
		if (attempt < 0) {
			throw new IllegalArgumentException("Attempt must not be negative");
		}
		long exponent = Math.max(0, attempt);
		double delay = minDelay * Math.pow(multiplier, exponent);
		return Math.min((long) Math.ceil(delay), maxDelay);
	}

	/**
	 * @see DelayStrategy#copy()
	 */
	@Override
	public DelayStrategy copy() {
		return ExponentialDelayStrategy.of(minDelay, maxDelay, timeUnit, multiplier);
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
