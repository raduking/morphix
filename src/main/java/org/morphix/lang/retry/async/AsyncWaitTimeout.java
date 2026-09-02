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
package org.morphix.lang.retry.async;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.morphix.lang.retry.DelayStrategy;
import org.morphix.lang.retry.delay.FixedDelayStrategy;
import org.morphix.reflection.Constructors;

/**
 * Async timeout wait implementation.
 *
 * @author Radu Sebastian LAZIN
 */
public class AsyncWaitTimeout implements AsyncWait {

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
	 * Default AsyncWaitTimeout object.
	 */
	public static final AsyncWaitTimeout DEFAULT = AsyncWaitTimeout.of(Default.TIMEOUT, Default.SLEEP);

	/**
	 * Timeout value.
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
	 * Executor used to schedule delays.
	 */
	private final Executor executor;

	/**
	 * Private constructor.
	 *
	 * @param timeout timeout
	 * @param timeoutTimeUnit timeout time unit
	 * @param delayStrategy strategy that determines the sleep interval between checks
	 * @param executor executor used to schedule delays
	 */
	protected AsyncWaitTimeout(final long timeout, final TimeUnit timeoutTimeUnit, final DelayStrategy delayStrategy, final Executor executor) {
		this.timeout = timeout;
		this.timeoutTimeUnit = timeoutTimeUnit;
		this.delayStrategy = delayStrategy;
		this.executor = executor;
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
	public static AsyncWaitTimeout of(final long timeout, final TimeUnit timeoutTimeUnit, final long interval,
			final TimeUnit intervalTimeUnit) {
		return of(timeout, timeoutTimeUnit, FixedDelayStrategy.of(interval, intervalTimeUnit));
	}

	/**
	 * Wait object builder.
	 *
	 * @param timeout timeout
	 * @param interval interval
	 * @return the wait object
	 */
	public static AsyncWaitTimeout of(final Duration timeout, final Duration interval) {
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
	public static AsyncWaitTimeout of(final long timeout, final TimeUnit timeoutTimeUnit, final DelayStrategy delayStrategy) {
		return new AsyncWaitTimeout(timeout, timeoutTimeUnit, delayStrategy, null);
	}

	/**
	 * Wait object builder with custom executor.
	 *
	 * @param timeout timeout
	 * @param timeoutTimeUnit timeout time unit
	 * @param delayStrategy strategy that determines the sleep interval between checks
	 * @param executor executor used to schedule delays
	 * @return the wait object
	 */
	public static AsyncWaitTimeout of(final long timeout, final TimeUnit timeoutTimeUnit, final DelayStrategy delayStrategy,
			final Executor executor) {
		return new AsyncWaitTimeout(timeout, timeoutTimeUnit, delayStrategy, executor);
	}

	/**
	 * @see AsyncWait#interval()
	 */
	@Override
	public long interval() {
		return delayStrategy.delay(attempt.get());
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
	 * Defers the wait asynchronously, returning a {@link CompletableFuture} that completes after the delay, incrementing
	 * the attempt counter once the wait is over.
	 *
	 * @return a future that completes after the configured delay
	 */
	@Override
	public CompletableFuture<Void> defer() {
		if (!keepWaiting()) {
			return AsyncWait.super.defer();
		}
		CompletableFuture<Void> future = AsyncWait.super.defer();
		return future.whenComplete((v, e) -> attempt.updateAndGet(value -> value == Integer.MAX_VALUE ? value : value + 1));
	}

	/**
	 * Returns a copy.
	 *
	 * @return a copy
	 */
	@Override
	public AsyncWaitTimeout copy() {
		return AsyncWaitTimeout.of(timeout, timeoutTimeUnit, delayStrategy.copy(), executor);
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
		AsyncWaitTimeout thatWait = (AsyncWaitTimeout) that;
		return timeout == thatWait.timeout
				&& timeoutTimeUnit == thatWait.timeoutTimeUnit
				&& Objects.equals(delayStrategy, thatWait.delayStrategy)
				&& attempt.get() == thatWait.attempt.get()
				&& Objects.equals(start, thatWait.start)
				&& Objects.equals(executor, thatWait.executor);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(timeout, timeoutTimeUnit, delayStrategy, attempt.get(), start, executor);
	}
}
