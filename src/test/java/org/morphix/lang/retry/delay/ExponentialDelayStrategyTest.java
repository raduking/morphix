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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ExponentialDelayStrategy}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExponentialDelayStrategyTest {

	private static final long MIN_DELAY = 100;
	private static final long MAX_DELAY = 1000;
	private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
	private static final double MULTIPLIER = 2d;

	@Test
	void shouldReturnConfiguredTimeUnit() {
		ExponentialDelayStrategy strategy = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);

		assertThat(strategy.timeUnit(), equalTo(TIME_UNIT));
	}

	@Test
	void shouldComputeExponentialDelayForAttempts() {
		ExponentialDelayStrategy strategy = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);

		assertThat(strategy.delay(1), equalTo(100L));
		assertThat(strategy.delay(2), equalTo(200L));
		assertThat(strategy.delay(3), equalTo(400L));
	}

	@Test
	void shouldCapDelayToMaximumValue() {
		ExponentialDelayStrategy strategy = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);

		long result = strategy.delay(10);

		assertThat(result, equalTo(MAX_DELAY));
	}

	@Test
	void shouldThrowExceptionForAttemptZero() {
		ExponentialDelayStrategy strategy = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);

		assertThrows(IllegalArgumentException.class, () -> strategy.delay(0));
	}

	@Test
	void shouldThrowExceptionForNegativeAttempt() {
		ExponentialDelayStrategy strategy = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);

		assertThrows(IllegalArgumentException.class, () -> strategy.delay(-1));
	}

	@Test
	void shouldThrowExceptionWhenMaximumDelayIsLessThanMinimumDelay() {
		assertThrows(IllegalArgumentException.class,
				() -> new ExponentialDelayStrategy(MIN_DELAY, MIN_DELAY - 1, TIME_UNIT, MULTIPLIER));
	}

	@Test
	void shouldThrowExceptionWhenMultiplierIsLessThanOrEqualToOne() {
		assertThrows(IllegalArgumentException.class,
				() -> new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, 1d));
		assertThrows(IllegalArgumentException.class,
				() -> new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, 0.5d));
	}

	@Test
	void shouldThrowExceptionWhenTimeUnitIsNull() {
		assertThrows(NullPointerException.class,
				() -> new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, null, MULTIPLIER));
	}

	@Test
	void shouldReturnTrueOnEqualsForTheSameInstance() {
		ExponentialDelayStrategy strategy = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);

		assertEquals(strategy, strategy);
	}

	@Test
	void shouldReturnTrueOnEqualsForEqualObjects() {
		ExponentialDelayStrategy strategy1 = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);
		ExponentialDelayStrategy strategy2 = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);

		boolean result = strategy1.equals(strategy2);

		assertTrue(result);
		assertThat(strategy1.hashCode(), equalTo(strategy2.hashCode()));
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsNull() {
		ExponentialDelayStrategy strategy = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);

		assertNotEquals(null, strategy);
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsADifferentClass() {
		ExponentialDelayStrategy strategy = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);

		assertNotEquals(strategy, new Object());
	}

	@Test
	void shouldReturnFalseOnEqualsIfMinimumDelayIsDifferent() {
		ExponentialDelayStrategy strategy1 = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);
		ExponentialDelayStrategy strategy2 = new ExponentialDelayStrategy(MIN_DELAY + 1, MAX_DELAY, TIME_UNIT, MULTIPLIER);

		assertNotEquals(strategy1, strategy2);
	}

	@Test
	void shouldReturnFalseOnEqualsIfMaximumDelayIsDifferent() {
		ExponentialDelayStrategy strategy1 = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);
		ExponentialDelayStrategy strategy2 = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY + 1, TIME_UNIT, MULTIPLIER);

		assertNotEquals(strategy1, strategy2);
	}

	@Test
	void shouldReturnFalseOnEqualsIfTimeUnitIsDifferent() {
		ExponentialDelayStrategy strategy1 = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);
		ExponentialDelayStrategy strategy2 = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TimeUnit.SECONDS, MULTIPLIER);

		assertNotEquals(strategy1, strategy2);
	}

	@Test
	void shouldReturnFalseOnEqualsIfMultiplierIsDifferent() {
		ExponentialDelayStrategy strategy1 = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);
		ExponentialDelayStrategy strategy2 = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER + 1d);

		assertNotEquals(strategy1, strategy2);
	}

	@Test
	void shouldBuildHashCodeBasedOnFields() {
		ExponentialDelayStrategy strategy = new ExponentialDelayStrategy(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);

		int expected = Objects.hash(MIN_DELAY, MAX_DELAY, TIME_UNIT, MULTIPLIER);
		int result = strategy.hashCode();

		assertThat(result, equalTo(expected));
	}
}
