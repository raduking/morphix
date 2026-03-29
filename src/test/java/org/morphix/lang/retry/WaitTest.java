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
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link Wait}.
 *
 * @author Radu Sebastian LAZIN
 */
class WaitTest {

	@Test
	void shouldHaveFunctionalInterfaceAnnotation() {
		assertTrue(Wait.class.isAnnotationPresent(FunctionalInterface.class));
	}

	static class TestWait implements Wait {

		static final BiConsumer<Long, TimeUnit> CUSTOM_SLEEP_ACTION = (interval, timeUnit) -> {
			// No operation
		};

		@Override
		public boolean keepWaiting() {
			return false;
		}

		@Override
		public BiConsumer<Long, TimeUnit> sleepAction() {
			return CUSTOM_SLEEP_ACTION;
		}
	}

	@Test
	void shouldReturnDefaultSleepActionIntervalAndTimeUnit() {
		TestWait wait = new TestWait();

		assertThat(wait.interval(), equalTo(Wait.Default.INTERVAL));
		assertThat(wait.timeUnit(), equalTo(Wait.Default.TIME_UNIT));
		assertThat(wait.sleepAction(), not(equalTo(Wait.Default.SLEEP_ACTION)));
	}

	@Test
	void shouldHaveNoSideEffectsOnCallingDefaultMethods() {
		TestWait wait = new TestWait();
		wait.start();
		wait.now();
		wait.copy();
		wait.sleepAction();
		wait.interval();
		wait.timeUnit();

		assertFalse(wait.keepWaiting());

		assertThat(wait.interval(), equalTo(Wait.Default.INTERVAL));
		assertThat(wait.timeUnit(), equalTo(Wait.Default.TIME_UNIT));
		assertThat(wait.sleepAction(), equalTo(TestWait.CUSTOM_SLEEP_ACTION));
	}

	@Test
	void shouldReturnTheSameInstanceOnCopy() {
		TestWait wait = new TestWait();

		Wait copy = wait.copy();

		assertSame(wait, copy);
	}

	@Test
	void shouldThrowExceptionOnCallingWaitDefaultConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(Wait.Default.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
