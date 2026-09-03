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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.morphix.utils.Tests;

/**
 * Test class for {@link AsyncWaitTimeout}.
 *
 * @author Radu Sebastian LAZIN
 */
class AsyncWaitTimeoutTest {

	private static final long TIMEOUT_MS = 1;
	private static final long INTERVAL_MS = 1;
	private static final Duration TIMEOUT = Duration.ofMillis(TIMEOUT_MS);
	private static final Duration INTERVAL = Duration.ofMillis(INTERVAL_MS);
	private static final Instant START = Instant.MIN;

	@Test
	void shouldReturnTrueOnTwoEqualObjects() {
		AsyncWaitTimeout waitTimeout1 = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);
		AsyncWaitTimeout waitTimeout2 = AsyncWaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertTrue(result);
		assertThat(waitTimeout1.hashCode(), equalTo(waitTimeout2.hashCode()));
	}

	@Test
	void shouldReturnTrueOnEqualsForCopied() {
		AsyncWaitTimeout waitTimeout1 = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);
		waitTimeout1.start(START);

		AsyncWaitTimeout waitTimeout2 = waitTimeout1.copy();
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertTrue(result);
		assertThat(waitTimeout1.hashCode(), equalTo(waitTimeout2.hashCode()));
	}

	@Test
	void shouldReturnTrueOnEqualsOnTheSameObject() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);

		boolean result = waitTimeout.equals(waitTimeout);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfTimeoutIsDifferent() {
		AsyncWaitTimeout waitTimeout1 = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);
		AsyncWaitTimeout waitTimeout2 = AsyncWaitTimeout.of(TIMEOUT_MS + 1, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfTimeoutUnitIsDifferent() {
		AsyncWaitTimeout waitTimeout1 = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);
		AsyncWaitTimeout waitTimeout2 = AsyncWaitTimeout.of(TIMEOUT_MS, TimeUnit.DAYS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfIntervalIsDifferent() {
		AsyncWaitTimeout waitTimeout1 = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);
		AsyncWaitTimeout waitTimeout2 = AsyncWaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS + 1, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfIntervalUnitIsDifferent() {
		AsyncWaitTimeout waitTimeout1 = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);
		AsyncWaitTimeout waitTimeout2 = AsyncWaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.DAYS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfStartIsDifferent() {
		AsyncWaitTimeout waitTimeout1 = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);
		AsyncWaitTimeout waitTimeout2 = AsyncWaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START.plus(1, ChronoUnit.HOURS));

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsNull() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);

		boolean result = waitTimeout.equals(null);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsADifferentClass() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);

		boolean result = waitTimeout.equals(new Object());

		assertFalse(result);
	}

	@Test
	void shouldSetTheStartTimeOnCallingStart() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);

		waitTimeout.start();
		org.morphix.lang.thread.Threads.safeSleep(Duration.ofMillis(5));

		AsyncWaitTimeout copyWithoutStart = waitTimeout.copy();
		boolean result = waitTimeout.equals(copyWithoutStart);

		assertFalse(result);
	}

	@Test
	void shouldSetTheProvidedStartTimeOnCallingStart() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);

		waitTimeout.start(START);

		AsyncWaitTimeout copyWithStart = waitTimeout.copy();
		copyWithStart.start(START);
		boolean result = waitTimeout.equals(copyWithStart);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnKeepWaitingIfTimeoutReached() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(Duration.ofSeconds(-10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.keepWaiting();

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueOnKeepWaitingIfTimeoutNotReached() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(Duration.ofSeconds(10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.keepWaiting();

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnIsOverWithEpochMillisWhenTimeoutReached() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(Duration.ofSeconds(-10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.isOver(Instant.now().toEpochMilli());

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnIsOverWithEpochMillisWhenTimeoutNotReached() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(Duration.ofSeconds(10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.isOver(Instant.now().toEpochMilli());

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueOnIsOverWhenTimeoutReached() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(Duration.ofSeconds(-10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.isOver(Instant.now());

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnIsOverWhenTimeoutNotReached() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(Duration.ofSeconds(10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.isOver(Instant.now());

		assertFalse(result);
	}

	@Test
	void shouldWaitOnNowAsync() throws Exception {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(Duration.ofMillis(10), Duration.ofMillis(5));
		waitTimeout.start();

		Instant before = Instant.now();
		waitTimeout.defer().get();
		Instant after = Instant.now();

		long elapsedMillis = Duration.between(before, after).toMillis();

		assertThat(elapsedMillis, greaterThanOrEqualTo(5L));
	}

	@Test
	void shouldNotWaitOnNowAsyncIfTimeoutReached() throws Exception {
		long interval = 50;
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.of(Duration.ofSeconds(-10), Duration.ofMillis(interval));
		waitTimeout.start();

		Instant before = Instant.now();
		waitTimeout.defer().get();
		Instant after = Instant.now();

		long elapsedMillis = Duration.between(before, after).toMillis();

		assertThat(elapsedMillis, lessThan(interval / 2));
	}

	@Test
	void shouldIncreaseAttemptAfterEachWait() throws Exception {
		AsyncWaitTimeout waitTimeout = new AsyncWaitTimeout(1, TimeUnit.DAYS, attempt -> attempt, null) {
			@Override
			public Executor executor() {
				return Runnable::run;
			}
		};
		waitTimeout.start();

		waitTimeout.defer().get();
		waitTimeout.defer().get();

		assertThat(waitTimeout.interval(), equalTo(3L));
	}

	@Test
	void shouldIncreaseAttemptAfterEachWaitUsingRecordedIntervals() throws Exception {
		List<Long> recordedIntervals = new ArrayList<>();
		AsyncWaitTimeout waitTimeout = new AsyncWaitTimeout(1, TimeUnit.DAYS, attempt -> attempt, null) {
			@Override
			public Executor executor() {
				return Runnable::run;
			}

			@Override
			public long interval() {
				long interval = super.interval();
				recordedIntervals.add(interval);
				return interval;
			}
		};
		waitTimeout.start();

		waitTimeout.defer().get();
		waitTimeout.defer().get();

		assertThat(recordedIntervals, equalTo(List.of(1L, 2L)));
	}

	@Test
	void shouldNotOverflowAttemptWhenAtIntegerMaxValue() throws Exception {
		AsyncWaitTimeout waitTimeout = new AsyncWaitTimeout(1, TimeUnit.DAYS, attempt -> attempt, null) {
			@Override
			public Executor executor() {
				return Runnable::run;
			}

			@Override
			public long interval() {
				return 0;
			}
		};
		waitTimeout.start();

		AtomicInteger attempt = Fields.IgnoreAccess.get(waitTimeout, "attempt");
		attempt.set(Integer.MAX_VALUE);

		waitTimeout.defer().get();

		assertThat(attempt.get(), equalTo(Integer.MAX_VALUE));
	}

	@Test
	void shouldReturnFalseOnEqualsIfAttemptIsDifferent() throws Exception {
		AsyncWaitTimeout waitTimeout1 = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);
		AsyncWaitTimeout waitTimeout2 = AsyncWaitTimeout.of(TIMEOUT, INTERVAL);

		waitTimeout1.start(START);
		waitTimeout2.start(START);
		AtomicInteger attempt = Fields.IgnoreAccess.get(waitTimeout1, "attempt");
		attempt.set(Integer.MAX_VALUE);

		waitTimeout1.defer().get();

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateWaitTimeoutDefaultConstructor() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(AsyncWaitTimeout.Default.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldHaveTheCorrectDefaultValues() {
		assertThat(AsyncWaitTimeout.Default.SLEEP, Matchers.equalTo(Duration.ofSeconds(1)));
		assertThat(AsyncWaitTimeout.Default.TIMEOUT, Matchers.equalTo(Duration.ofSeconds(30)));
	}

	@Test
	void shouldInstantiateWaitTimeoutWithDefaultValues() {
		AsyncWaitTimeout waitTimeout = AsyncWaitTimeout.DEFAULT;

		assertThat(waitTimeout.interval(), Matchers.equalTo(AsyncWaitTimeout.Default.SLEEP.toMillis()));
		assertThat(waitTimeout.timeUnit(), Matchers.equalTo(TimeUnit.MILLISECONDS));
	}
}
