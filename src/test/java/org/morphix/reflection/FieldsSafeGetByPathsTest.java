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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test class for {@link Fields.Safe#getByPaths(Object, String...)} and {@link Fields.Safe#getByPaths(Object, String)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsSafeGetByPathsTest {

	private record Person(String name) {
		// empty
	}

	@SuppressWarnings("unused")
	private static final class Data {

		private final Person person = new Person("Alice");

		private final List<Person> persons = List.of(new Person("Alice"));

		private final Object missing = null;

		private final Map<String, String> attributes = Map.of("a,b", "commaKey");

		private final Map<String, Person> keyed = Map.of("a,b", new Person("commaKey"));
	}

	private final Data data = new Data();

	@ParameterizedTest
	@ValueSource(
		strings = {
				"person.name",
				"unknown,person.name",
				"missing.name,person.name",
				"missing[0].name,person.name",
				"persons[100].name,person.name",
				"unknown, persons[0].name",
				",,,person.name,,,",
				" , , person.name , ",
				"attributes['unknown,key'],person.name",
				"unknown , person.name",
				".bad,attributes[],person.name",
				"attributes['missing\\\\'].name,person.name",
				"attributes[foo[bar]],person.name",
				"attributes[foo['a,b']],person.name",
				"attributes[a'b'c],person.name",
				"person.name] [skip],person.name",
				"person.name] [foo[bar]],person.name"
		})
	void shouldResolveFirstNonNullPath(final String paths) {
		var result = Fields.Safe.getByPaths(data, paths);

		assertThat(result, equalTo("Alice"));
	}

	@Test
	void shouldResolveFirstNonNullPathWithVarargs() {
		var result = Fields.Safe.getByPaths(data, "unknown", "person.name");

		assertThat(result, equalTo("Alice"));
	}

	@Test
	void shouldTryNextAlternativeWhenMapRejectsStringKey() {
		Map<Integer, String> numbered = new TreeMap<>();
		numbered.put(1, "one");
		Map<String, Object> root = Map.of("numbered", numbered, "name", "Alice");

		var result = Fields.Safe.getByPaths(root, "numbered.missing,name");

		assertThat(result, equalTo("Alice"));
	}

	@Test
	void shouldResolveFieldAtFirstLevel() {
		var result = Fields.Safe.getByPaths(data, "person");

		assertThat(result, equalTo(data.person));
	}

	@ParameterizedTest
	@ValueSource(
		strings = {
				"",
				" , , ",
				"unknown",
				"missing.name",
				"persons[100].name",
				"person.name] trailing"
		})
	void shouldReturnNullWhenNoPathResolves(final String paths) {
		var result = Fields.Safe.getByPaths(data, paths);

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullWhenCommaSeparatedPathsAreNull() {
		var result = Fields.Safe.getByPaths(data, (String) null);

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullForEmptyVarargs() {
		var result = Fields.Safe.getByPaths(data, new String[] { });

		assertThat(result, nullValue());
	}

	@Test
	void shouldSkipNullPathInVarargs() {
		var result = Fields.Safe.getByPaths(data, null, "person.name");

		assertThat(result, equalTo("Alice"));
	}

	@Test
	void shouldReturnNullWhenPathsArrayIsNull() {
		var result = Fields.Safe.getByPaths(data, (String[]) null);

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullWhenOnlyPathIsNull() {
		var result = Fields.Safe.getByPaths(data, new String[] { null });

		assertThat(result, nullValue());
	}

	@ParameterizedTest
	@ValueSource(
		strings = {
				"attributes['a,b'],person.name",
				"keyed['a,b'].name,person.name"
		})
	void shouldNotSplitOnCommaInsideBrackets(final String paths) {
		var result = Fields.Safe.getByPaths(data, paths);

		assertThat(result, equalTo("commaKey"));
	}
}
