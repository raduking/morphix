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
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link AsyncWait}.
 *
 * @author Radu Sebastian LAZIN
 */
class AsyncWaitTest {

	private static final long TIMEOUT_SECONDS = 1;

	static class TestAsyncWait implements AsyncWait {

		@Override
		public boolean keepWaiting() {
			return false;
		}

		@Override
		public long interval() {
			return 0;
		}

		@Override
		public TimeUnit timeUnit() {
			return TimeUnit.MILLISECONDS;
		}
	}

	@Test
	void shouldHaveFunctionalInterfaceAnnotation() {
		assertTrue(AsyncWait.class.isAnnotationPresent(FunctionalInterface.class));
	}

	@Test
	void shouldThrowExceptionOnCallingWaitDefaultConstructor() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(AsyncWait.Default.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldUseDefaultValuesAndKeepCopyAndStartAsNoOps() {
		AsyncWait wait = () -> false;

		wait.start();

		assertThat(wait.interval(), equalTo(AsyncWait.Default.INTERVAL));
		assertThat(wait.timeUnit(), equalTo(AsyncWait.Default.TIME_UNIT));
		assertThat(AsyncWait.Default.POLL_INTERVAL, equalTo(Duration.ofMillis(50)));
		assertNotNull(wait.executor());
		assertSame(wait, wait.copy());
		assertFalse(wait.keepWaiting());
	}

	@Test
	void shouldCompleteDeferWithoutBlocking() throws Exception {
		TestAsyncWait wait = new TestAsyncWait();

		assertThat(wait.defer().get(TIMEOUT_SECONDS, TimeUnit.SECONDS), equalTo(null));
	}

	@Test
	void shouldCompleteDeferExceptionallyWhenExecutorIsNull() {
		AsyncWait wait = new TestAsyncWait() {

			@Override
			public Executor executor() {
				return null;
			}
		};

		ExecutionException e = assertThrows(ExecutionException.class,
				() -> wait.defer().get(TIMEOUT_SECONDS, TimeUnit.SECONDS));

		assertThat(e.getCause(), instanceOf(NullPointerException.class));
	}

	@Nested
	class UntilTest {

		@Test
		void shouldReturnImmediatelyWhenConditionIsAlreadyTrue() throws Exception {
			boolean result = AsyncWait.until(() -> true).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

			assertTrue(result);
		}

		@Test
		void shouldReturnFalseImmediatelyWhenTimeoutIsNegative() throws Exception {
			boolean result = AsyncWait.until(() -> false, Duration.ofSeconds(-1)).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

			assertFalse(result);
		}

		@Test
		void shouldReturnTrueWhenConditionBecomesTrueBeforeTimeout() throws Exception {
			AtomicInteger checks = new AtomicInteger();

			boolean result = AsyncWait.until(() -> checks.incrementAndGet() >= 2, Duration.ofSeconds(TIMEOUT_SECONDS), Duration.ofMillis(10))
					.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

			assertTrue(result);
		}

		@Test
		void shouldReturnFalseWhenTimeoutIsReached() throws Exception {
			boolean result = AsyncWait.until(() -> false, Duration.ofMillis(20), Duration.ofMillis(5))
					.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

			assertFalse(result);
		}

		@Test
		void shouldReturnFalseWhenCurrentThreadIsInterrupted() throws Exception {
			Thread.currentThread().interrupt();
			try {
				boolean result = AsyncWait.until(() -> false, Duration.ofSeconds(TIMEOUT_SECONDS), Duration.ofMillis(10))
						.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

				assertFalse(result);
			} finally {
				Thread.interrupted();
			}
		}

		@Test
		void shouldKeepPollingWhenTimeoutIsZeroUntilConditionIsTrue() throws Exception {
			AtomicInteger checks = new AtomicInteger();

			boolean result = AsyncWait.until(() -> checks.incrementAndGet() >= 3, Duration.ZERO, Duration.ofMillis(5))
					.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

			assertTrue(result);
		}

		@Test
		void shouldCompleteExceptionallyWhenPollIntervalIsNull() {
			ExecutionException e = assertThrows(ExecutionException.class,
					() -> AsyncWait.until(() -> false, Duration.ofSeconds(TIMEOUT_SECONDS), null)
							.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));

			assertThat(e.getCause(), instanceOf(NullPointerException.class));
		}
	}
}
