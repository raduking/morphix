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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link GenericClass}.
 *
 * @author Radu Sebastian LAZIN
 */
class GenericClassTest {

	@Test
	void shouldThrowExceptionIfTheGenericArgumentIsNotAGenericClass() {
		Exception exception = null;
		try {
			@SuppressWarnings("unused")
			GenericClass<String> gc = new GenericClass<>() {
				// empty
			};
		} catch (Exception e) {
			exception = e;
		}

		assertThat(exception.getClass(), equalTo(ReflectionException.class));
		assertThat(exception.getMessage(),
				equalTo("Generic argument type must be a generic class (ParameterizedType), where " + String.class + " is not."));
	}

	@Test
	void shouldDetectGenericArgumentGenericType() {
		GenericClass<List<String>> gc = new GenericClass<>() {
			// empty
		};

		GenericType expected = GenericType.of(List.class, new Type[] { String.class }, null);

		Type result = gc.getGenericArgumentType();

		assertThat(result.toString(), equalTo(expected.toString()));
	}

	@Test
	void shouldDetectGenericArgumentGenericTypes() {
		GenericClass<Map<Long, String>> gc = new GenericClass<>() {
			// empty
		};

		GenericType expected = GenericType.of(Map.class, new Type[] { Long.class, String.class }, null);

		Type result = gc.getGenericArgumentType();

		assertThat(result.toString(), equalTo(expected.toString()));
	}

	@Test
	void shouldDetectGenericArgumentGenericTypesFromGenericClass() {
		GenericClass<Map<Long, String>> gc = new GenericClass<>() {
			// empty
		};

		GenericType expected = GenericType.of(gc);

		Type result = gc.getGenericArgumentType();

		assertThat(result.toString(), equalTo(expected.toString()));
	}

	@Test
	void shouldDetectGenericArgumentGenericTypesFromGenericClassWithOwnerType() {
		GenericClass<Map.Entry<Long, String>> gc = new GenericClass<>() {
			// empty
		};

		GenericType expected = GenericType.of(gc);

		Type result = gc.getGenericArgumentType();

		assertThat(result.toString(), equalTo(expected.toString()));
	}

	static class Outer<T> {

		class Inner<U> {
			// empty
		}
	}

	@Test
	void shouldDetectGenericArgumentGenericTypesFromGenericClassWithOwnerTypeAsParameterizedType() {
		GenericClass<Outer<String>.Inner<Long>> gc = new GenericClass<>() {
			// empty
		};

		GenericType expected = GenericType.of(gc);

		Type result = gc.getGenericArgumentType();

		assertThat(result.toString(), equalTo(expected.toString()));
	}

	@Test
	void shouldBuildANewGenericClassWithFactoryMethod() {
		Type type = GenericType.of(List.class, String.class);
		GenericClass<List<String>> gc = GenericClass.of(type);

		assertThat(gc.toString(), equalTo("GenericClass<java.util.List<java.lang.String>>"));
	}

	@Test
	void shouldReturnTrueOnEqualsWithTheSameObject() {
		Type type = GenericType.of(List.class, String.class);
		GenericClass<String> gc = GenericClass.of(type);

		boolean result = gc.equals(gc);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnEqualsWithObjectsThatHaveTheSameUnderlyingType() {
		Type type1 = GenericType.of(List.class, String.class);
		Type type2 = GenericType.of(List.class, String.class);
		GenericClass<List<String>> gc1 = GenericClass.of(type1);
		GenericClass<List<String>> gc2 = GenericClass.of(type2);

		boolean result = gc1.equals(gc2);

		assertTrue(result);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	void shouldReturnFalseOnEqualsWhenTheParameterIsNotAGenericClass() {
		Type type = GenericType.of(List.class, String.class);
		GenericClass<List<String>> gc = GenericClass.of(type);

		boolean result = gc.equals("x");

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWithObjectsThatHaveDifferentUnderlyingTypes() {
		Type type1 = GenericType.of(List.class, String.class);
		Type type2 = GenericType.of(Set.class, Integer.class);
		GenericClass<String> gc1 = GenericClass.of(type1);
		GenericClass<String> gc2 = GenericClass.of(type2);

		boolean result = gc1.equals(gc2);

		assertFalse(result);
	}

	@Test
	void shouldReturnTheUnderlyingTypesHashCodeOnHashCode() {
		Type type = GenericType.of(List.class, String.class);
		GenericClass<List<String>> gc = GenericClass.of(type);

		int expected = type.hashCode();
		int result = gc.hashCode();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldBeAbleToChenageTheType() {
		GenericClass<List<String>> gc1 = new GenericClass<>() {
			// empty
		};
		GenericClass<Map<Long, String>> gc2 = new GenericClass<>() {
			// empty
		};

		gc1.setGenericArgumentType(gc2.getGenericArgumentType());

		GenericType expected = GenericType.of(Map.class, new Type[] { Long.class, String.class }, null);

		Type result = gc1.getGenericArgumentType();

		assertThat(result.toString(), equalTo(expected.toString()));
	}

	@Test
	void shouldThrowExceptionIfSetTypeTriesToChangeTheTypeToANonParameterizedType() {
		GenericClass<List<String>> gc = new GenericClass<>() {
			// empty
		};

		ReflectionException e = assertThrows(ReflectionException.class, () -> gc.setGenericArgumentType(String.class));

		assertThat(e.getMessage(), equalTo("Generic argument type must be a generic class (ParameterizedType), where " + String.class + " is not."));
	}
}
