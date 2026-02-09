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

/**
 * Functional interface for converting a value of type T to its string representation. This can be used in various
 * contexts where a custom string conversion is needed, such as in logging, serialization, or building string
 * representations of objects.
 *
 * @param <T> the type of the value to convert
 * @author Radu Sebastian LAZIN
 */
public interface ToStringFunction<T> extends Function<T, String> {

	/**
	 * Converts the given value to a string representation.
	 *
	 * @param t the object to convert
	 * @return the string representation of the value
	 */
	String toString(T t);

	/**
	 * @see Function#apply(Object)
	 */
	@Override
	default String apply(final T t) {
		return toString(t);
	}

	/**
	 * Creates a {@link ToStringFunction} that uses the default {@code toString()} method of the value.
	 *
	 * @param <T> the type of the value
	 * @return a {@link ToStringFunction} that converts values to strings using their default {@code toString()} method
	 */
	static <T> ToStringFunction<T> identity() {
		return T::toString;
	}

	/**
	 * Creates a {@link ToStringFunction} that converts the value to a string and then converts it to lower case.
	 *
	 * @param <T> the type of the value
	 * @return a {@link ToStringFunction} that converts values to lower case strings
	 */
	static <T> ToStringFunction<T> toLowerCase() {
		return object -> object.toString().toLowerCase();
	}

	/**
	 * Creates a {@link ToStringFunction} that converts the value to a string and then converts it to upper case.
	 *
	 * @param <T> the type of the value
	 * @return a {@link ToStringFunction} that converts values to upper case strings
	 */
	static <T> ToStringFunction<T> toUpperCase() {
		return object -> object.toString().toUpperCase();
	}
}
