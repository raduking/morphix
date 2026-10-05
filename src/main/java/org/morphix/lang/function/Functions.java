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

import java.util.function.Function;

import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;

/**
 * Functions utility methods.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Functions {

	/**
	 * A function that always returns true.
	 */
	private static final Function<Object, Boolean> RETURN_TRUE = t -> true;

	/**
	 * A function that always returns false.
	 */
	private static final Function<Object, Boolean> RETURN_FALSE = t -> false;

	/**
	 * A function that always returns null.
	 */
	private static final Function<Object, Object> RETURN_NULL = t -> null;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Functions() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a function that always returns true.
	 *
	 * @param <T> the type of the input to the function
	 *
	 * @return a function that always returns true
	 */
	public static <T> Function<T, Boolean> returnTrue() {
		return JavaObjects.cast(RETURN_TRUE);
	}

	/**
	 * Returns a function that always returns false.
	 *
	 * @param <T> the type of the input to the function
	 *
	 * @return a function that always returns false
	 */
	public static <T> Function<T, Boolean> returnFalse() {
		return JavaObjects.cast(RETURN_FALSE);
	}

	/**
	 * Returns a function that always returns null.
	 *
	 * @param <T> the type of the input to the function
	 * @param <R> the type of the result of the function
	 *
	 * @return a function that always returns null
	 */
	public static <T, R> Function<T, R> returnNull() {
		return JavaObjects.cast(RETURN_NULL);
	}
}
