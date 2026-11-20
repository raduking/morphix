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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.morphix.lang.collections.FastArrayList;

/**
 * Test class for {@link Fields.Safe#getByPath(Object, String)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsSafeGetByPathTest {

	private static final String FIELD_VALUE = "fieldValue";

	private static final String GETTER_VALUE = "getterValue";

	private static final String NON_EXISTENT_FIELD = "$NonExistentField$";

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

	public static class WithIndexedContainers {

		public String[] tags = { "alpha", "beta" };

		public List<String> codes = new FastArrayList<>(new String[] { "x", "y" });

		public Map<String, String> attributes = new LinkedHashMap<>();

		public WithIndexedContainers() {
			attributes.put("color", "blue");
			attributes.put("size", "large");
		}
	}

	@Test
	void shouldGetFieldAtFirstLevel() {
		WithNestedField obj = new WithNestedField();

		Object result = Fields.Safe.getByPath(obj, "inner");

		assertThat(result, equalTo(obj.inner));
	}

	@Test
	void shouldGetNestedField() {
		WithNestedField obj = new WithNestedField();

		Object result = Fields.Safe.getByPath(obj, "inner.value");

		assertThat(result, equalTo(FIELD_VALUE));
	}

	@Test
	void shouldReturnNullIfFieldDoesNotExistAtFirstLevel() {
		WithNestedField obj = new WithNestedField();

		Object result = Fields.Safe.getByPath(obj, NON_EXISTENT_FIELD);

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullIfNestedFieldDoesNotExist() {
		WithNestedField obj = new WithNestedField();

		Object result = Fields.Safe.getByPath(obj, "inner." + NON_EXISTENT_FIELD);

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullIfIntermediateFieldIsNull() {
		WithNestedField obj = new WithNestedField();
		obj.inner = null;

		Object result = Fields.Safe.getByPath(obj, "inner.value");

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullForEmptyPath() {
		Object result = Fields.Safe.getByPath(new WithNestedField(), "");

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullForPathWithTrailingDot() {
		Object result = Fields.Safe.getByPath(new WithNestedField(), "inner.");

		assertThat(result, nullValue());
	}

	@Test
	void shouldReadTheFieldAndNotTheGetterOnASingleSegmentPath() {
		WithTransformingGetter obj = new WithTransformingGetter();

		Object result = Fields.Safe.getByPath(obj, "value");

		assertThat(result, equalTo(FIELD_VALUE));
	}

	@Test
	void shouldReturnNullIfObjectIsNull() {
		Object result = Fields.Safe.getByPath(null, "value");

		assertThat(result, nullValue());
	}

	@Test
	void shouldThrowWhenPathIsNull() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.Safe.getByPath(new WithNestedField(), null));
		assertThat(e.getMessage(), equalTo("Cannot resolve field path: path is null"));
	}

	@Test
	void shouldGetValueByArrayIndexPath() {
		WithIndexedContainers obj = new WithIndexedContainers();

		assertThat(Fields.Safe.getByPath(obj, "tags[1]"), equalTo("beta"));
		assertThat(Fields.Safe.getByPath(obj, "tags[0]"), equalTo("alpha"));
	}

	@Test
	void shouldGetValueByListIndexPath() {
		WithIndexedContainers obj = new WithIndexedContainers();

		assertThat(Fields.Safe.getByPath(obj, "codes[1]"), equalTo("y"));
	}

	@Test
	void shouldGetValueByMapKeyPath() {
		WithIndexedContainers obj = new WithIndexedContainers();

		assertThat(Fields.Safe.getByPath(obj, "attributes['color']"), equalTo("blue"));
		assertThat(Fields.Safe.getByPath(obj, "attributes[size]"), equalTo("large"));
	}

	@Test
	void shouldGetNestedValueAfterIndexPath() {
		WithIndexedContainers obj = new WithIndexedContainers();
		obj.tags = new String[] { "outer" };
		Wrapper wrapper = new Wrapper();
		wrapper.row = obj;

		assertThat(Fields.Safe.getByPath(wrapper, "row.tags[0]"), equalTo("outer"));
	}

	public static class Wrapper {

		public WithIndexedContainers row;
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
	@CsvSource({
			"person.name, Alice",
			"persons[0].name, Alice",
			"persons[1].name, Bob",
			"array[0], Alice",
			"array[1], Bob",
			"attributes.name, Alice",
			"attributes[name], Alice",
			"attributes['name'], Alice",
			"attributes[\"name\"], Alice",
			"attributes['first.name'], Bob",
			"attributes['0'], zero",
			"attributes[''], empty",
			"attributes.size, map size",
			"attributes['a]b'], bracket"
	})
	void shouldResolvePath(final String path, final String expected) {
		var result = Fields.Safe.getByPath(data, path);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldResolvePrimitiveArrayElement() {
		Integer result = Fields.Safe.getByPath(data, "numbers[1]");

		assertThat(result, equalTo(20));
	}

	@Test
	void shouldResolveNestedArrayElement() {
		Integer result = Fields.Safe.getByPath(data, "matrix[1][0]");

		assertThat(result, equalTo(30));
	}

	@Test
	void shouldResolveKeyContainingComma() {
		var result = Fields.Safe.getByPath(data, "attributes['a,b']");

		assertThat(result, equalTo("comma"));
	}

	@Test
	void shouldResolveKeyContainingQuote() {
		var result = Fields.Safe.getByPath(data, "attributes['a\\'b']");

		assertThat(result, equalTo("quote"));
	}

	@Test
	void shouldResolveKeyContainingBackslash() {
		var result = Fields.Safe.getByPath(data, "attributes['a\\\\b']");

		assertThat(result, equalTo("backslash"));
	}

	@Test
	void shouldResolveRootList() {
		List<Person> persons = List.of(new Person("Alice"));

		var result = Fields.Safe.getByPath(persons, "[0].name");

		assertThat(result, equalTo("Alice"));
	}

	@Test
	void shouldResolveRootMap() {
		Map<String, String> map = Map.of("name", "Alice");

		var result = Fields.Safe.getByPath(map, "['name']");

		assertThat(result, equalTo("Alice"));
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
				"attributes['name]",
				"attributes['name'",
				"attributes['name']name",
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
