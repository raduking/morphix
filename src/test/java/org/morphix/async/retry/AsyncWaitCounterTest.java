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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.morphix.lang.retry.DelayStrategy;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link AsyncWaitCounter}.
 *
 * @author Radu Sebastian LAZIN
 */
class AsyncWaitCounterTest {

	private static final long MILLIS = 1;
	private static final Duration INTERVAL = Duration.ofMillis(MILLIS);
	private static final int MAX_COUNT = 3;

	@Test
	void shouldCreateWaitCounterUsingDelayStrategy() {
		DelayStrategy delayStrategy = attempt -> attempt;
		AsyncWaitCounter waitCounter = AsyncWaitCounter.of(MAX_COUNT, delayStrategy);

		assertThat(waitCounter.interval(), equalTo(1L));
		waitCounter.keepWaiting();
		assertThat(waitCounter.interval(), equalTo(2L));
		assertThat(waitCounter.timeUnit(), equalTo(TimeUnit.MILLISECONDS));
	}

	@Test
	void shouldReturnTrueOnTwoEqualObjects() {
		AsyncWaitCounter waitCounter1 = AsyncWaitCounter.of(MAX_COUNT, INTERVAL);
		AsyncWaitCounter waitCounter2 = AsyncWaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnTheSameObject() {
		AsyncWaitCounter waitCounter = AsyncWaitCounter.of(MAX_COUNT, INTERVAL);

		boolean result = waitCounter.equals(waitCounter);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnTwoEqualObjectsWhenCountersAdvance() {
		AsyncWaitCounter waitCounter1 = AsyncWaitCounter.of(MAX_COUNT, INTERVAL);
		AsyncWaitCounter waitCounter2 = AsyncWaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		waitCounter1.start();
		waitCounter2.start();

		waitCounter1.keepWaiting();
		waitCounter2.keepWaiting();

		boolean result = waitCounter1.equals(waitCounter2);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWhenOneCounterAdvances() {
		AsyncWaitCounter waitCounter1 = AsyncWaitCounter.of(MAX_COUNT, INTERVAL);
		AsyncWaitCounter waitCounter2 = AsyncWaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		waitCounter1.start();
		waitCounter1.keepWaiting();

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWithDifferentMaxCounts() {
		AsyncWaitCounter waitCounter1 = AsyncWaitCounter.of(MAX_COUNT, INTERVAL);
		AsyncWaitCounter waitCounter2 = AsyncWaitCounter.of(MAX_COUNT + 1, MILLIS, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWithDifferentInterval() {
		AsyncWaitCounter waitCounter1 = AsyncWaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);
		AsyncWaitCounter waitCounter2 = AsyncWaitCounter.of(MAX_COUNT, MILLIS + 1, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWithDifferentTimeUnits() {
		AsyncWaitCounter waitCounter1 = AsyncWaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.HOURS);
		AsyncWaitCounter waitCounter2 = AsyncWaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnACopy() {
		AsyncWaitCounter waitCounter = AsyncWaitCounter.of(MAX_COUNT, INTERVAL);

		AsyncWaitCounter waitCounterCopy = waitCounter.copy();

		assertNotSame(waitCounter, waitCounterCopy);
		assertEquals(waitCounter, waitCounterCopy);
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsNull() {
		AsyncWaitCounter waitCounter = AsyncWaitCounter.of(MAX_COUNT, INTERVAL);

		boolean result = waitCounter.equals(null);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsADifferentClass() {
		AsyncWaitCounter waitCounter = AsyncWaitCounter.of(MAX_COUNT, INTERVAL);

		boolean result = waitCounter.equals(new Object());

		assertFalse(result);
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateWaitCounterDefaultConstructor() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(AsyncWaitCounter.Default.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldHaveTheCorrectDefaultValues() {
		assertThat(AsyncWaitCounter.Default.MAX_COUNT, equalTo(3));
		assertThat(AsyncWaitCounter.Default.SLEEP, equalTo(Duration.ofSeconds(1)));
	}

	@Test
	void shouldInstantiateWaitCounterWithDefaultValues() {
		AsyncWaitCounter waitCounter = AsyncWaitCounter.DEFAULT;

		assertThat(waitCounter.maxCount(), equalTo(AsyncWaitCounter.Default.MAX_COUNT));
		assertThat(waitCounter.interval(), equalTo(AsyncWaitCounter.Default.SLEEP.toMillis()));
		assertThat(waitCounter.timeUnit(), equalTo(TimeUnit.MILLISECONDS));
	}

	@Test
	void shouldRunUntimeoutAfterWait() throws Exception {
		AsyncWaitCounter waitCounter = AsyncWaitCounter.of(MAX_COUNT, Duration.ZERO);

		waitCounter.defer().get();

		assertTrue(true);
	}

	@Test
	void shouldStopWaitingAfterMaxCount() {
		AsyncWaitCounter waitCounter = AsyncWaitCounter.of(MAX_COUNT, Duration.ZERO);

		waitCounter.start();
		assertTrue(waitCounter.keepWaiting());
		assertTrue(waitCounter.keepWaiting());
		assertFalse(waitCounter.keepWaiting());
	}
}
