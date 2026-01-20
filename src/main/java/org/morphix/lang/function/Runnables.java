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
package org.morphix.lang.function;

import java.util.function.Supplier;

import org.morphix.reflection.Constructors;

/**
 * Utility methods for {@link Runnable} objects.
 *
 * @author Radu Sebastian LAZIN
 */
public class Runnables {

	/**
	 * An empty runnable.
	 */
	private static final Runnable EMPTY_RUNNABLE = () -> {
		// empty
	};

	/**
	 * Private constructor.
	 */
	private Runnables() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a runnable that does nothing.
	 *
	 * @return a runnable that does nothing
	 */
	public static Runnable doNothing() {
		return EMPTY_RUNNABLE;
	}

	/**
	 * Transforms a runnable to a supplier that returns null. This method is useful when you need a streamlined way of
	 * handling suppliers and runnable-s in functional calls.
	 *
	 * @param <T> supplier return type
	 * @param runnable runnable to transform to supplier
	 * @return null
	 */
	public static <T> Supplier<T> toSupplier(final Runnable runnable) {
		return Suppliers.supplyNull(runnable);
	}

	/**
	 * Transforms a runnable and a supplier to another supplier that runs the runnable first and the supplier second.
	 *
	 * @param <T> supplier return type
	 * @param runnable runnable to run before the supplier
	 * @param supplier supplier to run after the runnable
	 * @return supplier
	 */
	public static <T> Supplier<T> compose(final Runnable runnable, final Supplier<T> supplier) {
		return Suppliers.compose(runnable, supplier);
	}

}
