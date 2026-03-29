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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import org.morphix.lang.collections.AlwaysEmptyList;
import org.morphix.lang.collections.Lists;
import org.morphix.lang.function.Runnables;
import org.morphix.lang.function.Suppliers;
import org.morphix.lang.retry.Retry;

/**
 * Information accumulator abstract class.<br/>
 * It accumulates information based on a given action.
 * <p>
 * Actions can be given via:<br/>
 * {@link Supplier}, {@link Runnable} classes.
 * <p>
 * Currently used to accumulate exceptions during retries, see {@link Retry}.
 *
 * @param <T> accumulated information type
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class Accumulator<T> {

	/**
	 * Holds the accumulated information.
	 */
	private final List<T> list;

	/**
	 * Hidden constructor.
	 */
	protected Accumulator() {
		this(new CopyOnWriteArrayList<>());
	}

	/**
	 * Hidden constructor.
	 *
	 * @param list list to use for accumulation
	 */
	private Accumulator(final List<T> list) {
		this.list = list;
	}

	/**
	 * Information accumulator method. The second supplier is used to provide a default return value in case no information
	 * was accumulated. It is a supplier to avoid unnecessary computation of the default return value.
	 *
	 * @param <U> supplier type
	 *
	 * @param supplier information supplier
	 * @param defaultReturnSupplier default return supplier
	 * @return the supplier result or a default return
	 */
	public abstract <U> U accumulate(final Supplier<U> supplier, final Supplier<U> defaultReturnSupplier);

	/**
	 * Information accumulator method.
	 *
	 * @param runnable action
	 */
	public void accumulate(final Runnable runnable) {
		accumulate(Runnables.toSupplier(runnable), Suppliers.supplyNull());
	}

	/**
	 * Information accumulator method.
	 *
	 * @param <U> supplier type
	 *
	 * @param supplier information supplier
	 * @return the supplier result
	 */
	public <U> U accumulate(final Supplier<U> supplier) {
		return accumulate(supplier, Suppliers.supplyNull());
	}

	/**
	 * This method should be called to signal that the accumulator finished accumulating the wanted information.
	 * <p>
	 * Can be implemented in derived classes for any purpose. By default, it clears the accumulated information.
	 */
	public void rest() {
		clear();
	}

	/**
	 * Clears all the accumulated information.
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * Returns all the accumulated information.
	 *
	 * @return all the accumulated information
	 */
	public List<T> getInformationList() {
		return list;
	}

	/**
	 * Returns the first accumulated information, null if no information was accumulated.
	 *
	 * @return the last accumulated information.
	 */
	public T firstInformation() {
		return Lists.first(list);
	}

	/**
	 * Returns the last accumulated information, null if no information was accumulated.
	 *
	 * @return the last accumulated information.
	 */
	public T lastInformation() {
		return Lists.last(list);
	}

	/**
	 * Returns true if the information accumulated is empty.
	 *
	 * @return true if the information accumulated is empty
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * Returns true if the information accumulated is not empty. Equivalent to {@link #hasInformation()}.
	 *
	 * @return true if the information accumulated is not empty
	 */
	public boolean isNotEmpty() {
		return !isEmpty();
	}

	/**
	 * Returns true if the information accumulated is not empty. Equivalent to {@link #isNotEmpty()}.
	 *
	 * @return true if the information accumulated is not empty
	 */
	public boolean hasInformation() {
		return isNotEmpty();
	}

	/**
	 * Returns the size of this accumulator meaning how much information it accumulated.
	 *
	 * @return the size of this accumulator meaning how much information it accumulated
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Adds information to the accumulator.
	 *
	 * @param information information to add
	 */
	public void addInformation(final T information) {
		getInformationList().add(information);
	}

	/**
	 * Returns an empty accumulator. Equivalent to {@link #empty()}
	 *
	 * @param <T> accumulator information type
	 * @return an empty accumulator
	 */
	@SuppressWarnings("unchecked")
	public static <T> Accumulator<T> noAccumulator() {
		return (Accumulator<T>) EmptyAccumulator.EMPTY_ACCUMULATOR;
	}

	/**
	 * Returns an empty accumulator. Equivalent to {@link #noAccumulator()}.
	 *
	 * @param <T> accumulator information type
	 *
	 * @return an empty accumulator
	 */
	public static <T> Accumulator<T> empty() {
		return noAccumulator();
	}

	/**
	 * Basic implementation which accumulates nothing.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class EmptyAccumulator extends Accumulator<Object> { // NOSONAR we need only one instance

		/**
		 * Singleton instance.
		 */
		private static final EmptyAccumulator EMPTY_ACCUMULATOR = new EmptyAccumulator();

		/**
		 * Private constructor.
		 */
		private EmptyAccumulator() {
			super(AlwaysEmptyList.of());
		}

		/**
		 * @see Accumulator#accumulate(Supplier, Supplier)
		 */
		@Override
		public <U> U accumulate(final Supplier<U> supplier, final Supplier<U> defaultReturn) {
			return supplier.get();
		}
	}
}
