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
package org.morphix.convert.strategy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.Fields;

/**
 * Test class for {@link FinderPathStrategy}.
 *
 * @author Radu Sebastian LAZIN
 */
class FinderPathStrategyTest {

	public static class Entry {

		public static String staticName = "static";

		@SuppressWarnings("unused")
		private String name;

		private Entry(final String name) {
			this.name = name;
		}
	}

	public static class Source {

		private final Entry[] entries = { new Entry("array"), new Entry(null) };

		private final List<Entry> items = List.of(new Entry("list"));

		private final Map<String, Entry> keyed = Map.of("a,b", new Entry("map"), "key", new Entry("plain"));

		@SuppressWarnings("unused")
		private final Entry missing = null;
	}

	@Test
	void shouldFindFinalFieldAndItsOwnerThroughContainers() {
		Source source = new Source();
		FinderPathStrategy strategy = new FinderPathStrategy();

		assertField(strategy.find(source, List.of(), "entries[0].name"), source.entries[0], "array");
		assertField(strategy.find(source, List.of(), "items[0].name"), source.items.get(0), "list");
		assertField(strategy.find(source, List.of(), "keyed['a,b'].name"), source.keyed.get("a,b"), "map");
		assertField(strategy.find(source, List.of(), "keyed[key].name"), source.keyed.get("key"), "plain");
		assertField(strategy.find(source, List.of(), "keyed.key.name"), source.keyed.get("key"), "plain");
		assertField(strategy.find(source.entries, List.of(), "[0].name"), source.entries[0], "array");
	}

	@Test
	void shouldFindFirstExistingFieldAmongAlternatives() {
		Source source = new Source();
		ExtendedField field = FinderPathStrategy.getConverterFieldByPathInHierarchy(source,
				" , missing.name, entries[9].name, keyed['a,b'].name, items[0].name ");

		assertField(field, source.keyed.get("a,b"), "map");
	}

	@Test
	void shouldFindNullValuedFieldWithoutTryingFollowingAlternative() {
		Source source = new Source();
		String paths = "entries[1].name,items[0].name";

		ExtendedField field = FinderPathStrategy.getConverterFieldByPathInHierarchy(source, paths);

		assertField(field, source.entries[1], null);
		assertThat(Fields.Safe.getByPaths(source, paths), equalTo("list"));
	}

	@Test
	void shouldRejectStaticFieldsAndContainerElementsAsTerminalFields() {
		Source source = new Source();

		for (String path : List.of("entries[0].staticName", "entries[0]", "keyed['a,b']", "keyed.key",
				"missing.name", "entries[0].", "entries[0].name[0]")) {
			assertThat(path, FinderPathStrategy.getConverterFieldByPathInHierarchy(source, path),
					sameInstance(ExtendedField.EMPTY));
		}
		assertThat(FinderPathStrategy.getConverterFieldByPathInHierarchy(null, "entries[0].name"),
				sameInstance(ExtendedField.EMPTY));
		assertField(FinderPathStrategy.getConverterFieldByPathInHierarchy(source, "entries[0].staticName,items[0].name"),
				source.items.get(0), "list");
		assertField(FinderPathStrategy.getConverterFieldByPathInHierarchy(source, "entries[0],items[0].name"),
				source.items.get(0), "list");
	}

	@Test
	void shouldResolveMapKeysInsteadOfInternalFields() {
		Map<String, Entry> map = new HashMap<>();
		Entry entry = new Entry("map size");
		map.put("size", entry);

		assertField(FinderPathStrategy.getConverterFieldByPathInHierarchy(map, "size.name"), entry, "map size");
		assertThat(FinderPathStrategy.getConverterFieldByPathInHierarchy(map, "size"),
				sameInstance(ExtendedField.EMPTY));
		assertThat(Fields.Safe.getByPath(map, "size.name"), equalTo("map size"));
		assertThat(Fields.Safe.getByPath(map, "threshold"), nullValue());
	}

	private static void assertField(final ExtendedField field, final Entry owner, final String value) {
		assertThat(field.getObject(), sameInstance(owner));
		assertThat(field.getName(), equalTo("name"));
		assertThat(field.getField().getDeclaringClass(), equalTo(Entry.class));
		if (null == value) {
			assertThat(field.getFieldValue(), nullValue());
		} else {
			assertThat(field.getFieldValue(), equalTo(value));
		}
	}
}
