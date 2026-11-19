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
	void shouldParseDotSeparatedProperties() {
		List<FieldPaths.Step> steps = FieldPaths.parse("inner.value");

		assertThat(steps.size(), equalTo(2));
		assertThat(steps.get(0).kind(), equalTo(FieldPaths.StepKind.PROPERTY));
		assertThat(steps.get(0).selector(), equalTo("inner"));
		assertThat(steps.get(1).selector(), equalTo("value"));
	}

	@Test
	void shouldParseArrayIndexAndProperty() {
		List<FieldPaths.Step> steps = FieldPaths.parse("entries[0].name");

		assertThat(steps.size(), equalTo(3));
		assertThat(steps.get(1).kind(), equalTo(FieldPaths.StepKind.INDEX));
		assertThat(steps.get(1).selector(), equalTo("0"));
		assertThat(steps.get(2).selector(), equalTo("name"));
	}

	@Test
	void shouldReturnNullForInvalidExpression() {
		assertThat(FieldPaths.parse(".inner"), nullValue());
		assertThat(FieldPaths.parse("entries[0"), nullValue());
		assertThat(FieldPaths.parse("entries[]"), nullValue());
	}

	@Test
	void shouldSplitCommaSeparatedPathsOutsideBrackets() {
		List<String> paths = FieldPaths.split("entries[0].name,data['a,b'],items[1].name");

		assertThat(paths.size(), equalTo(3));
		assertThat(paths.get(0), equalTo("entries[0].name"));
		assertThat(paths.get(1), equalTo("data['a,b']"));
		assertThat(paths.get(2), equalTo("items[1].name"));
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
