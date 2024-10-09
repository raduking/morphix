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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link GenericClass}.
 *
 * @author Radu Sebastian LAZIN
 */
class GenericClassTest {

	@Test
	void shouldDetectGenericArgumentType() {
		GenericClass<String> gc = new GenericClass<>() {
			// empty
		};

		assertThat(gc.getGenericArgumentType(), equalTo(String.class));
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

}
