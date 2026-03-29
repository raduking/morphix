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
package org.morphix.lang.accumulator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.morphix.lang.accumulator.ExceptionsAccumulator.Throw;

/**
 * Test class for {@link ExceptionsAccumulator}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExceptionsAccumulatorTest {

	private static final int COUNT = 3;

	@Test
	void shouldBuildExceptionsAccumulatorFromSet() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(Set.of(RuntimeException.class));

		assertThat(ea.getExceptionTypes(), hasSize(1));
		assertThat(ea.getExceptionTypes().getFirst(), equalTo(RuntimeException.class));
		assertFalse(ea.isWrapException());
		assertTrue(ea.isThrowException());
	}

	@Test
	void shouldBuildExceptionsAccumulatorFromNullThrowMode() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of((Throw) null);

		assertThat(ea.getExceptionTypes(), empty());
	}

	@Test
	void shouldBuildExceptionsAccumulatorFromNullExceptions() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of((Set<Class<?>>) null);

		assertThat(ea.getExceptionTypes(), empty());
	}

	@Test
	void shouldBuildExceptionsAccumulator() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();

		assertThat(ea.getExceptionTypes(), empty());
		assertFalse(ea.isWrapException());
		assertTrue(ea.isThrowException());
	}

	@Test
	void shouldBuildExceptionsAccumulatorWithAllParams() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(Throw.NONE, Set.of(RuntimeException.class));

		assertThat(ea.getExceptionTypes(), hasSize(1));
		assertThat(ea.getExceptionTypes().getFirst(), equalTo(RuntimeException.class));
		assertFalse(ea.isWrapException());
		assertFalse(ea.isThrowException());
	}

	@Test
	void shouldBuildExceptionsAccumulatorWithThrowMode() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(Throw.NONE);

		assertThat(ea.getExceptionTypes(), hasSize(0));
		assertFalse(ea.isWrapException());
		assertFalse(ea.isThrowException());
	}

	@Test
	void shouldAccumulateAllExceptions() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			ea.accumulate(() -> {
				throw new RuntimeException();
			});
		}

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertTrue(ea.hasExceptions());
	}

	@Test
	void shouldNotAccumulateAnything() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			ea.accumulate(() -> COUNT);
		}

		assertThat(ea.getExceptions(), hasSize(0));
		assertFalse(ea.hasExceptions());
	}

	@Test
	void shouldAccumulateAllExceptionsWithRunnable() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			ea.accumulate((Runnable) () -> {
				throw new RuntimeException();
			});
		}

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertTrue(ea.hasExceptions());
	}

	@Test
	void shouldNotAccumulateAnythingWithRunnable() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			ea.accumulate(() -> {
				@SuppressWarnings("unused")
				int x = 0;
			});
		}

		assertThat(ea.getExceptions(), hasSize(0));
		assertFalse(ea.hasExceptions());
	}

	@Test
	void shouldAccumulateOnlyRequiredExceptions() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(Set.of(IllegalArgumentException.class));
		for (int i = 0; i < COUNT; ++i) {
			try {
				ea.accumulate(() -> {
					throw new RuntimeException();
				});
			} catch (Exception e) {
				// swallow
			}
			int n = i;
			try {
				ea.accumulate(() -> {
					throw new IllegalArgumentException(String.valueOf(n));
				});
			} catch (Exception e) {
				// swallow
			}
		}

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertThat(ea.lastException().getMessage(), equalTo(String.valueOf(COUNT - 1)));
	}

	@Test
	void shouldThrowLastExceptionOnRest() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			int n = i;
			ea.accumulate((Runnable) () -> {
				throw new RuntimeException(String.valueOf(n));
			});
		}
		RuntimeException result = assertThrows(RuntimeException.class, ea::rest);

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertThat(result.getMessage(), equalTo(String.valueOf(COUNT - 1)));
	}

	@Test
	void shouldThrowLastExceptionOnRestWhenThrowRawIsConfigured() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(Throw.RAW);
		for (int i = 0; i < COUNT; ++i) {
			int n = i;
			ea.accumulate((Runnable) () -> {
				throw new RuntimeException(String.valueOf(n));
			});
		}
		RuntimeException result = assertThrows(RuntimeException.class, ea::rest);

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertThat(result.getMessage(), equalTo(String.valueOf(COUNT - 1)));
	}

	@Test
	void shouldThrowWrappedLastExceptionOnRest() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(Throw.WRAPPED);
		for (int i = 0; i < COUNT; ++i) {
			int n = i;
			ea.accumulate((Runnable) () -> {
				throw new RuntimeException(String.valueOf(n));
			});
		}
		AccumulatorException result = assertThrows(AccumulatorException.class, ea::rest);

		assertThat(ea.getExceptions(), hasSize(COUNT));
		assertThat(result.getCause().getMessage(), equalTo(String.valueOf(COUNT - 1)));
		assertTrue(ea.isWrapException());
	}

	@Test
	void shouldNotDoAnythingWhenNoThrowingIsConfiguredOnRest() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of(Throw.NONE);
		for (int i = 0; i < COUNT; ++i) {
			int n = i;
			ea.accumulate((Runnable) () -> {
				throw new RuntimeException(String.valueOf(n));
			});
		}
		ea.rest();

		assertThat(ea.getExceptions(), hasSize(COUNT));
	}

	@Test
	void shouldNotDoAnythingOnRestWhenNothingIsAccumulated() {
		ExceptionsAccumulator ea = ExceptionsAccumulator.of();
		for (int i = 0; i < COUNT; ++i) {
			ea.accumulate(() -> COUNT);
		}
		ea.rest();

		assertFalse(ea.hasExceptions());
	}
}
