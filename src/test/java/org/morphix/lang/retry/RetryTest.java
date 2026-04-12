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
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.lang.Messages;
import org.morphix.lang.accumulator.Accumulator;
import org.morphix.lang.accumulator.DurationAccumulator;
import org.morphix.lang.accumulator.ExceptionsAccumulator;
import org.morphix.lang.thread.Threads;
import org.morphix.lang.thread.Threads.ExecutionType;

/**
 * Test class for {@link Retry}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class RetryTest {

	private static final Logger LOGGER = Logger.getLogger(RetryTest.class.getName());

	private static final int RETRY_COUNT = 3;
	private static final String STRING_RESULT = "Done";
	private static final String NAME = "Foo";

	@Spy
	private Foo inSupplier;

	@Spy
	private Foo inConsumer;

	@Test
	void shouldRetryGivenTimes() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		retry.until(() -> {
			inSupplier.foo();
			return null;
		}, Objects::nonNull);

		verify(inSupplier, times(RETRY_COUNT)).foo();
	}

	@Test
	void shouldNotRetryWithNoRetry() {
		Retry retry = Retry.NO_RETRY;

		retry.until(() -> {
			inSupplier.foo();
			return null;
		}, Objects::nonNull);

		verify(inSupplier).foo();
	}

	@Test
	void shouldNotRetryWithNoRetryMethod() {
		Retry retry = Retry.noRetry();

		retry.until(() -> {
			inSupplier.foo();
			return null;
		}, Objects::nonNull);

		verify(inSupplier).foo();
	}

	@Test
	void shouldReturnDefaultRetry() {
		Retry retry = Retry.defaultRetry();

		assertThat(retry, equalTo(Retry.DEFAULT));
	}

	@Test
	void shouldRetryGivenTimesWithEmptyAccumulator() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		retry.until(() -> inSupplier.foo(), Objects::nonNull, Accumulator.noAccumulator());

		verify(inSupplier, times(RETRY_COUNT)).foo();
	}

	@Test
	void shouldRetryGivenTimesWithDurationAccumulator() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		DurationAccumulator durationAccumulator = DurationAccumulator.of();
		retry.until(() -> inSupplier.foo(), Objects::nonNull, durationAccumulator);

		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(durationAccumulator.getInformationList(), hasSize(RETRY_COUNT));
	}

	@Test
	void shouldRetryGivenTimesWithDurationAccumulatorSupplier() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		String foo = retry.until(() -> inSupplier.foo(), Objects::nonNull, DurationAccumulator::of);

		assertNull(foo);
		verify(inSupplier, times(RETRY_COUNT)).foo();
	}

	@Test
	void shouldRetryGivenTimesAndAccumulateAllExceptions() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();
		assertThrows(RuntimeException.class,
				() -> retry.until(
						() -> inSupplier.errorFoo(counter.incrementAndGet()),
						Objects::nonNull,
						exceptionsAccumulator));

		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT));
		for (int i = 1; i <= RETRY_COUNT; ++i) {
			verify(inSupplier).errorFoo(i);
			assertThat(exceptionsAccumulator.getExceptions().get(i - 1).getMessage(), equalTo(String.valueOf(i)));
		}
	}

	@Test
	void shouldRetryGivenTimesAndAccumulateExceptions() {
		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();
		String result = retry.until(() -> {
			inSupplier.foo();
			int c = counter.incrementAndGet();
			if (c < RETRY_COUNT) {
				throw expectedExceptions.get(c - 1);
			}
			return STRING_RESULT;
		}, Objects::nonNull, exceptionsAccumulator);

		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(result, equalTo(STRING_RESULT));
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldPolicyRetryGivenTimesAndAccumulateExceptions() {
		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();

		var result = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<String, Exception>policy()
				.stopWhen(STRING_RESULT::equals)
				.accumulateWith(exceptionsAccumulator)
				.on(() -> {
					inSupplier.foo();
					int c = counter.incrementAndGet();
					if (c < RETRY_COUNT) {
						throw expectedExceptions.get(c - 1);
					}
					return STRING_RESULT;
				});

		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(result, equalTo(STRING_RESULT));
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldPolicyRetryGivenTimesConsumeAndAccumulateExceptions() {
		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();

		var retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<String, Exception>policy()
				.stopWhen(STRING_RESULT::equals)
				.consumeBeforeWait(e -> inConsumer.foo(e))
				.accumulateWith(exceptionsAccumulator);

		var result = retry.on(() -> {
			inSupplier.foo();
			int c = counter.incrementAndGet();
			if (c < RETRY_COUNT) {
				throw expectedExceptions.get(c - 1);
			}
			return STRING_RESULT;
		});

		verify(inSupplier, times(RETRY_COUNT)).foo();
		for (RuntimeException e : expectedExceptions) {
			verify(inConsumer).foo(e);
		}
		assertThat(result, equalTo(STRING_RESULT));
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldPolicyRetryGivenTimesExecuteBeforeWait() {
		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger retryCounter = new AtomicInteger(0);
		AtomicInteger doBeforeCounter = new AtomicInteger(0);

		var retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<String, Exception>policy()
				.stopWhen(STRING_RESULT::equals)
				.doBeforeWait(() -> {
					int c = doBeforeCounter.getAndIncrement();
					RuntimeException e = expectedExceptions.get(c);
					inConsumer.foo(e);
				});

		var result = retry.on(() -> {
			inSupplier.foo();
			int c = retryCounter.incrementAndGet();
			if (c < RETRY_COUNT) {
				return null;
			}
			return STRING_RESULT;
		});

		verify(inSupplier, times(RETRY_COUNT)).foo();
		for (RuntimeException e : expectedExceptions) {
			verify(inConsumer).foo(e);
		}
		assertThat(result, equalTo(STRING_RESULT));
	}

	@Test
	void shouldPolicyRetryWithRunnableGivenTimesAndAccumulateExceptions() {
		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();

		var retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<Object, Exception>policy()
				.accumulateWith(exceptionsAccumulator);

		retry.on(() -> {
			inSupplier.foo();
			int c = counter.incrementAndGet();
			if (c < RETRY_COUNT) {
				throw expectedExceptions.get(c - 1);
			}
			return Retry.nonNull();
		});

		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldNotRetryWithNoRetryWhenAccumulatingInformation() {
		Retry retry = Retry.NO_RETRY;

		DurationAccumulator durationAccumulator = DurationAccumulator.of();
		String result = retry.until(() -> inSupplier.name(), Objects::nonNull, durationAccumulator);

		verify(inSupplier).name();
		assertThat(durationAccumulator.getInformationList(), hasSize(1));
		assertThat(result, equalTo(NAME));
	}

	@Test
	void shouldNotRetryWithNoRetryMethodWhenAccumulatingInformation() {
		Retry retry = Retry.noRetry();

		DurationAccumulator durationAccumulator = DurationAccumulator.of();
		String result = retry.until(() -> inSupplier.name(), Objects::nonNull, durationAccumulator);

		verify(inSupplier).name();
		assertThat(durationAccumulator.getInformationList(), hasSize(1));
		assertThat(result, equalTo(NAME));
	}

	@Test
	void shouldReturnTheSameReferenceWhenNoRetry() {
		Retry retry1 = Retry.NO_RETRY;
		Retry retry2 = Retry.noRetry();

		assertSame(retry1, retry2);
	}

	@Test
	void shouldRetryPolicyGivenTimesWithDurationAccumulator() {
		DurationAccumulator durationAccumulator = DurationAccumulator.of();
		var retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<String, Duration>policy()
				.accumulateWith(durationAccumulator);

		retry.on(() -> inSupplier.foo());

		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(durationAccumulator.getInformationList(), hasSize(RETRY_COUNT));
	}

	@Test
	void shouldReturnNoWait() {
		Wait wait = Retry.noWait();

		assertThat(wait, equalTo(Retry.NO_WAIT));
	}

	@Test
	void shouldRetryWithTheSameRetryInMultipleThreads() {
		int retryCount = 10;
		int threadCount = 10;
		List<Integer> integers = IntStream.range(0, threadCount).boxed().toList();

		Retry retry = Retry.of(WaitCounter.of(retryCount, Duration.ofMillis(10)));

		Threads.executeForEachIn(integers,
				i -> retry.until(() -> inSupplier.foo(), Objects::nonNull), ExecutionType.PARALLEL);

		verify(inSupplier, times(threadCount * retryCount)).foo();
	}

	@Test
	void shouldReturnFalseOnEqualsIfParameterIsNull() {
		boolean equals = Retry.defaultRetry().equals(null);

		assertFalse(equals);
	}

	@Test
	void shouldReturnTrueOnEqualsForEqualRetries() {
		Wait wait = WaitCounter.of(RETRY_COUNT, Duration.ofMillis(1));
		Retry retry1 = Retry.of(wait);
		Retry retry2 = Retry.of(wait);

		boolean equals = retry1.equals(retry2);

		assertTrue(equals);
	}

	@Test
	void shouldBuildHashCodeBasedOnWait() {
		Wait wait = WaitCounter.of(RETRY_COUNT, Duration.ofMillis(1));
		Retry retry = Retry.of(wait);

		int expected = Objects.hash(wait);
		int result = retry.hashCode();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnFalseWhenKeepWaitingOnNoWait() {
		Wait wait = Retry.noWait();

		boolean result = wait.keepWaiting();

		assertFalse(result);
	}

	@Test
	void shouldReturnTheStaticInstanceOnNoWait() {
		Wait wait = Retry.noWait();

		boolean result = wait == Retry.NO_WAIT;

		assertTrue(result);
	}

	public static class Foo {

		public <T> T foo() {
			return null;
		}

		public String name() {
			return NAME;
		}

		public String errorFoo(final int i) {
			throw new RuntimeException(String.valueOf(i));
		}

		public void foo(final Exception e) {
			LOGGER.fine(() -> Messages.message("Exception: type: {}, message: {}", e.getClass(), e.getMessage()));
		}
	}
}
