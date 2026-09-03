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
package org.morphix.async.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.retry.DelayStrategy;
import org.morphix.lang.retry.delay.FixedDelayStrategy;
import org.morphix.reflection.Constructors;

/**
 * Counter wait implementation. After each iteration, the counter waits for the given interval. If the interval is 0,
 * the counter doesn't wait.
 *
 * @author Radu Sebastian LAZIN
 */
public class AsyncWaitCounter implements AsyncWait {

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
	 * Default async wait counter: 3 times one second apart.
	 */
	public static final AsyncWaitCounter DEFAULT = AsyncWaitCounter.of(Default.MAX_COUNT, Default.SLEEP);

	/**
	 * The delay strategy used to compute the interval between retries.
	 */
	private final DelayStrategy delayStrategy;

	/**
	 * Maximum times to wait.
	 */
	private final int maxCount;

	/**
	 * Counter.
	 */
	private int count;

	/**
	 * Executor used to schedule delays.
	 */
	private final Executor executor;

	/**
	 * Private constructor.
	 *
	 * @param maxCount maximum number of retries
	 * @param delayStrategy delay strategy
	 * @param executor executor used to schedule delays
	 */
	private AsyncWaitCounter(final int maxCount, final DelayStrategy delayStrategy, final Executor executor) {
		this.maxCount = maxCount;
		this.count = 0;
		this.delayStrategy = delayStrategy;
		this.executor = executor;
	}

	/**
	 * Wait object builder.
	 *
	 * @param maxCount maximum number of retries
	 * @param delayStrategy delay strategy
	 * @return the wait object
	 */
	public static AsyncWaitCounter of(final int maxCount, final DelayStrategy delayStrategy) {
		return new AsyncWaitCounter(maxCount, delayStrategy, null);
	}

	/**
	 * Wait object builder.
	 *
	 * @param maxCount maximum number of retries
	 * @param interval interval
	 * @param intervalTimeUnit interval time unit
	 * @return the wait object
	 */
	public static AsyncWaitCounter of(final int maxCount, final long interval, final TimeUnit intervalTimeUnit) {
		return of(maxCount, FixedDelayStrategy.of(interval, intervalTimeUnit));
	}

	/**
	 * Wait object builder.
	 *
	 * @param maxCount maximum number of retries
	 * @param interval interval
	 * @return the wait object
	 */
	public static AsyncWaitCounter of(final int maxCount, final Duration interval) {
		return of(maxCount, interval.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Wait object builder with custom executor.
	 *
	 * @param maxCount maximum number of retries
	 * @param delayStrategy delay strategy
	 * @param executor executor used to schedule delays
	 * @return the wait object
	 */
	public static AsyncWaitCounter of(final int maxCount, final DelayStrategy delayStrategy, final Executor executor) {
		return new AsyncWaitCounter(maxCount, delayStrategy, executor);
	}

	/**
	 * @see AsyncWait#interval()
	 */
	@Override
	public long interval() {
		return delayStrategy.delay(count + 1);
	}

	/**
	 * @see AsyncWait#timeUnit()
	 */
	@Override
	public TimeUnit timeUnit() {
		return delayStrategy.timeUnit();
	}

	/**
	 * @see AsyncWait#executor()
	 */
	@Override
	public Executor executor() {
		return null != executor ? executor : AsyncWait.super.executor();
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
	 * Resets the counter.
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
	public AsyncWaitCounter copy() {
		return AsyncWaitCounter.of(maxCount, delayStrategy.copy(), executor);
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
		AsyncWaitCounter thatWait = (AsyncWaitCounter) that;
		return Objects.equals(delayStrategy, thatWait.delayStrategy)
				&& maxCount == thatWait.maxCount
				&& count == thatWait.count
				&& Objects.equals(executor, thatWait.executor);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(delayStrategy, maxCount, count, executor);
	}
}
