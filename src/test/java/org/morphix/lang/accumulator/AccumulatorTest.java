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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Accumulator}.
 *
 * @author Radu Sebastian LAZIN
 */
class AccumulatorTest {

	private static final String TEST_STRING = "test";
	private static final String DEFAULT_STRING = "default";
	private static final String CHECK_STRING = "check";

	@Test
	void shouldAccumulateInformation() {
		TestAccumulator accumulator = new TestAccumulator();

		accumulator.accumulate(() -> null, () -> null);

		assertThat(accumulator.getInformationList().size(), equalTo(1));
	}

	@Test
	void shouldAccumulateInformationWithRunnable() {
		TestAccumulator accumulator = new TestAccumulator();

		accumulator.accumulate(() -> {
			// empty
		});

		assertThat(accumulator.getInformationList().size(), equalTo(1));
	}

	@Test
	void shouldAccumulateInformationWithSupplier() {
		TestAccumulator accumulator = new TestAccumulator();

		accumulator.accumulate(() -> TEST_STRING);

		assertThat(accumulator.getInformationList().size(), equalTo(1));
	}

	@Test
	void shouldReturnSupplierValue() {
		TestAccumulator accumulator = new TestAccumulator();

		String result = accumulator.accumulate(() -> TEST_STRING, () -> DEFAULT_STRING);

		assertThat(result, equalTo(TEST_STRING));
		assertTrue(accumulator.isNotEmpty());
	}

	@Test
	void shouldReturnDefaultValue() {
		TestAccumulator accumulator = new TestAccumulator();

		String result = accumulator.accumulate(() -> CHECK_STRING, () -> DEFAULT_STRING);

		assertThat(result, equalTo(DEFAULT_STRING));
		assertThat(accumulator.getInformationList().size(), equalTo(2));
		assertThat(accumulator.getInformationList().get(0), equalTo(Boolean.TRUE));
		assertThat(accumulator.getInformationList().get(1), equalTo(Boolean.FALSE));
	}

	@Test
	void shouldReturnFirstAndLastInformation() {
		TestAccumulator accumulator = new TestAccumulator();

		String result = accumulator.accumulate(() -> CHECK_STRING, () -> DEFAULT_STRING);

		assertThat(result, equalTo(DEFAULT_STRING));
		assertThat(accumulator.firstInformation(), equalTo(Boolean.TRUE));
		assertThat(accumulator.lastInformation(), equalTo(Boolean.FALSE));
	}

	@Test
	void shouldReturnSize() {
		TestAccumulator accumulator = new TestAccumulator();

		assertThat(accumulator.size(), equalTo(0));

		accumulator.accumulate(() -> TEST_STRING);

		assertThat(accumulator.size(), equalTo(1));
	}

	@Test
	void shouldInitializeEmpty() {
		TestAccumulator accumulator = new TestAccumulator();

		assertTrue(accumulator.isEmpty());
		assertFalse(accumulator.isNotEmpty());
		assertThat(accumulator.size(), equalTo(0));
		assertFalse(accumulator.hasInformation());
	}

	@Test
	void shouldClearInformation() {
		TestAccumulator accumulator = new TestAccumulator();

		accumulator.accumulate(() -> TEST_STRING);

		assertThat(accumulator.size(), equalTo(1));

		accumulator.clear();

		assertThat(accumulator.size(), equalTo(0));
	}

	@Test
	void shouldClearInformationWithRest() {
		TestAccumulator accumulator = new TestAccumulator();

		accumulator.accumulate(() -> TEST_STRING);

		assertThat(accumulator.size(), equalTo(1));

		accumulator.rest();

		assertThat(accumulator.size(), equalTo(0));
	}

	@Test
	void shouldReturnTheSameEmptyInstance() {
		Accumulator<?> firstInstance = Accumulator.empty();
		Accumulator<?> secondInstance = Accumulator.empty();

		assertThat(firstInstance, equalTo(secondInstance));
	}

	@Test
	void shouldNotAccumulateAnyInformationWhenUsingEmptyAccumulator() {
		Accumulator<String> accumulator = Accumulator.empty();

		String result = accumulator.accumulate(() -> TEST_STRING, () -> DEFAULT_STRING);
		accumulator.accumulate(() -> {
			// empty
		});
		accumulator.accumulate(() -> null);

		assertThat(result, equalTo(TEST_STRING));
		assertThat(accumulator.size(), equalTo(0));
		assertThat(accumulator.getInformationList().size(), equalTo(0));
	}

	@Test
	void shouldNotAllowModificationOfInformationListForEmptyAccumulator() {
		Accumulator<String> accumulator = Accumulator.empty();

		accumulator.getInformationList().add(TEST_STRING);

		assertThat(accumulator.size(), equalTo(0));
		assertThat(accumulator.getInformationList().size(), equalTo(0));
	}

	static class TestAccumulator extends Accumulator<Boolean> {

		@Override
		public <U> U accumulate(final Supplier<U> supplier, final Supplier<U> defaultReturn) {
			getInformationList().add(Boolean.TRUE);
			U result = supplier.get();

			if (CHECK_STRING.equals(result)) {
				getInformationList().add(Boolean.FALSE);
				return defaultReturn.get();
			}
			return result;
		}
	}
}
