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
import static org.hamcrest.Matchers.sameInstance;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.morphix.lang.collections.FastArrayList;

/**
 * Test class for {@link FieldPathParser}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldPathParserTest {

	public static class Entry {

		public static String staticName = "static";

		public String name;
	}

	public static class WithContainers {

		public Entry[] entries = { new Entry(), new Entry() };

		public Entry nullEntry = new Entry();

		public List<Entry> items = new FastArrayList<>(new Entry[] { new Entry() });

		public Map<String, String> data = new LinkedHashMap<>();

		public WithContainers() {
			entries[0].name = "fromArray";
			entries[1].name = "second";
			nullEntry.name = null;
			items.get(0).name = "fromList";
			data.put("key", "fromMap");
			data.put("a,b", "commaKey");
			data.put("a\\", "backslash");
			data.put("a'b", "quote");
			data.put("a]b", "bracket");
			data.put("", "empty");
			data.put("0", "zero");
			data.put("say \"hi\"", "quoted");
		}
	}

	private final WithContainers root = new WithContainers();

	@Nested
	class ResolveValueTests {

		@ParameterizedTest
		@ValueSource(
			strings = {
					"",
					".entries",
					".bad",
					"entries.",
					"entries..name",
					"entries.[0]",
					"entries[0",
					"entries[]",
					"entries[0]]",
					"entries[0]name",
					"entries[2147483648]",
					"entries[-1]",
					"entries[notAnIndex]",
					"entries[0].name,items[0].name",
					"items[9].name",
					" entries[0].name",
					"entries[0].name "
			})
		void shouldReturnNullForInvalidOrUnresolvedSinglePath(final String path) {
			var result = FieldPathParser.resolveValue(root, path);

			assertThat(path, result, nullValue());
		}

		@ParameterizedTest
		@MethodSource("singlePaths")
		void shouldResolveSinglePath(final String path, final String expected) {
			var result = FieldPathParser.resolveValue(root, path);

			assertThat(result, equalTo(expected));
		}

		private static Stream<Arguments> singlePaths() {
			return Stream.of(
					Arguments.of("entries[0].name", "fromArray"),
					Arguments.of("entries[1].name", "second"),
					Arguments.of("items[0].name", "fromList"),
					Arguments.of("data['key']", "fromMap"),
					Arguments.of("data[key]", "fromMap"),
					Arguments.of("data['a,b']", "commaKey"),
					Arguments.of("data[a,b]", "commaKey"),
					Arguments.of("data['a\\\\']", "backslash"),
					Arguments.of("data['a\\'b']", "quote"),
					Arguments.of("data[\"a]b\"]", "bracket"),
					Arguments.of("data['']", "empty"),
					Arguments.of("data['0']", "zero"),
					Arguments.of("data['say \\\"hi\\\"']", "quoted"));
		}

		@ParameterizedTest
		@MethodSource("rootPaths")
		void shouldResolveRootContainer(final Object container, final String path, final Object expected) {
			var result = FieldPathParser.resolveValue(container, path);

			assertThat(result, equalTo(expected));
		}

		private static Stream<Arguments> rootPaths() {
			return Stream.of(
					Arguments.of(new int[][] { { 10, 20 }, { 30, 40 } }, "[1][0]", 30),
					Arguments.of(List.of(List.of("value")), "[0][0]", "value"),
					Arguments.of(Map.of("a.b", "value"), "['a.b']", "value"),
					Arguments.of(Map.of("a,b", "value"), "a,b", "value"));
		}

		@Test
		void shouldReturnNullForNullRoot() {
			var result = FieldPathParser.resolveValue(null, "entries[0]");

			assertThat(result, nullValue());
		}

		@Test
		void shouldResolveTerminalListElement() {
			var result = FieldPathParser.resolveValue(root, "items[0]");

			assertThat(result, equalTo(root.items.get(0)));
		}
	}

	@Nested
	class ResolveFirstValueTests {

		@ParameterizedTest
		@MethodSource("alternativePaths")
		void shouldResolveFirstMatchingAlternative(final String paths, final String expected) {
			var result = FieldPathParser.resolveFirstValue(root, paths);

			assertThat(result, equalTo(expected));
		}

		private static Stream<Arguments> alternativePaths() {
			return Stream.of(
					Arguments.of(" , missing.name, items[9].name, data['a,b'], entries[0].name, ", "commaKey"),
					Arguments.of("data['missing,key'], items[0].name", "fromList"),
					Arguments.of(".bad,entries[],entries[0]] ,items[0].name", "fromList"),
					Arguments.of("entries[2147483648],items[0].name", "fromList"),
					Arguments.of("items[0].name,entries[", "fromList"),
					Arguments.of("data['missing\\\\'],items[0].name", "fromList"),
					Arguments.of("data['a\\\\'],items[0].name", "backslash"),
					Arguments.of("data[a,b],items[0].name", "commaKey"),
					Arguments.of("  entries[0].name  ", "fromArray"),
					Arguments.of("  data['key']  ", "fromMap"),
					Arguments.of("entries[0]. name,items[0].name", "fromList"));
		}

		@ParameterizedTest
		@ValueSource(
			strings = {
					", ,",
					"data['unterminated,items[0].name"
			})
		void shouldReturnNullWhenNoAlternativeMatches(final String paths) {
			var result = FieldPathParser.resolveFirstValue(root, paths);

			assertThat(result, nullValue());
		}
	}

	@Nested
	class ResolveFirstFieldTests {

		@Test
		void shouldResolveFirstMatchingField() {
			ExtendedField field = FieldPathParser.resolveFirstField(root,
					" , missing.name, items[9].name, entries[0].name ");

			assertThat(field.getObject(), equalTo(root.entries[0]));
			assertThat(field.getName(), equalTo("name"));
			assertThat(field.getFieldValue(), equalTo("fromArray"));
		}

		@Test
		void shouldResolveFieldWithNullValueWithoutEvaluatingNextAlternative() {
			ExtendedField field = FieldPathParser.resolveFirstField(root, "nullEntry.name,entries[0].name");

			assertThat(field.getObject(), equalTo(root.nullEntry));
			assertThat(field.getName(), equalTo("name"));
			assertThat(field.getFieldValue(), nullValue());
		}

		@ParameterizedTest
		@ValueSource(
			strings = {
					"",
					", ,",
					"missing.name,items[9].name",
					"items[0]",
					"data['key']",
					"entries[0].staticName"
			})
		void shouldReturnEmptyWhenNoAlternativeMatches(final String paths) {
			ExtendedField field = FieldPathParser.resolveFirstField(root, paths);

			assertThat(field, sameInstance(ExtendedField.EMPTY));
		}

		@Test
		void shouldReturnEmptyForNullRoot() {
			ExtendedField field = FieldPathParser.resolveFirstField(null, "entries[0].name");

			assertThat(field, sameInstance(ExtendedField.EMPTY));
		}

		@Test
		void shouldReturnEmptyForNullPaths() {
			ExtendedField field = FieldPathParser.resolveFirstField(root, null);

			assertThat(field, sameInstance(ExtendedField.EMPTY));
		}
	}
}
