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
package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test class for {@link Fields.Safe#getByPath(Object, String)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsSafeGetByPathTest {

	private static final String FIELD_VALUE = "fieldValue";

	private static final String GETTER_VALUE = "getterValue";

	private final Data data = new Data();

	public static class WithTransformingGetter {

		public String value = FIELD_VALUE;

		public String getValue() {
			return GETTER_VALUE;
		}
	}

	public static class WithNestedField {

		public WithTransformingGetter inner = new WithTransformingGetter();
	}

	@Test
	void shouldGetFieldAtFirstLevel() {
		WithNestedField obj = new WithNestedField();

		Object result = Fields.Safe.getByPath(obj, "inner");

		assertThat(result, equalTo(obj.inner));
	}

	@ParameterizedTest
	@MethodSource("getterPaths")
	void shouldReadTheFieldAndNotTheGetter(final Object root, final String path) {
		var result = Fields.Safe.getByPath(root, path);

		assertThat(result, equalTo(FIELD_VALUE));
	}

	private static Stream<Arguments> getterPaths() {
		return Stream.of(
				Arguments.of(new WithTransformingGetter(), "value"),
				Arguments.of(new WithNestedField(), "inner.value"));
	}

	@Test
	void shouldReturnNullIfObjectIsNull() {
		Object result = Fields.Safe.getByPath(null, "value");

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullWhenPathIsNull() {
		var result = Fields.Safe.getByPath(new WithNestedField(), null);

		assertThat(result, nullValue());
	}

	private record Person(String name) {
		// empty
	}

	@SuppressWarnings("unused")
	private static final class Data {

		private final Person person = new Person("Alice");

		private final List<Person> persons = List.of(
				new Person("Alice"),
				new Person("Bob"));

		private final String[] array = { "Alice", "Bob" };

		private final int[] numbers = { 10, 20, 30 };

		private final int[][] matrix = {
				{ 10, 20 },
				{ 30, 40 }
		};

		private final Map<String, Object> attributes = Map.of(
				"name", "Alice",
				"first.name", "Bob",
				"a,b", "comma",
				"a]b", "bracket",
				"a'b", "quote",
				"a\\b", "backslash",
				"0", "zero",
				"", "empty",
				"size", "map size");

		private final Object missing = null;
	}

	@ParameterizedTest
	@MethodSource("resolvedPaths")
	void shouldResolvePath(final String path, final Object expected) {
		var result = Fields.Safe.getByPath(data, path);

		assertThat(result, equalTo(expected));
	}

	private static Stream<Arguments> resolvedPaths() {
		return Stream.of(
				Arguments.of("person.name", "Alice"),
				Arguments.of("persons[0]", new Person("Alice")),
				Arguments.of("persons[0].name", "Alice"),
				Arguments.of("persons[1].name", "Bob"),
				Arguments.of("array[0]", "Alice"),
				Arguments.of("array[1]", "Bob"),
				Arguments.of("numbers[1]", 20),
				Arguments.of("matrix[1][0]", 30),
				Arguments.of("attributes.name", "Alice"),
				Arguments.of("attributes[name]", "Alice"),
				Arguments.of("attributes['name']", "Alice"),
				Arguments.of("attributes[\"name\"]", "Alice"),
				Arguments.of("attributes['first.name']", "Bob"),
				Arguments.of("attributes['0']", "zero"),
				Arguments.of("attributes['']", "empty"),
				Arguments.of("attributes.size", "map size"),
				Arguments.of("attributes['a]b']", "bracket"),
				Arguments.of("attributes['a,b']", "comma"),
				Arguments.of("attributes['a\\'b']", "quote"),
				Arguments.of("attributes['a\\\\b']", "backslash"));
	}

	@ParameterizedTest
	@MethodSource("rootPaths")
	void shouldResolveRootContainer(final Object root, final String path, final Object expected) {
		var result = Fields.Safe.getByPath(root, path);

		assertThat(result, equalTo(expected));
	}

	private static Stream<Arguments> rootPaths() {
		return Stream.of(
				Arguments.of(List.of(new Person("Alice")), "[0].name", "Alice"),
				Arguments.of(Map.of("name", "Alice"), "['name']", "Alice"),
				Arguments.of(Map.of("a,b", "Alice"), "a,b", "Alice"));
	}

	@ParameterizedTest
	@ValueSource(
		strings = {
				"",
				".",
				".person",
				"person.",
				"person..name",
				"person.[0]",
				"persons[]",
				"persons[",
				"persons[0",
				"persons[0]]",
				"persons[-1]",
				"persons[100]",
				"persons[2147483648]",
				"persons[0]name",
				"persons[0].",
				"persons[[0]]",
				"attributes['name]",
				"attributes['name'",
				"attributes['unfinished\\",
				"attributes['name']name",
				"attributes[a'b'c]",
				"attributes[unknown]",
				"missing.name",
				"missing[0]",
				"person.unknown",
				"unknown"
		})
	void shouldReturnNullForInvalidOrUnresolvedPath(final String path) {
		var result = Fields.Safe.getByPath(data, path);

		assertNull(result);
	}
}
