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
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.morphix.lang.retry.delay.FixedDelayStrategy;
import org.morphix.reflection.Constructors;

/**
 * Timeout wait implementation.
 *
 * @author Radu Sebastian LAZIN
 */
public class WaitTimeout implements Wait {

	/**
	 * Default values name space.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Default {

		/**
		 * Default timeout: 30 seconds.
		 */
		public static final Duration TIMEOUT = Duration.ofSeconds(30);

		/**
		 * Default sleep: 1 second.
		 */
		public static final Duration SLEEP = Duration.ofSeconds(1);

		/**
		 * Private constructor.
		 */
		private Default() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * Default Wait object.
	 */
	public static final WaitTimeout DEFAULT = WaitTimeout.of(Default.TIMEOUT, Default.SLEEP);

	/**
	 * Timeout
	 */
	private final long timeout;

	/**
	 * Time unit for timeout.
	 */
	private final TimeUnit timeoutTimeUnit;

	/**
	 * Strategy that determines the sleep interval between checks.
	 */
	private final DelayStrategy delayStrategy;

	/**
	 * Counts the number of attempts.
	 */
	private final AtomicInteger attempt = new AtomicInteger(0);

	/**
	 * Start time.
	 */
	private Instant start;

	/**
	 * Private constructor.
	 *
	 * @param timeout timeout
	 * @param timeoutTimeUnit timeout time unit
	 * @param delayStrategy strategy that determines the sleep interval between checks
	 */
	protected WaitTimeout(final long timeout, final TimeUnit timeoutTimeUnit, final DelayStrategy delayStrategy) {
		this.timeout = timeout;
		this.timeoutTimeUnit = timeoutTimeUnit;
		this.delayStrategy = delayStrategy;
		start();
	}

	/**
	 * Wait object builder.
	 *
	 * @param timeout timeout
	 * @param timeoutTimeUnit timeout time unit
	 * @param interval interval
	 * @param intervalTimeUnit interval time unit
	 * @return the wait object
	 */
	public static WaitTimeout of(final long timeout, final TimeUnit timeoutTimeUnit, final long interval, final TimeUnit intervalTimeUnit) {
		return of(timeout, timeoutTimeUnit, FixedDelayStrategy.of(interval, intervalTimeUnit));
	}

	/**
	 * Wait object builder.
	 *
	 * @param timeout timeout
	 * @param interval interval
	 * @return the wait object
	 */
	public static WaitTimeout of(final Duration timeout, final Duration interval) {
		return of(timeout.toMillis(), TimeUnit.MILLISECONDS, interval.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Wait object builder.
	 *
	 * @param timeout timeout
	 * @param timeoutTimeUnit timeout time unit
	 * @param delayStrategy strategy that determines the sleep interval between checks
	 * @return the wait object
	 */
	public static WaitTimeout of(final long timeout, final TimeUnit timeoutTimeUnit, final DelayStrategy delayStrategy) {
		return new WaitTimeout(timeout, timeoutTimeUnit, delayStrategy);
	}

	/**
	 * @see Wait#interval()
	 */
	@Override
	public long interval() {
		return delayStrategy.delay(attempt.get());
	}

	/**
	 * @see Wait#timeUnit()
	 */
	@Override
	public TimeUnit timeUnit() {
		return delayStrategy.timeUnit();
	}

	/**
	 * Resets the start time.
	 */
	@Override
	public void start() {
		start(Instant.now());
	}

	/**
	 * Sets the start time.
	 *
	 * @param start time to set
	 */
	protected void start(final Instant start) {
		this.attempt.set(1);
		this.start = start;
	}

	/**
	 * @see Wait#now()
	 */
	@Override
	public void now() {
		// don't wait if the timeout is over
		if (!keepWaiting()) {
			return;
		}
		Wait.super.now();
		attempt.updateAndGet(value -> value == Integer.MAX_VALUE ? value : value + 1);
	}

	/**
	 * Returns true if the retry should keep waiting.
	 *
	 * @return true if the retry should keep waiting
	 */
	@Override
	public boolean keepWaiting() {
		return !isOver();
	}

	/**
	 * Returns true if the wait is over. This method can be used to check if the timeout has been reached since the internal
	 * start time.
	 *
	 * @return true if the wait is over
	 */
	public boolean isOver() {
		return isOver(start);
	}

	/**
	 * Returns true if the wait is over. This method can be used to check if the timeout has been reached since the provided
	 * start time.
	 *
	 * @param start start time
	 * @return true if the wait is over
	 */
	public boolean isOver(final Instant start) {
		return Instant.now().isAfter(start.plus(timeout, timeoutTimeUnit.toChronoUnit()));
	}

	/**
	 * Returns true if the wait is over. This method can be used to check if the timeout has been reached since the provided
	 * start time in epoch milliseconds.
	 *
	 * @param startTimeEpochMilli start time in epoch milliseconds
	 * @return true if the wait is over
	 */
	public boolean isOver(final long startTimeEpochMilli) {
		return isOver(Instant.ofEpochMilli(startTimeEpochMilli));
	}

	/**
	 * Returns a copy.
	 *
	 * @return a copy
	 */
	@Override
	public WaitTimeout copy() {
		return WaitTimeout.of(timeout, timeoutTimeUnit, delayStrategy.copy());
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object that) {
		if (this == that) {
			return true;
		}
		if (null == that || that.getClass() != getClass()) {
			return false;
		}
		WaitTimeout thatWait = (WaitTimeout) that;
		return timeout == thatWait.timeout
				&& timeoutTimeUnit == thatWait.timeoutTimeUnit
				&& Objects.equals(delayStrategy, thatWait.delayStrategy)
				&& attempt.get() == thatWait.attempt.get()
				&& Objects.equals(start, thatWait.start);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(timeout, timeoutTimeUnit, delayStrategy, attempt.get(), start);
	}
}
