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
package org.morphix.async.accumulator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.morphix.lang.accumulator.Accumulator;
import org.morphix.lang.accumulator.ExceptionsAccumulator;
import org.morphix.lang.accumulator.ExceptionsAccumulator.Throw;

/**
 * Test class for {@link AsyncAccumulator}.
 *
 * @author Radu Sebastian LAZIN
 */
class AsyncAccumulatorTest {

	@Test
	void shouldRejectNullAccumulator() {
		assertThrows(NullPointerException.class, () -> new AsyncAccumulator<>(null));
	}

	@Test
	void shouldRunRunnableWithoutFailing() {
		AtomicBoolean called = new AtomicBoolean(false);
		AsyncAccumulator<Object> accumulator = new AsyncAccumulator<>(Accumulator.noAccumulator());

		assertDoesNotThrow(() -> accumulator.accumulate(() -> called.set(true)).join());
		assertTrue(called.get());
	}

	@Test
	void shouldAccumulateSuccessfulResult() {
		AsyncAccumulator<Object> accumulator = new AsyncAccumulator<>(Accumulator.noAccumulator());

		String result = accumulator.accumulate(() -> CompletableFuture.completedFuture("ok")).join();

		assertThat(result, equalTo("ok"));
		assertFalse(accumulator.isNotEmpty());
	}

	@Test
	void shouldReturnFailedFutureWhenAccumulatorRethrows() {
		AsyncAccumulator<Object> accumulator = new AsyncAccumulator<>(Accumulator.noAccumulator());

		ExecutionException e = assertThrows(ExecutionException.class,
				() -> accumulator.accumulate(() -> {
					throw new IllegalStateException("boom");
				}).get());

		assertThat(e.getCause(), instanceOf(IllegalStateException.class));
		assertThat(e.getCause().getMessage(), equalTo("boom"));
	}

	@Test
	void shouldAccumulateExceptionAndExposeLastInformation() {
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of(Throw.NONE);
		AsyncAccumulator<Exception> accumulator = new AsyncAccumulator<>(exceptionsAccumulator);

		String result = accumulator.accumulate(() -> CompletableFuture.failedFuture(new IllegalStateException("x")), () -> "default")
				.join();

		assertThat(result, equalTo("default"));
		assertTrue(accumulator.isNotEmpty());
		assertThat(accumulator.lastInformation().getMessage(), equalTo("x"));

		accumulator.rest();
	}

	@Test
	void shouldFailExhaustWhenRestThrows() {
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();
		AsyncAccumulator<Exception> accumulator = new AsyncAccumulator<>(exceptionsAccumulator);
		accumulator.accumulate(() -> {
			throw new IllegalStateException("boom");
		}).join();

		ExecutionException e = assertThrows(ExecutionException.class, () -> accumulator.exhaust("done").get());

		assertThat(e.getCause(), instanceOf(IllegalStateException.class));
	}

	@Test
	void shouldExhaustWithResultWhenEmpty() {
		AsyncAccumulator<Object> accumulator = new AsyncAccumulator<>(Accumulator.noAccumulator());

		assertThat(accumulator.exhaust("done").join(), equalTo("done"));
	}

	@Test
	void shouldExhaustWithResultWhenRestDoesNotThrow() {
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of(Throw.NONE);
		AsyncAccumulator<Exception> accumulator = new AsyncAccumulator<>(exceptionsAccumulator);
		accumulator.accumulate(() -> {
			throw new IllegalStateException("boom");
		}).join();

		assertThat(accumulator.exhaust("done").join(), equalTo("done"));
	}
}
