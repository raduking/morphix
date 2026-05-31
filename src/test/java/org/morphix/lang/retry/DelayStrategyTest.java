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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link DelayStrategy}.
 *
 * @author Radu Sebastian LAZIN
 */
class DelayStrategyTest {

	@Test
	void shouldHaveFunctionalInterfaceAnnotation() {
		assertTrue(DelayStrategy.class.isAnnotationPresent(FunctionalInterface.class));
	}

	@Test
	void shouldReturnDefaultTimeUnit() {
		DelayStrategy strategy = attempt -> 10;

		TimeUnit result = strategy.timeUnit();

		assertThat(result, equalTo(DelayStrategy.Default.TIME_UNIT));
	}

	@Test
	void shouldReturnChronoUnitBasedOnStrategyTimeUnit() {
		DelayStrategy strategy = new DelayStrategy() {
			@Override
			public long delay(final int attempt) {
				return 10;
			}

			@Override
			public TimeUnit timeUnit() {
				return TimeUnit.SECONDS;
			}
		};

		ChronoUnit result = strategy.chronoUnit();

		assertThat(result, equalTo(ChronoUnit.SECONDS));
	}

	@Test
	void shouldReturnDelayForFirstAttemptWhenCallingDelayWithoutArguments() {
		DelayStrategy strategy = attempt -> attempt * 10;

		long result = strategy.delay();

		assertThat(result, equalTo(10L));
	}

	@Test
	void shouldConvertDelayForFirstAttemptToProvidedTimeUnit() {
		DelayStrategy strategy = attempt -> 1000;

		long result = strategy.delay(TimeUnit.SECONDS);

		assertThat(result, equalTo(1L));
	}

	@Test
	void shouldConvertDelayForProvidedAttemptToProvidedTimeUnit() {
		DelayStrategy strategy = attempt -> attempt * 1000;

		long result = strategy.delay(3, TimeUnit.SECONDS);

		assertThat(result, equalTo(3L));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenConvertingFirstAttemptDelayWithNullTimeUnit() {
		DelayStrategy strategy = attempt -> 1000;

		assertThrows(NullPointerException.class, () -> strategy.delay((TimeUnit) null));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenConvertingDelayWithNullTimeUnit() {
		DelayStrategy strategy = attempt -> 1000;

		assertThrows(NullPointerException.class, () -> strategy.delay(1, null));
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateDelayStrategyDefaultConstructor() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(DelayStrategy.Default.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldHaveTheCorrectDefaultValues() {
		assertThat(DelayStrategy.Default.TIME_UNIT, equalTo(TimeUnit.MILLISECONDS));
	}
}
