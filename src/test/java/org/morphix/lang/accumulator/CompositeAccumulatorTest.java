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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.morphix.lang.accumulator.ExceptionsAccumulator.Throw;
import org.morphix.lang.function.Runnables;

/**
 * Test class for {@link CompositeAccumulator}.
 *
 * @author Radu Sebastian LAZIN
 */
class CompositeAccumulatorTest {

	private static final String DEFAULT = "default";
	private static final String VALUE = "value";

	@Test
	void shouldInstantiateWithMoreAccumulators() {
		ExceptionsAccumulator exceptionsAccumulator = ExceptionsAccumulator.of();
		DurationAccumulator durationAccumulator = DurationAccumulator.of();

		CompositeAccumulator victim = CompositeAccumulator.of(durationAccumulator, exceptionsAccumulator);

		assertThat(victim.getAccumulators(), hasSize(2));
	}

	@Test
	void shouldAccumulateWithEachAccumulatorInComposite() {
		DurationAccumulator da1 = DurationAccumulator.of();
		DurationAccumulator da2 = DurationAccumulator.of();

		CompositeAccumulator victim = CompositeAccumulator.of(da1, da2);
		victim.accumulate(Runnables.doNothing());

		assertThat(victim.getInformationList(), hasSize(2));
	}

	@Test
	void shouldAccumulateWithEachAccumulatorButExceptionsAccumulatorInComposite() {
		DurationAccumulator da1 = DurationAccumulator.of();
		ExceptionsAccumulator ea1 = ExceptionsAccumulator.of();

		CompositeAccumulator victim = CompositeAccumulator.of(da1, ea1);
		victim.accumulate(Runnables.doNothing());

		assertThat(victim.getInformationList(), hasSize(1));
	}

	@Test
	void shouldAccumulateWithEachAccumulatorInCompositeIncludingExceptions() {
		DurationAccumulator da1 = DurationAccumulator.of();
		ExceptionsAccumulator ea1 = ExceptionsAccumulator.of(Throw.NONE);

		CompositeAccumulator victim = CompositeAccumulator.of(ea1, da1);
		victim.accumulate(() -> {
			throw new RuntimeException();
		});

		assertThat(victim.getInformationList(), hasSize(2));
	}

	@Test
	void shouldInstantiateEmptyCompositeAccumulator() {
		CompositeAccumulator victim = CompositeAccumulator.of();

		assertThat(victim.getAccumulators(), hasSize(0));
	}

	@Test
	void shouldReturnDefaultValueWhenNoAccumulators() {
		CompositeAccumulator victim = CompositeAccumulator.of();

		String result = victim.accumulate(() -> VALUE, () -> DEFAULT);

		assertThat(victim.getAccumulators(), hasSize(0));
		assertThat(victim.isEmpty(), equalTo(true));
		assertThat(result, equalTo(DEFAULT));
	}

	@Test
	void shouldCallClearOnAllAccumulators() {
		TestAccumulator acc1 = new TestAccumulator();
		TestAccumulator acc2 = new TestAccumulator();

		CompositeAccumulator victim = CompositeAccumulator.of(acc1, acc2);
		victim.accumulate(() -> VALUE);

		assertThat(acc1.isNotEmpty(), equalTo(true));
		assertThat(acc2.isNotEmpty(), equalTo(true));

		victim.clear();

		assertThat(acc1.isEmpty(), equalTo(true));
		assertThat(acc2.isEmpty(), equalTo(true));
	}

	@Test
	void shouldCallRestOnAllAccumulators() {
		TestAccumulator acc1 = new TestAccumulator();
		TestAccumulator acc2 = new TestAccumulator();

		CompositeAccumulator victim = CompositeAccumulator.of(acc1, acc2);
		victim.accumulate(() -> VALUE);

		assertThat(acc1.isNotEmpty(), equalTo(true));
		assertThat(acc2.isNotEmpty(), equalTo(true));

		victim.rest();

		assertThat(acc1.isEmpty(), equalTo(true));
		assertThat(acc2.isEmpty(), equalTo(true));
	}

	static class TestAccumulator extends Accumulator<Boolean> {

		@Override
		public <U> U accumulate(final Supplier<U> supplier, final Supplier<U> defaultReturn) {
			getInformationList().add(Boolean.TRUE);
			return supplier.get();
		}
	}
}
