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
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link FieldPaths}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldPathsTest {

	@ParameterizedTest
	@MethodSource("invalidProperties")
	void shouldReturnNullForInvalidProperty(final Object obj, final String name) {
		var result = FieldPaths.readProperty(obj, name);

		assertThat(result, nullValue());
	}

	private static Stream<Arguments> invalidProperties() {
		return Stream.of(
				Arguments.of(null, "key"),
				Arguments.of(new Object(), null),
				Arguments.of(new Object(), ""));
	}

	@Test
	void shouldReturnNullForNullMapKey() {
		var result = FieldPaths.readMapKey(Map.of("key", "value"), null);

		assertThat(result, nullValue());
	}
}
