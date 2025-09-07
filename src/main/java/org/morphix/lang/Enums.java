/*
 * Copyright 2025 the original author or authors.
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
package org.morphix.lang;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.morphix.reflection.Constructors;

/**
 * Utility class for java enums.
 *
 * @author Radu Sebastian LAZIN
 */
public class Enums {

	/**
	 * Builds a name map for all the given values for an enum.
	 *
	 * @param <T> enum type
	 *
	 * @param values values for an enum
	 * @return name map
	 */
	public static <T extends Enum<T>> Map<String, T> buildNameMap(final T[] values) {
		return buildNameMap(values, T::toString);
	}

	/**
	 * Builds a name map for all the given values for an enum using a string mapper function. The mapper function can be for
	 * example {@link Enum#name()}, {@link Enum#toString()}, etc.
	 *
	 * @param <T> enum type
	 * @param <K> name map key type
	 *
	 * @param values values for an enum
	 * @param mapper the function which maps the value to and from the given key type
	 * @return name map
	 */
	public static <K, T extends Enum<T>> Map<K, T> buildNameMap(final T[] values, final Function<T, K> mapper) {
		return Stream.of(values).collect(Collectors.toUnmodifiableMap(mapper, Function.identity()));
	}

	/**
	 * Transforms from a string to an enum value given an enum map (can further be enhanced to work for just the enum class
	 * as parameter).
	 *
	 * @param <T> enum type
	 *
	 * @param enumStringValue the string value of the enum
	 * @param nameMap string to enum value map
	 * @param values expected values for an enum
	 * @return enum value
	 */
	public static <T extends Enum<T>> T fromString(final String enumStringValue, final Map<String, T> nameMap, final T[] values) {
		return from(enumStringValue, nameMap, values);
	}

	/**
	 * Transforms from a string to an enum value given an enum map (can further be enhanced to work for just the enum class
	 * as parameter).
	 *
	 * @param <T> enum type
	 * @param <K> name map key type
	 *
	 * @param value the value of the enum
	 * @param nameMap string to enum value map
	 * @param values expected values for an enum
	 * @return enum value
	 */
	public static <K, T extends Enum<T>> T from(final K value, final Map<K, T> nameMap, final T[] values) {
		T enumValue = nameMap.get(value);
		if (null == enumValue) {
			throw new IllegalArgumentException("'" + value + "' has no corresponding value. "
					+ "Accepted values: " + List.of(values));
		}
		return enumValue;
	}

	/**
	 * Transforms from a string to an enum value given an enum map (can further be enhanced to work for just the enum class
	 * as parameter). This method will return a default value if the value is not found.
	 *
	 * @param <T> enum type
	 * @param <K> name map key type
	 *
	 * @param value the string value of the enum
	 * @param nameMap string to enum value map
	 * @param defaultValueSupplier supplies a default value
	 * @return enum value
	 */
	public static <K, T extends Enum<T>> T from(final K value, final Map<K, T> nameMap, final Supplier<T> defaultValueSupplier) {
		T enumValue = nameMap.get(value);
		return Nullables.nonNullOrDefault(enumValue, defaultValueSupplier);
	}

	/**
	 * Transforms from a string to an enum value given an enum map (can further be enhanced to work for just the enum class
	 * as parameter). This method will return a default value if the value is not found.
	 *
	 * @param <T> enum type
	 *
	 * @param enumStringValue the string value of the enum
	 * @param nameMap string to enum value map
	 * @param defaultValueSupplier supplies a default value
	 * @return enum value
	 */
	public static <T extends Enum<T>> T fromString(final String enumStringValue, final Map<String, T> nameMap,
			final Supplier<T> defaultValueSupplier) {
		return from(enumStringValue, nameMap, defaultValueSupplier);
	}

	/**
	 * Returns the same value as {@link Enum#valueOf(Class, String)} except it returns null when the name is null instead of
	 * {@link NullPointerException}.
	 *
	 * @param <T> enum type
	 *
	 * @param enumClass enum class
	 * @param name enum name
	 * @return enum value
	 */
	public static <T extends Enum<T>> T valueOf(final Class<T> enumClass, final String name) {
		if (null == name) {
			return null;
		}
		try {
			return Enum.valueOf(enumClass, name);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Enum does not exist: " + enumClass.getCanonicalName() + "." + name, e);
		}
	}

	/**
	 * Returns the result of calling {@link Enum#name()} by checking for null enum first. If the enum is {@code null}, the
	 * result is {@code null}.
	 *
	 * @param <T> enum type
	 *
	 * @param enumObject enum object
	 * @return the name of the enum object
	 */
	public static <T extends Enum<T>> String safeName(final T enumObject) {
		return null != enumObject ? enumObject.name() : null;
	}

	/**
	 * Private constructor.
	 */
	private Enums() {
		throw Constructors.unsupportedOperationException();
	}

}
