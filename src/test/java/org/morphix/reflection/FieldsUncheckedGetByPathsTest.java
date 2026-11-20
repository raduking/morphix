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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link Fields.Unchecked#getByPaths(Object, String)} and
 * {@link Fields.Unchecked#getByPaths(Object, String...)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsUncheckedGetByPathsTest {

	@Test
	void shouldResolveFirstPathFromCommaSeparatedAlternatives() {
		var result = Fields.Unchecked.getByPaths(Map.of("name", "Alice"), "unknown,name");

		assertThat(result, equalTo("Alice"));
	}

	@ParameterizedTest
	@MethodSource("nullInputs")
	void shouldReturnNullForNullInput(final Object root, final String paths) {
		var result = Fields.Unchecked.getByPaths(root, paths);

		assertThat(result, nullValue());
	}

	private static Stream<Arguments> nullInputs() {
		return Stream.of(
				Arguments.of((Object) null, "name"),
				Arguments.of(Map.of("name", "Alice"), (String) null));
	}

	@Test
	void shouldResolveFirstNonNullVarargsPath() {
		var result = Fields.Unchecked.getByPaths(Map.of("name", "Alice"), "unknown", null, "name");

		assertThat(result, equalTo("Alice"));
	}

	@Test
	void shouldReturnNullForEmptyVarargs() {
		var result = Fields.Unchecked.getByPaths(Map.of("name", "Alice"), new String[] { });

		assertThat(result, nullValue());
	}

	@Test
	void shouldThrowForNullVarargsArray() {
		assertThrows(NullPointerException.class, () -> Fields.Unchecked.getByPaths(Map.of("name", "Alice"), (String[]) null));
	}
}
