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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link WaitCounter}.
 *
 * @author Radu Sebastian LAZIN
 */
class WaitCounterTest {

	private static final long MILLIS = 1;
	private static final Duration INTERVAL = Duration.ofMillis(MILLIS);
	private static final int MAX_COUNT = 3;

	@Test
	void shouldReturnTrueOnTwoEqualObjects() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, INTERVAL);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnTheSameObject() {
		WaitCounter waitCounter = WaitCounter.of(MAX_COUNT, INTERVAL);

		boolean result = waitCounter.equals(waitCounter);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnTwoEqualObjectsWhenCountersAdvance() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, INTERVAL);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		waitCounter1.start();
		waitCounter2.start();

		waitCounter1.keepWaiting();
		waitCounter2.keepWaiting();

		boolean result = waitCounter1.equals(waitCounter2);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWhenOneCounterAdvances() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, INTERVAL);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		waitCounter1.start();
		waitCounter1.keepWaiting();

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWithDifferentMaxCounts() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, INTERVAL);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT + 1, MILLIS, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWithDifferentInterval() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT, MILLIS + 1, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsTwoObjectsWithDifferentTimeUnits() {
		WaitCounter waitCounter1 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.HOURS);
		WaitCounter waitCounter2 = WaitCounter.of(MAX_COUNT, MILLIS, TimeUnit.MILLISECONDS);

		boolean result = waitCounter1.equals(waitCounter2);

		assertFalse(result);
	}

	@Test
	void shouldReturnACopy() {
		WaitCounter waitCounter = WaitCounter.of(MAX_COUNT, INTERVAL);

		WaitCounter waitCounterCopy = waitCounter.copy();

		assertNotSame(waitCounter, waitCounterCopy);
		assertEquals(waitCounter, waitCounterCopy);
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsNull() {
		WaitCounter waitCounter = WaitCounter.of(MAX_COUNT, INTERVAL);

		boolean result = waitCounter.equals(null);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsADifferentClass() {
		WaitCounter waitCounter = WaitCounter.of(MAX_COUNT, INTERVAL);

		boolean result = waitCounter.equals(new Object());

		assertFalse(result);
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateWaitCounterDefaultConstructor() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(WaitCounter.Default.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldHaveTheCorrectDefaultValues() {
		assertThat(WaitCounter.Default.MAX_COUNT, equalTo(3));
		assertThat(WaitCounter.Default.SLEEP, equalTo(Duration.ofSeconds(1)));
	}

	@Test
	void shouldInstantiateWaitCounterWithDefaultValues() {
		WaitCounter waitCounter = WaitCounter.DEFAULT;

		assertThat(waitCounter.maxCount(), equalTo(WaitCounter.Default.MAX_COUNT));
		assertThat(waitCounter.interval(), equalTo(WaitCounter.Default.SLEEP.toMillis()));
		assertThat(waitCounter.timeUnit(), equalTo(TimeUnit.MILLISECONDS));
	}
}
