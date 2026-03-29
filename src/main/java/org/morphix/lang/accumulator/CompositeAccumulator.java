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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
import org.morphix.lang.collections.Lists;

/**
 * Composite accumulator to aggregate multiple accumulators.
 *
 * @author Radu Sebastian LAZIN
 */
public class CompositeAccumulator extends Accumulator<Object> {

	/**
	 * List of accumulators contained in this composite.
	 */
	private final List<Accumulator<?>> accumulators = new ArrayList<>();

	/**
	 * Constructor with multiple accumulators.
	 *
	 * @param accumulators accumulators to add to the composite
	 */
	@SafeVarargs
	private CompositeAccumulator(final Accumulator<?>... accumulators) {
		Nullables.whenNotNull(accumulators)
				.then(accs -> this.accumulators.addAll(List.of(accs)));
	}

	/**
	 * Returns a composite accumulator containing the given accumulators.
	 *
	 * @param accumulators the accumulators
	 * @return a composite accumulator containing the given accumulators
	 */
	@SafeVarargs
	public static CompositeAccumulator of(final Accumulator<?>... accumulators) {
		return new CompositeAccumulator(accumulators);
	}

	/**
	 * Returns an empty composite accumulator.
	 *
	 * @return an empty composite accumulator
	 */
	public static CompositeAccumulator of() {
		return of((Accumulator<? super Object>[]) null);
	}

	/**
	 * @see Accumulator#accumulate(Supplier, Supplier)
	 */
	@Override
	public <U> U accumulate(final Supplier<U> supplier, final Supplier<U> defaultReturnSupplier) {
		if (Lists.isEmpty(accumulators)) {
			return defaultReturnSupplier.get();
		}
		Supplier<U> chainSupplier = supplier;
		for (int i = accumulators.size() - 1; i > 0; --i) {
			Accumulator<?> accumulator = accumulators.get(i);
			Supplier<U> tempSupplier = chainSupplier;
			chainSupplier = () -> accumulator.accumulate(tempSupplier, defaultReturnSupplier);
		}
		return Lists.first(accumulators).accumulate(chainSupplier);
	}

	/**
	 * @see Accumulator#getInformationList()
	 */
	@Override
	public List<Object> getInformationList() {
		return JavaObjects.cast(accumulators.stream()
				.flatMap(accumulator -> accumulator.getInformationList().stream())
				.toList());
	}

	/**
	 * @see Accumulator#rest()
	 */
	@Override
	public void rest() {
		accumulators.forEach(Accumulator::rest);
	}

	/**
	 * @see Accumulator#clear()
	 */
	@Override
	public void clear() {
		accumulators.forEach(Accumulator::clear);
	}

	/**
	 * Returns the contained accumulators.
	 *
	 * @return the contained accumulators
	 */
	public List<Accumulator<Object>> getAccumulators() {
		return JavaObjects.cast(accumulators);
	}

}
