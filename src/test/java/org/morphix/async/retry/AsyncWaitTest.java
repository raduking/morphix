package org.morphix.async.retry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link AsyncWait}.
 *
 * @author Radu Sebastian LAZIN
 */
class AsyncWaitTest {

	static class TestAsyncWait implements AsyncWait {

		@Override
		public boolean keepWaiting() {
			return false;
		}
	}

	@Test
	void shouldReturnDefaultIntervalAndTimeUnit() {
		TestAsyncWait asyncWait = new TestAsyncWait();

		assertThat(asyncWait.interval(), equalTo(AsyncWait.Default.INTERVAL));
		assertThat(asyncWait.timeUnit(), equalTo(AsyncWait.Default.TIME_UNIT));
	}

	@Test
	void shouldHaveFunctionalInterfaceAnnotation() {
		assertTrue(AsyncWait.class.isAnnotationPresent(FunctionalInterface.class));
	}

	@Test
	void shouldThrowExceptionOnCallingWaitDefaultConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(AsyncWait.Default.class);

		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldReturnImmediatelyWhenConditionIsAlreadyTrue() throws Exception {
		boolean result = AsyncWait.until(() -> true).get(1, TimeUnit.SECONDS);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseImmediatelyWhenTimeoutIsNegative() throws Exception {
		boolean result = AsyncWait.until(() -> false, Duration.ofSeconds(-1)).get(1, TimeUnit.SECONDS);

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueWhenConditionBecomesTrueBeforeTimeout() throws Exception {
		AtomicInteger checks = new AtomicInteger();

		boolean result = AsyncWait.until(() -> checks.incrementAndGet() >= 2, Duration.ofSeconds(1), Duration.ofMillis(10))
				.get(1, TimeUnit.SECONDS);

		assertTrue(result);
	}
}
