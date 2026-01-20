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

import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;

/**
 * Suppliers utility methods.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Suppliers {

	/**
	 * A supplier that always returns true.
	 */
	private static final Supplier<Boolean> SUPPLY_TRUE = () -> true;

	/**
	 * A supplier that always returns false.
	 */
	private static final Supplier<Boolean> SUPPLY_FALSE = () -> false;

	/**
	 * A supplier that always returns null.
	 */
	private static final Supplier<Object> SUPPLY_NULL = () -> null;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Suppliers() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a supplier that always returns true.
	 *
	 * @return a supplier that always returns true
	 */
	public static Supplier<Boolean> supplyTrue() {
		return SUPPLY_TRUE;
	}

	/**
	 * Returns a supplier that always returns false.
	 *
	 * @return a supplier that always returns false
	 */
	public static Supplier<Boolean> supplyFalse() {
		return SUPPLY_FALSE;
	}

	/**
	 * Returns a supplier that always returns null.
	 *
	 * @param <T> the return type of the supplier
	 *
	 * @return a supplier that always returns null
	 */
	public static <T> Supplier<T> supplyNull() {
		return JavaObjects.cast(SUPPLY_NULL);
	}
}
