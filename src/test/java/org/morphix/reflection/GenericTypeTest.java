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
package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link GenericType}.
 *
 * @author Radu Sebastian LAZIN
 */
class GenericTypeTest {

	@Test
	void shouldSetAllFields() {
		ParameterizedType type = GenericType.of(String.class, new Type[] { Integer.class }, Long.class);

		assertThat(type.getRawType(), equalTo(String.class));
		assertThat(type.getActualTypeArguments(), equalTo(new Type[] { Integer.class }));
		assertThat(type.getOwnerType(), equalTo(Long.class));
	}

	@Test
	void shouldReturnTrueOnEqualsWhenAllFieldsAreEqual() {
		ParameterizedType type1 = GenericType.of(String.class, new Type[] { Integer.class }, Long.class);
		ParameterizedType type2 = GenericType.of(String.class, new Type[] { Integer.class }, Long.class);

		boolean result = type1.equals(type2);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnEqualsForTheSameInstance() {
		ParameterizedType type = GenericType.of(String.class, new Type[] { Integer.class }, Long.class);

		boolean result = type.equals(type);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenRawTypeIsNotEqual() {
		ParameterizedType type1 = GenericType.of(String.class, new Type[] { Integer.class }, Long.class);
		ParameterizedType type2 = GenericType.of(Integer.class, new Type[] { Integer.class }, Long.class);

		boolean result = type1.equals(type2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenRawTypeActualArgumentTypeIsNotEqual() {
		ParameterizedType type1 = GenericType.of(String.class, new Type[] { Integer.class }, Long.class);
		ParameterizedType type2 = GenericType.of(String.class, new Type[] { Long.class }, Long.class);

		boolean result = type1.equals(type2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenOwnerTypeIsNotEqual() {
		ParameterizedType type1 = GenericType.of(String.class, new Type[] { Integer.class }, Long.class);
		ParameterizedType type2 = GenericType.of(String.class, new Type[] { Integer.class }, String.class);

		boolean result = type1.equals(type2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenOtherIsNotParameterizedType() {
		ParameterizedType type = GenericType.of(String.class, new Type[] { Integer.class }, Long.class);
		Type other = Object.class;

		boolean result = type.equals(other);

		assertFalse(result);
	}

	@Test
	void shouldBuildHashCodeFromAllMembers() {
		ParameterizedType type = GenericType.of(String.class, new Type[] { Integer.class }, Long.class);

		int hash = Arrays.hashCode(new Type[] { Integer.class }) ^
				Objects.hashCode(Long.class) ^
				Objects.hashCode(String.class);

		assertThat(type.hashCode(), equalTo(hash));
	}

	@Test
	void shouldThrowExceptionForNegativeIndex() {
		assertThrows(ReflectionException.class, () -> GenericType.getGenericParameterType(ArrayList.class, -1));
	}

	@Test
	void shouldReturnTrueForGenericClass() {
		boolean result = GenericType.isGenericClass(ArrayList.class);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseForNonGenericClass() {
		boolean result = GenericType.isGenericClass(Object.class);

		assertFalse(result);
	}

	@Test
	void shouldFailToExtractArgumentTypeForNonGenericClasses() {
		assertThrows(ReflectionException.class, () -> GenericType.getGenericParameterType(String.class, 0));
	}

	@Test
	void shouldFailToExtractArgumentTypeForWrongGenericTypeIndex() {
		assertThrows(ReflectionException.class, () -> GenericType.getGenericParameterType(ArrayList.class, 1));
	}

	@Test
	void shouldThrowExceptionWhenGenericTypeCannotBeBuiltFromGenericClass() {
		GenericClass<String> gc = new GenericClass<>() {
			// empty
		};

		assertThrows(IllegalArgumentException.class, () -> GenericType.of(gc));
	}
}
