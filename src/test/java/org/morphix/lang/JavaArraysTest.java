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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JavaArrays}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaArraysTest {

	@Test
	void shouldReturnEmptyArrayWhenNullProvided() {
		String[] result = JavaArrays.safe(null, String.class);

		assertNotNull(result);
		assertThat(result.length, equalTo(0));
	}

	@Test
	void shouldReturnSameArrayWhenNotNullProvided() {
		String[] input = new String[] { "a", "b", "c" };
		String[] result = JavaArrays.safe(input, String.class);

		assertThat(result, equalTo(input));
	}

	@Test
	void shouldReturnNullWhenEmptyArrayProvided() {
		Integer[] input = new Integer[] { };
		Integer[] result = JavaArrays.nullIfEmpty(input);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnSameArrayWhenNonEmptyArrayProvided() {
		Integer[] input = new Integer[] { 1, 2, 3 };
		Integer[] result = JavaArrays.nullIfEmpty(input);

		assertThat(result, equalTo(input));
	}

	@Test
	void shouldConvertNullInputToEmptyArray() {
		Object[] result = JavaArrays.toArray(null);

		assertThat(result, equalTo(new Object[] { }));
	}

	@Test
	void shouldConvertPrimitiveArrayToObjectArray() {
		int[] intArray = new int[] { 1, 2, 3 };

		Object[] result = JavaArrays.toArray(intArray);

		assertThat(result, equalTo(new Object[] { 1, 2, 3 }));
	}

	@Test
	void shouldConvertIterableToObjectArray() {
		List<String> list = Arrays.asList("x", "y", "z");

		Object[] result = JavaArrays.toArray(list);

		assertThat(result, equalTo(new Object[] { "x", "y", "z" }));
	}

	@Test
	void shouldConvertSingleObjectInputToObjectArray() {
		Integer singleValue = 42;

		Object[] result = JavaArrays.toArray(singleValue);

		assertThat(result, equalTo(new Object[] { 42 }));
	}

	@Test
	void shouldConvertObjectArrayInputToObjectArray() {
		Object[] inputArray = new Object[] { "a", 1, true };

		Object[] result = JavaArrays.toArray(inputArray);

		assertThat(result, equalTo(new Object[] { "a", 1, true }));
	}

	@Test
	void shouldConvertStringArrayInputToObjectArray() {
		String[] stringArray = new String[] { "a", "b", "c" };

		Object[] result = JavaArrays.toArray(stringArray);

		assertThat(result, equalTo(new Object[] { "a", "b", "c" }));
	}

	@Test
	void shouldReturnFalseForNonEmptyArrayOnIsEmpty() {
		String[] array = new String[] { "not", "empty" };

		boolean result = JavaArrays.isEmpty(array);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnTrueForEmptyArrayOnIsEmpty() {
		String[] array = new String[] { };

		boolean result = JavaArrays.isEmpty(array);

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnTrueForNullArrayOnIsEmpty() {
		String[] array = null;

		boolean result = JavaArrays.isEmpty(array);

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnTrueForNonEmptyArrayOnIsNotEmpty() {
		String[] array = new String[] { "not", "empty" };

		boolean result = JavaArrays.isNotEmpty(array);

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnFalseForEmptyArrayOnIsNotEmpty() {
		String[] array = new String[] { };

		boolean result = JavaArrays.isNotEmpty(array);

		assertThat(result, equalTo(false));
	}
}
