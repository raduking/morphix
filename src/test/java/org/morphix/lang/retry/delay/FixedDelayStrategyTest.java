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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link FixedDelayStrategy}.
 *
 * @author Radu Sebastian LAZIN
 */
class FixedDelayStrategyTest {

	private static final long DELAY = 100;
	private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

	@Test
	void shouldBuildUsingFactoryMethod() {
		FixedDelayStrategy strategy = FixedDelayStrategy.of(DELAY, TIME_UNIT);

		assertThat(strategy.delay(1), equalTo(DELAY));
		assertThat(strategy.timeUnit(), equalTo(TIME_UNIT));
	}

	@Test
	void shouldReturnTheConfiguredFixedDelayForAnyPositiveAttempt() {
		FixedDelayStrategy strategy = new FixedDelayStrategy(DELAY, TIME_UNIT);

		assertThat(strategy.delay(1), equalTo(DELAY));
		assertThat(strategy.delay(10), equalTo(DELAY));
	}

	@Test
	void shouldThrowExceptionForAttemptZero() {
		FixedDelayStrategy strategy = new FixedDelayStrategy(DELAY, TIME_UNIT);

		assertThrows(IllegalArgumentException.class, () -> strategy.delay(0));
	}

	@Test
	void shouldThrowExceptionForNegativeAttempt() {
		FixedDelayStrategy strategy = new FixedDelayStrategy(DELAY, TIME_UNIT);

		assertThrows(IllegalArgumentException.class, () -> strategy.delay(-1));
	}

	@Test
	void shouldReturnTrueOnEqualsForTheSameInstance() {
		FixedDelayStrategy strategy = new FixedDelayStrategy(DELAY, TIME_UNIT);

		assertEquals(strategy, strategy);
	}

	@Test
	void shouldReturnTrueOnEqualsForEqualObjects() {
		FixedDelayStrategy strategy1 = new FixedDelayStrategy(DELAY, TIME_UNIT);
		FixedDelayStrategy strategy2 = new FixedDelayStrategy(DELAY, TIME_UNIT);

		boolean result = strategy1.equals(strategy2);

		assertTrue(result);
		assertThat(strategy1.hashCode(), equalTo(strategy2.hashCode()));
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsNull() {
		FixedDelayStrategy strategy = new FixedDelayStrategy(DELAY, TIME_UNIT);

		boolean result = strategy.equals(null);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfOtherIsADifferentClass() {
		FixedDelayStrategy strategy = new FixedDelayStrategy(DELAY, TIME_UNIT);

		boolean result = strategy.equals(new Object());

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfDelayIsDifferent() {
		FixedDelayStrategy strategy1 = new FixedDelayStrategy(DELAY, TIME_UNIT);
		FixedDelayStrategy strategy2 = new FixedDelayStrategy(DELAY + 1, TIME_UNIT);

		boolean result = strategy1.equals(strategy2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsIfTimeUnitIsDifferent() {
		FixedDelayStrategy strategy1 = new FixedDelayStrategy(DELAY, TIME_UNIT);
		FixedDelayStrategy strategy2 = new FixedDelayStrategy(DELAY, TimeUnit.SECONDS);

		boolean result = strategy1.equals(strategy2);

		assertFalse(result);
	}

	@Test
	void shouldBuildHashCodeBasedOnFields() {
		FixedDelayStrategy strategy = new FixedDelayStrategy(DELAY, TIME_UNIT);

		int expected = Objects.hash(DELAY, TIME_UNIT);
		int result = strategy.hashCode();

		assertThat(result, equalTo(expected));
	}
}
