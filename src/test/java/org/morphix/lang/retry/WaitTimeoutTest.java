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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.morphix.lang.thread.Threads;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link WaitTimeout}.
 *
 * @author Radu Sebastian LAZIN
 */
class WaitTimeoutTest {

	private static final long TIMEOUT_MS = 1;
	private static final long INTERVAL_MS = 1;
	private static final Duration TIMEOUT = Duration.ofMillis(TIMEOUT_MS);
	private static final Duration INTERVAL = Duration.ofMillis(INTERVAL_MS);
	private static final Instant START = Instant.MIN;

	@Test
	void shouldReturnTrueOnTwoEqualObjects() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertTrue(result);
		assertThat(waitTimeout1.hashCode(), equalTo(waitTimeout2.hashCode()));
	}

	@Test
	void shouldReturnTrueOnEqualsForCopied() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		waitTimeout1.start(START);

		WaitTimeout waitTimeout2 = waitTimeout1.copy();
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertTrue(result);
		assertThat(waitTimeout1.hashCode(), equalTo(waitTimeout2.hashCode()));
	}

	@Test
	void shouldReturnTrueOnEqualsOnTheSameObject() {
		WaitTimeout waitTimeout = WaitTimeout.of(TIMEOUT, INTERVAL);

		boolean result = waitTimeout.equals(waitTimeout);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfTimeoutIsDifferent() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS + 1, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfTimeoutUnitIsDifferent() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS, TimeUnit.DAYS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfIntervalIsDifferent() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS + 1, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfIntervalUnitIsDifferent() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.DAYS);

		waitTimeout1.start(START);
		waitTimeout2.start(START);

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfStartIsDifferent() {
		WaitTimeout waitTimeout1 = WaitTimeout.of(TIMEOUT, INTERVAL);
		WaitTimeout waitTimeout2 = WaitTimeout.of(TIMEOUT_MS, TimeUnit.MILLISECONDS, INTERVAL_MS, TimeUnit.MILLISECONDS);

		waitTimeout1.start(START);
		waitTimeout2.start(START.plus(1, ChronoUnit.HOURS));

		boolean result = waitTimeout1.equals(waitTimeout2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsNull() {
		WaitTimeout waitTimeout = WaitTimeout.of(TIMEOUT, INTERVAL);

		boolean result = waitTimeout.equals(null);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsADifferentClass() {
		WaitTimeout waitTimeout = WaitTimeout.of(TIMEOUT, INTERVAL);

		boolean result = waitTimeout.equals(new Object());

		assertFalse(result);
	}

	@Test
	void shouldSetTheStartTimeOnCallingStart() {
		WaitTimeout waitTimeout = WaitTimeout.of(TIMEOUT, INTERVAL);

		waitTimeout.start();
		Threads.safeSleep(Duration.ofMillis(5));

		WaitTimeout copyWithoutStart = waitTimeout.copy();
		boolean result = waitTimeout.equals(copyWithoutStart);

		assertFalse(result);
	}

	@Test
	void shouldSetTheProvidedStartTimeOnCallingStart() {
		WaitTimeout waitTimeout = WaitTimeout.of(TIMEOUT, INTERVAL);

		waitTimeout.start(START);

		WaitTimeout copyWithStart = waitTimeout.copy();
		copyWithStart.start(START);
		boolean result = waitTimeout.equals(copyWithStart);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnKeepWaitingIfTimeoutReached() {
		WaitTimeout waitTimeout = WaitTimeout.of(Duration.ofSeconds(-10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.keepWaiting();

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueOnKeepWaitingIfTimeoutNotReached() {
		WaitTimeout waitTimeout = WaitTimeout.of(Duration.ofSeconds(10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.keepWaiting();

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnIsOverWithEpochMillisWhenTimeoutReached() {
		WaitTimeout waitTimeout = WaitTimeout.of(Duration.ofSeconds(-10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.isOver(Instant.now().toEpochMilli());

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnIsOverWithEpochMillisWhenTimeoutNotReached() {
		WaitTimeout waitTimeout = WaitTimeout.of(Duration.ofSeconds(10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.isOver(Instant.now().toEpochMilli());

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueOnIsOverWhenTimeoutReached() {
		WaitTimeout waitTimeout = WaitTimeout.of(Duration.ofSeconds(-10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.isOver(Instant.now());

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnIsOverWhenTimeoutNotReached() {
		WaitTimeout waitTimeout = WaitTimeout.of(Duration.ofSeconds(10), INTERVAL);
		waitTimeout.start();

		boolean result = waitTimeout.isOver(Instant.now());

		assertFalse(result);
	}

	@Test
	void shouldWaitOnNow() {
		WaitTimeout waitTimeout = WaitTimeout.of(Duration.ofMillis(10), Duration.ofMillis(5));
		waitTimeout.start();

		Instant before = Instant.now();
		waitTimeout.now();
		Instant after = Instant.now();

		long elapsedMillis = Duration.between(before, after).toMillis();
		assertThat(elapsedMillis, greaterThanOrEqualTo(5L));
	}

	@Test
	void shouldNotWaitOnNowIfTimeoutReached() {
		AtomicBoolean sleepCalls = new AtomicBoolean(false);
		BiConsumer<Long, TimeUnit> sleepAction = (interval, timeUnit) -> sleepCalls.set(true);
		WaitTimeout waitTimeout = new WaitTimeout(-10, TimeUnit.SECONDS, -10, TimeUnit.SECONDS) {
			@Override
			public BiConsumer<Long, TimeUnit> sleepAction() {
				return sleepAction;
			}
		};
		waitTimeout.start();

		waitTimeout.now();

		assertThat(sleepCalls.get(), equalTo(false));
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateWaitTimeoutDefaultConstructor() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(WaitTimeout.Default.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldHaveTheCorrectDefaultValues() {
		assertThat(WaitTimeout.Default.SLEEP, Matchers.equalTo(Duration.ofSeconds(1)));
		assertThat(WaitTimeout.Default.TIMEOUT, Matchers.equalTo(Duration.ofSeconds(30)));
	}

	@Test
	void shouldInstantiateWaitCounterWithDefaultValues() {
		WaitTimeout waitTimeout = WaitTimeout.DEFAULT;

		assertThat(waitTimeout.interval(), Matchers.equalTo(WaitTimeout.Default.SLEEP.toMillis()));
		assertThat(waitTimeout.timeUnit(), Matchers.equalTo(TimeUnit.MILLISECONDS));
	}
}
