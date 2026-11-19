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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.morphix.lang.collections.FastArrayList;

/**
 * Test class for {@link FieldPaths}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldPathsTest {

	public static class Entry {

		public String name;
	}

	public static class WithContainers {

		public Entry[] entries = { new Entry() };

		public List<Entry> items = new FastArrayList<>(new Entry[] { new Entry() });

		public Map<String, String> data = new LinkedHashMap<>();

		public WithContainers() {
			entries[0].name = "fromArray";
			items.get(0).name = "fromList";
			data.put("key", "fromMap");
			data.put("a,b", "commaKey");
		}
	}

	@Test
	void shouldRejectInvalidExpressions() {
		WithContainers root = new WithContainers();

		for (String path : List.of(".entries", "entries.", "entries..name", "entries.[0]", "entries[0",
				"entries[]", "entries[0]]", "entries[0]name", "entries[2147483648]", "entries[0].name,items[0].name")) {
			assertThat(path, FieldPaths.resolveValue(root, path), nullValue());
		}
	}

	@Test
	void shouldResolveAlternativesWithoutSplittingQuotedKeys() {
		WithContainers root = new WithContainers();

		assertThat(FieldPaths.resolveValues(root, " , missing.name, items[9].name, data['a,b'], entries[0].name, "),
				equalTo("commaKey"));
		assertThat(FieldPaths.resolveValues(root, "data['missing,key'], items[0].name"), equalTo("fromList"));
		assertThat(FieldPaths.resolveValues(root, ", ,"), nullValue());
	}

	@Test
	void shouldRecoverFromMalformedAlternatives() {
		WithContainers root = new WithContainers();

		assertThat(FieldPaths.resolveValues(root, ".bad,entries[],entries[0]] ,items[0].name"), equalTo("fromList"));
		assertThat(FieldPaths.resolveValues(root, "entries[2147483648],items[0].name"), equalTo("fromList"));
		assertThat(FieldPaths.resolveValues(root, "data['unterminated,items[0].name"), nullValue());
		assertThat(FieldPaths.resolveValues(root, "items[0].name,entries["), equalTo("fromList"));
	}

	@Test
	void shouldResolveEscapedKeysAndFollowingAlternatives() {
		WithContainers root = new WithContainers();
		root.data.put("a\\", "backslash");
		root.data.put("a'b", "quote");
		root.data.put("a]b", "bracket");
		root.data.put("", "empty");
		root.data.put("0", "zero");
		root.data.put("a,b", "comma");

		assertThat(FieldPaths.resolveValue(root, "data['a\\\\']"), equalTo("backslash"));
		assertThat(FieldPaths.resolveValues(root, "data['missing\\\\'],items[0].name"), equalTo("fromList"));
		assertThat(FieldPaths.resolveValues(root, "data['a\\\\'],items[0].name"), equalTo("backslash"));
		assertThat(FieldPaths.resolveValue(root, "data['a\\'b']"), equalTo("quote"));
		assertThat(FieldPaths.resolveValue(root, "data[\"a]b\"]"), equalTo("bracket"));
		assertThat(FieldPaths.resolveValue(root, "data['']"), equalTo("empty"));
		assertThat(FieldPaths.resolveValue(root, "data['0']"), equalTo("zero"));
		assertThat(FieldPaths.resolveValue(root, "data[a,b]"), equalTo("comma"));
		assertThat(FieldPaths.resolveValues(root, "data[a,b],items[0].name"), equalTo("comma"));
	}

	@Test
	void shouldResolveRootContainersAndChainedIndices() {
		assertThat(FieldPaths.resolveValue(new int[][] { { 10, 20 }, { 30, 40 } }, "[1][0]"), equalTo(30));
		assertThat(FieldPaths.resolveValue(List.of(List.of("value")), "[0][0]"), equalTo("value"));
		assertThat(FieldPaths.resolveValue(Map.of("a.b", "value"), "['a.b']"), equalTo("value"));
		assertThat(FieldPaths.resolveValue(Map.of("a,b", "value"), "a,b"), equalTo("value"));
	}

	@Test
	void shouldTrimOnlyCompleteAlternatives() {
		WithContainers root = new WithContainers();

		assertThat(FieldPaths.resolveValues(root, "  entries[0].name  "), equalTo("fromArray"));
		assertThat(FieldPaths.resolveValues(root, "  data['key']  "), equalTo("fromMap"));
		assertThat(FieldPaths.resolveValue(root, " entries[0].name"), nullValue());
		assertThat(FieldPaths.resolveValue(root, "entries[0].name "), nullValue());
		assertThat(FieldPaths.resolveValues(root, "entries[0]. name,items[0].name"), equalTo("fromList"));
	}

	@Test
	void shouldResolveArrayListAndMapPaths() {
		WithContainers root = new WithContainers();

		assertThat(FieldPaths.resolveValue(root, "entries[0].name"), equalTo("fromArray"));
		assertThat(FieldPaths.resolveValue(root, "items[0].name"), equalTo("fromList"));
		assertThat(FieldPaths.resolveValue(root, "data['key']"), equalTo("fromMap"));
		assertThat(FieldPaths.resolveValue(root, "data['a,b']"), equalTo("commaKey"));
		assertThat(FieldPaths.resolveValue(root, "data[key]"), equalTo("fromMap"));
		assertThat(FieldPaths.resolveValue(root, "items[9].name"), nullValue());
	}

	@Test
	void shouldReturnNullForEmptyOrInvalidResolve() {
		WithContainers root = new WithContainers();

		assertThat(FieldPaths.resolveValue(root, ""), nullValue());
		assertThat(FieldPaths.resolveValue(root, ".bad"), nullValue());
		assertThat(FieldPaths.resolveValue(root, "entries[-1]"), nullValue());
		assertThat(FieldPaths.resolveValue(null, "entries[0]"), nullValue());
		assertThat(FieldPaths.resolveValue(root, "entries[notAnIndex]"), nullValue());
		assertThat(FieldPaths.resolveValue(root, "items[0]"), equalTo(root.items.get(0)));
	}

	@Test
	void shouldUnescapeQuotedMapKeys() {
		WithContainers root = new WithContainers();
		root.data.put("say \"hi\"", "quoted");

		assertThat(FieldPaths.resolveValue(root, "data['say \\\"hi\\\"']"), equalTo("quoted"));
	}

	@Test
	void shouldResolveNestedArrayIndices() {
		WithContainers root = new WithContainers();
		root.entries = new Entry[] { new Entry(), new Entry() };
		root.entries[1].name = "second";

		assertThat(FieldPaths.resolveValue(root, "entries[1].name"), equalTo("second"));
	}
}
