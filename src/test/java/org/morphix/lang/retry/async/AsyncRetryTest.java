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
package org.morphix.lang.retry.async;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.lang.Messages;
import org.morphix.lang.accumulator.Accumulator;
import org.morphix.lang.accumulator.ExceptionsAccumulator;

/**
 * Test class for {@link AsyncRetry}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class AsyncRetryTest {

	private static final Logger LOGGER = Logger.getLogger(AsyncRetryTest.class.getName());

	private static final int RETRY_COUNT = 3;
	private static final String STRING_RESULT = "Done";
	private static final String NAME = "Foo";
	private static final long TIMEOUT_MILLIS = 5000;

	@Spy
	private Foo inSupplier;

	@Spy
	private Foo inConsumer;

	@Test
	void shouldRetryGivenTimesWithAsyncSupplier() throws Exception {
		AsyncRetry retry = AsyncRetry.of(AsyncWaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		CompletableFuture<Object> result = retry.until(() -> {
			inSupplier.foo();
			return CompletableFuture.completedFuture(null);
		}, Objects::nonNull);

		assertNull(result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
		verify(inSupplier, times(RETRY_COUNT)).foo();
	}

	@Test
	void shouldRetryGivenTimesWithSyncSupplier() throws Exception {
		AsyncRetry retry = AsyncRetry.of(AsyncWaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		CompletableFuture<Object> result = retry.until(() -> {
			inSupplier.foo();
			return CompletableFuture.completedFuture(null);
		}, Objects::nonNull);

		assertNull(result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
		verify(inSupplier, times(RETRY_COUNT)).foo();
	}

	@Test
	void shouldNotRetryWithNoRetry() throws Exception {
		AsyncRetry retry = AsyncRetry.NO_RETRY;

		CompletableFuture<Object> result = retry.until(() -> {
			inSupplier.foo();
			return CompletableFuture.completedFuture(null);
		}, Objects::nonNull);

		assertNull(result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
		verify(inSupplier).foo();
	}

	@Test
	void shouldNotRetryWithNoRetryMethod() throws Exception {
		AsyncRetry retry = AsyncRetry.noRetry();

		CompletableFuture<Object> result = retry.until(() -> {
			inSupplier.foo();
			return CompletableFuture.completedFuture(null);
		}, Objects::nonNull);

		assertNull(result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
		verify(inSupplier).foo();
	}

	@Test
	void shouldReturnDefaultRetry() {
		AsyncRetry retry = AsyncRetry.defaultRetry();

		assertThat(retry, equalTo(AsyncRetry.DEFAULT));
	}

	@Test
	void shouldRetryGivenTimesWithEmptyAccumulator() throws Exception {
		AsyncRetry retry = AsyncRetry.of(AsyncWaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		CompletableFuture<?> result = retry.until(() -> CompletableFuture.completedFuture(inSupplier.foo()), Objects::nonNull,
				Accumulator.empty());

		assertNull(result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
		verify(inSupplier, times(RETRY_COUNT)).foo();
	}

	@Test
	void shouldRetryGivenTimesAndAccumulateExceptions() throws Exception {
		AsyncRetry retry = AsyncRetry.of(AsyncWaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();
		CompletableFuture<String> result = retry.until(() -> {
			inSupplier.foo();
			int c = counter.incrementAndGet();
			if (c < RETRY_COUNT) {
				throw expectedExceptions.get(c - 1);
			}
			return CompletableFuture.completedFuture(STRING_RESULT);
		}, Objects::nonNull, exceptionsAccumulator);

		assertThat(result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), equalTo(STRING_RESULT));
		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldPolicyRetryGivenTimesAndAccumulateExceptions() throws Exception {
		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();

		CompletableFuture<String> result = AsyncRetry.of(AsyncWaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<String, Exception>policy()
				.stopWhen(STRING_RESULT::equals)
				.accumulateWith(exceptionsAccumulator)
				.onAsync(() -> {
					inSupplier.foo();
					int c = counter.incrementAndGet();
					if (c < RETRY_COUNT) {
						throw expectedExceptions.get(c - 1);
					}
					return CompletableFuture.completedFuture(STRING_RESULT);
				});

		assertThat(result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), equalTo(STRING_RESULT));
		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldPolicyRetryGivenTimesConsumeAndAccumulateExceptions() throws Exception {
		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();

		var retry = AsyncRetry.of(AsyncWaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<String, Exception>policy()
				.stopWhen(STRING_RESULT::equals)
				.consumeBeforeWait(e -> inConsumer.foo(e))
				.accumulateWith(exceptionsAccumulator);

		CompletableFuture<String> result = retry.onAsync(() -> {
			inSupplier.foo();
			int c = counter.incrementAndGet();
			if (c < RETRY_COUNT) {
				throw expectedExceptions.get(c - 1);
			}
			return CompletableFuture.completedFuture(STRING_RESULT);
		});

		assertThat(result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), equalTo(STRING_RESULT));
		verify(inSupplier, times(RETRY_COUNT)).foo();
		for (RuntimeException e : expectedExceptions) {
			verify(inConsumer).foo(e);
		}
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldPolicyRetryGivenTimesExecuteBeforeWait() throws Exception {
		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger retryCounter = new AtomicInteger(0);
		AtomicInteger doBeforeCounter = new AtomicInteger(0);

		var retry = AsyncRetry.of(AsyncWaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<String, Exception>policy()
				.stopWhen(STRING_RESULT::equals)
				.doBeforeWait(() -> {
					int c = doBeforeCounter.getAndIncrement();
					RuntimeException e = expectedExceptions.get(c);
					inConsumer.foo(e);
				});

		CompletableFuture<String> result = retry.onAsync(() -> {
			inSupplier.foo();
			int c = retryCounter.incrementAndGet();
			if (c < RETRY_COUNT) {
				return CompletableFuture.completedFuture(null);
			}
			return CompletableFuture.completedFuture(STRING_RESULT);
		});

		assertThat(result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), equalTo(STRING_RESULT));
		verify(inSupplier, times(RETRY_COUNT)).foo();
		for (RuntimeException e : expectedExceptions) {
			verify(inConsumer).foo(e);
		}
	}

	@Test
	void shouldPolicyRetryWithRunnableGivenTimesAndAccumulateExceptions() throws Exception {
		List<RuntimeException> expectedExceptions = IntStream.range(1, 3)
				.boxed()
				.map(i -> new RuntimeException(String.valueOf(i)))
				.toList();

		AtomicInteger counter = new AtomicInteger(0);
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();

		var retry = AsyncRetry.of(AsyncWaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)))
				.<Object, Exception>policy()
				.accumulateWith(exceptionsAccumulator);

		CompletableFuture<Object> result = retry.onAsync(() -> {
			inSupplier.foo();
			int c = counter.incrementAndGet();
			if (c < RETRY_COUNT) {
				throw expectedExceptions.get(c - 1);
			}
			return CompletableFuture.completedFuture(AsyncRetry.nonNull());
		});

		assertNotNull(result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT - 1));
		assertThat(exceptionsAccumulator.getExceptions(), equalTo(expectedExceptions));
	}

	@Test
	void shouldNotRetryWithNoRetryWhenAccumulatingInformation() {
		AsyncRetry retry = AsyncRetry.NO_RETRY;

		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();
		CompletableFuture<Object> result = retry.until(() -> {
			inSupplier.name();
			throw new RuntimeException();
		}, Objects::nonNull, exceptionsAccumulator);

		assertThrows(CompletionException.class, () -> result.join());
		verify(inSupplier).name();
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(1));
	}

	@Test
	void shouldNotRetryWithNoRetryMethodWhenAccumulatingInformation() {
		AsyncRetry retry = AsyncRetry.noRetry();

		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();
		CompletableFuture<Object> result = retry.until(() -> {
			inSupplier.name();
			throw new RuntimeException();
		}, Objects::nonNull, exceptionsAccumulator);

		assertThrows(CompletionException.class, () -> result.join());
		verify(inSupplier).name();
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(1));
	}

	@Test
	void shouldExposeLastErrorOnExhaustionWhenAccumulatorDoesNotThrow() throws Exception {
		AsyncRetry retry = AsyncRetry.of(AsyncWaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of(ExceptionsAccumulator.Throw.NONE);
		CompletableFuture<Object> result = retry.until(() -> {
			inSupplier.foo();
			throw new IllegalStateException();
		}, Objects::nonNull, exceptionsAccumulator);

		ExecutionException e = assertThrows(ExecutionException.class, () -> result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
		assertThat(e.getCause(), instanceOf(IllegalStateException.class));
		verify(inSupplier, times(RETRY_COUNT)).foo();
		assertThat(exceptionsAccumulator.getExceptions(), hasSize(RETRY_COUNT));
	}

	@Test
	void shouldReturnTheSameReferenceWhenNoRetry() {
		AsyncRetry retry1 = AsyncRetry.NO_RETRY;
		AsyncRetry retry2 = AsyncRetry.noRetry();

		assertSame(retry1, retry2);
	}

	@Test
	void shouldReturnNoWait() {
		AsyncWait wait = AsyncRetry.noWait();

		assertThat(wait, equalTo(AsyncRetry.NO_WAIT));
	}

	@Test
	void shouldReturnFalseOnEqualsIfParameterIsNull() {
		boolean equals = AsyncRetry.defaultRetry().equals(null);

		assertFalse(equals);
	}

	@Test
	void shouldReturnTrueOnEqualsForEqualRetries() {
		AsyncWait wait = AsyncWaitCounter.of(RETRY_COUNT, Duration.ofMillis(1));
		AsyncRetry retry1 = AsyncRetry.of(wait);
		AsyncRetry retry2 = AsyncRetry.of(wait);

		boolean equals = retry1.equals(retry2);

		assertTrue(equals);
	}

	@Test
	void shouldBuildHashCodeBasedOnWait() {
		AsyncWait wait = AsyncWaitCounter.of(RETRY_COUNT, Duration.ofMillis(1));
		AsyncRetry retry = AsyncRetry.of(wait);

		int expected = Objects.hash(wait);
		int result = retry.hashCode();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnFalseWhenKeepWaitingOnNoWait() {
		AsyncWait wait = AsyncRetry.noWait();

		boolean result = wait.keepWaiting();

		assertFalse(result);
	}

	@Test
	void shouldReturnTheStaticInstanceOnNoWait() {
		AsyncWait wait = AsyncRetry.noWait();

		boolean result = wait == AsyncRetry.NO_WAIT;

		assertTrue(result);
	}

	@Test
	void shouldFailTheFutureWithErrorOnNoAccumulator() {
		AsyncRetry retry = AsyncRetry.of(AsyncWaitCounter.of(RETRY_COUNT, Duration.ofSeconds(0)));

		CompletableFuture<Object> result = retry.until(() -> {
			throw new RuntimeException("boom");
		}, Objects::nonNull);

		Exception e = assertThrows(ExecutionException.class, () -> result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
		assertThat(e.getCause().getMessage(), equalTo("boom"));
	}

	public static class Foo {

		public <T> T foo() {
			return null;
		}

		public String name() {
			return NAME;
		}

		public void foo(final Exception e) {
			LOGGER.fine(() -> Messages.message("Exception: type: {}, message: {}", e.getClass(), e.getMessage()));
		}
	}
}
