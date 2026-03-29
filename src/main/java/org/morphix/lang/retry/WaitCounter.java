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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.morphix.reflection.Constructors;

/**
 * Counter wait implementation. After each iteration, the counter waits for the given interval. If the interval is 0,
 * the counter doesn't wait.
 *
 * @author Radu Sebastian LAZIN
 */
public class WaitCounter implements Wait {

	/**
	 * Default values name space.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Default {

		/**
		 * Default maximum count: 3.
		 */
		public static final int MAX_COUNT = 3;

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
	 * Default wait counter: 3 times one second apart.
	 */
	public static final WaitCounter DEFAULT = WaitCounter.of(Default.MAX_COUNT, Default.SLEEP);

	/**
	 * Interval between waits
	 */
	private final long interval;

	/**
	 * Interval time unit
	 */
	private final TimeUnit intervalTimeUnit;

	/**
	 * Maximum times to wait.
	 */
	private final int maxCount;

	/**
	 * Counter.
	 */
	private int count;

	/**
	 * Private constructor.
	 *
	 * @param interval interval
	 * @param intervalTimeUnit interval time unit
	 */
	private WaitCounter(final int maxCount, final long interval, final TimeUnit intervalTimeUnit) {
		this.maxCount = maxCount;
		this.interval = interval;
		this.intervalTimeUnit = intervalTimeUnit;
		this.count = 0;
	}

	/**
	 * Wait object builder.
	 *
	 * @param maxCount maximum number of retries
	 * @param interval interval
	 * @param intervalTimeUnit interval time unit
	 * @return the wait object
	 */
	public static WaitCounter of(final int maxCount, final long interval, final TimeUnit intervalTimeUnit) {
		return new WaitCounter(maxCount, interval, intervalTimeUnit);
	}

	/**
	 * Wait object builder.
	 *
	 * @param maxCount maximum number of retries
	 * @param interval interval
	 * @return the wait object
	 */
	public static WaitCounter of(final int maxCount, final Duration interval) {
		return of(maxCount, interval.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @see Wait#interval()
	 */
	@Override
	public long interval() {
		return interval;
	}

	/**
	 * @see Wait#timeUnit()
	 */
	@Override
	public TimeUnit timeUnit() {
		return intervalTimeUnit;
	}

	/**
	 * Returns the maximum count.
	 *
	 * @return the maximum count
	 */
	public int maxCount() {
		return maxCount;
	}

	/**
	 * Resets the start time.
	 */
	@Override
	public void start() {
		this.count = 0;
	}

	/**
	 * Returns true if the wait should keep waiting.
	 *
	 * @return true if the wait should keep waiting
	 */
	@Override
	public boolean keepWaiting() {
		return !isOver(++count);
	}

	/**
	 * Returns true if the wait is over.
	 *
	 * @param count current count
	 * @return true, if the wait is over
	 */
	public boolean isOver(final int count) {
		return count >= maxCount;
	}

	/**
	 * Returns a copy.
	 *
	 * @return a copy
	 */
	@Override
	public WaitCounter copy() {
		return WaitCounter.of(maxCount, interval, intervalTimeUnit);
	}

	/**
	 * Equals method that also verifies that objects are of the same class.
	 */
	@Override
	public boolean equals(final Object that) {
		if (this == that) {
			return true;
		}
		if (null == that || that.getClass() != getClass()) {
			return false;
		}
		WaitCounter thatWait = (WaitCounter) that;
		return Objects.equals(interval, thatWait.interval)
				&& Objects.equals(intervalTimeUnit, thatWait.intervalTimeUnit)
				&& Objects.equals(maxCount, thatWait.maxCount)
				&& Objects.equals(count, thatWait.count);
	}

	/**
	 * Hash code implementation.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(interval, intervalTimeUnit, maxCount, count);
	}
}
