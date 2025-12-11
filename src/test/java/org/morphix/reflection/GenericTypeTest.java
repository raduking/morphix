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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	void shouldReturnTrueOnEqualsForOtherParameterizedTypeWithEqualFields() {
		ParameterizedType type1 = GenericType.of(String.class, new Type[] { Integer.class }, Long.class);
		ParameterizedType type2 = new ParameterizedType() {

			@Override
			public Type[] getActualTypeArguments() {
				return new Type[] { Integer.class };
			}

			@Override
			public Type getRawType() {
				return String.class;
			}

			@Override
			public Type getOwnerType() {
				return Long.class;
			}
		};

		boolean result = type1.equals(type2);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnEqualsForJavaParameterizedTypeImpl() {
		ParameterizedType type1 = GenericType.of(List.class, new Type[] { Integer.class }, null);

		Class<?> parameterizedTypeClass = Classes.getOne("sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl");
		Method makeMethod = Methods.getOneDeclared("make", parameterizedTypeClass, Class.class, Type[].class, Type.class);
		ParameterizedType type2 = Methods.IgnoreAccess.invoke(makeMethod, null,
				List.class,
				new Type[] { Integer.class },
				null);

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
		int index = -1;
		ReflectionException e = assertThrows(ReflectionException.class, () -> GenericType.getGenericParameterType(ArrayList.class, index));

		assertThat(e.getMessage(), equalTo("Generic parameter type index cannot be negative, received: " + index));
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
		int index = 0;
		ReflectionException e = assertThrows(ReflectionException.class, () -> GenericType.getGenericParameterType(String.class, index));

		assertThat(e.getMessage(), equalTo(String.class.getCanonicalName() + " is not a generic class"));
	}

	@Test
	void shouldFailToExtractArgumentTypeForWrongGenericTypeIndex() {
		int index = 1;
		ReflectionException e = assertThrows(ReflectionException.class, () -> GenericType.getGenericParameterType(ArrayList.class, index));

		assertThat(e.getMessage(), equalTo("Cannot extract generic parameter type at index " + index +
				" from " + ArrayList.class.getCanonicalName() + " because it has only 1 generic parameter(s)"));
	}

	@Test
	void shouldThrowExceptionWhenGenericTypeCannotBeBuiltFromGenericClass() {
		GenericClass<List<String>> gc = new GenericClass<>() {
			// empty
		};
		Fields.IgnoreAccess.set(gc, "type", String.class);

		ReflectionException e = assertThrows(ReflectionException.class, () -> GenericType.of(gc));

		assertThat(e.getMessage(), equalTo("Cannot build GenericType from " + String.class +
				" because it is not a " + ParameterizedType.class));
	}
}
