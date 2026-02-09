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
package org.morphix.lang;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.StreamSupport;

/**
 * Utility class for Java arrays. It is named JavaArrays to avoid confusion with {@link Arrays} class.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JavaArrays {

	/**
	 * Returns the provided array, or an empty array if the parameter is {@code null}.
	 *
	 * @param <T> the component type of the array
	 *
	 * @param array the provided array
	 * @param componentType due to type erasure this information is needed to create an empty array
	 * @return the given array if not null, empty array otherwise
	 */
	@SuppressWarnings("unchecked")
	static <T> T[] safe(final T[] array, final Class<T> componentType) {
		return null == array ? (T[]) Array.newInstance(componentType, 0) : array;
	}

	/**
	 * Returns {@code null} if the given array is empty, the array itself otherwise.
	 *
	 * @param <T> the component type of the array
	 *
	 * @param array the provided array
	 * @return {@code null} if the given array is empty, the array itself otherwise
	 */
	static <T> T[] nullIfEmpty(final T[] array) {
		return isEmpty(array) ? null : array;
	}

	/**
	 * Converts the given value into an Object array. This method is useful for handling various input types uniformly as
	 * arrays in scenarios such as processing method arguments, handling collections, or dealing with variable-length
	 * inputs, it simplifies the handling of different input types by providing a consistent array representation.
	 * <ul>
	 * <li>If the value is {@code null}, an empty array is returned.</li>
	 * <li>If the value is already an array, it is converted to Object array</li>
	 * <li>If the value is an instance of {@link Iterable}, it is converted to an Object array using streams.</li>
	 * <li>If the value is neither an array nor an {@link Iterable}, a single-element Object array is returned containing
	 * the value.</li>
	 * </ul>
	 * This method always returns a new Object array, ensuring that the returned array is a separate instance from the input
	 * value.
	 *
	 * @param value the array to convert
	 * @return the converted Object array
	 */
	static Object[] toArray(final Object value) {
		if (null == value) {
			return new Object[0];
		}
		Class<?> type = value.getClass();

		if (type.isArray()) {
			if (!type.getComponentType().isPrimitive()) {
				return ((Object[]) value).clone();
			}
			int length = Array.getLength(value);
			Object[] array = new Object[length];
			for (int i = 0; i < length; ++i) {
				array[i] = Array.get(value, i);
			}
			return array;
		}
		if (value instanceof Iterable<?> iterable) {
			return StreamSupport.stream(iterable.spliterator(), false).toArray();
		}
		return new Object[] { value };
	}

	/**
	 * Checks if the given array is null or has no elements.
	 *
	 * @param array the array to check
	 * @return true if the array is null or empty, false otherwise
	 */
	static boolean isEmpty(final Object[] array) {
		return null == array || 0 == array.length;
	}

	/**
	 * Checks if the given array is not null and has at least one element.
	 *
	 * @param array the array to check
	 * @return true if the array is not null and not empty, false otherwise
	 */
	static boolean isNotEmpty(final Object[] array) {
		return !isEmpty(array);
	}
}
