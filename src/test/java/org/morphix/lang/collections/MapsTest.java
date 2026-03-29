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
package org.morphix.lang.collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Maps}.
 *
 * @author Radu Sebastian LAZIN
 */
class MapsTest {

	private static final String KEY = "key";
	private static final String VALUE = "value";
	private static final UnaryOperator<String> TO_UPPER = String::toUpperCase;

	@Nested
	class SafeTests {

		@Test
		void shouldReturnEmptyMapIfMapIsNullOnSafe() {
			var result = Maps.safe(null);

			assertThat(result, equalTo(Collections.emptyMap()));
		}

		@Test
		void shouldReturnTheMapIfMapIsNotNullOnSafe() {
			var map = Map.of();

			var result = Maps.safe(map);

			assertThat(result, equalTo(map));
		}
	}

	@Nested
	class GetOrDefaultTests {

		@Test
		void shouldReturnKeyIfMapHasIt() {
			var map = Map.of(KEY, VALUE);

			Object value = Maps.getOrDefault(map, KEY, v -> v, (String) null);

			assertThat(value, equalTo(VALUE));
		}

		@Test
		void shouldReturnDefaultValueIfMapDoesNotHaveIt() {
			var map = Map.of(KEY, VALUE);

			Object value = Maps.getOrDefault(map, VALUE, v -> v, KEY);

			assertThat(value, equalTo(KEY));
		}
	}

	@Nested
	class MultiValueMapTests {

		@Test
		void shouldReturnAMultiValueMapFromAMap() {
			Map<String, String> map = Map.of(KEY, VALUE);

			Map<String, List<String>> multiMap = Maps.multiValueMap(map);

			assertThat(multiMap.get(KEY), equalTo(List.of(VALUE)));
		}

		@Test
		void shouldReturnAMutableMultiValueMapFromNullMap() {
			Map<String, List<String>> multiMap = Maps.multiValueMap(null);

			assertThat(multiMap.entrySet(), hasSize(0));
			assertTrue(Maps.isEmpty(multiMap));

			assertDoesNotThrow(() -> multiMap.put(KEY, List.of(VALUE)));
			assertTrue(Maps.isNotEmpty(multiMap));
		}
	}

	@Nested
	class EmptyTests {

		@Test
		void shouldReturnTrueOnIsEmptyForEmptyMap() {
			assertTrue(Maps.isEmpty(Map.of()));
		}

		@Test
		void shouldReturnFalseOnIsNotEmptyForEmptyMap() {
			assertFalse(Maps.isNotEmpty(Map.of()));
		}

		@Test
		void shouldReturnTrueOnIsEmptyForNullMap() {
			assertTrue(Maps.isEmpty(null));
		}

		@Test
		void shouldReturnFalseOnIsNotEmptyForNullMap() {
			assertFalse(Maps.isNotEmpty(null));
		}
	}

	@Nested
	class ConvertKeysTests {

		@Test
		void shouldReturnEmptyMapWhenInputMapIsNull() {
			Map<String, String> result = Maps.convertKeys(null, TO_UPPER);

			assertThat(result, is(anEmptyMap()));
		}

		@Test
		void shouldReturnEmptyMapWhenInputMapIsEmpty() {
			Map<String, String> result = Maps.convertKeys(Map.of(), TO_UPPER);

			assertThat(result, is(anEmptyMap()));
		}

		@Test
		void shouldConvertKeysForSimpleMap() {
			Map<String, Integer> map = Map.of("a", 1, "b", 2);

			Map<String, Integer> result = Maps.convertKeys(map, TO_UPPER);

			assertThat(result, hasEntry("A", 1));
			assertThat(result, hasEntry("B", 2));
			assertThat(result.size(), is(2));
		}

		@Test
		void shouldConvertKeysRecursivelyForNestedMaps() {
			Map<String, Object> nested = new LinkedHashMap<>();
			nested.put("inner", Map.of("key", "value"));

			Map<String, Object> result = Maps.convertKeys(nested, TO_UPPER);

			Map<?, ?> innerMap = (Map<?, ?>) result.get("INNER");

			assertThat(innerMap, hasEntry("KEY", "value"));
		}

		@Test
		void shouldNotConvertNestedMapWhenNestedMapIsEmpty() {
			Map<String, Object> nested = new LinkedHashMap<>();
			nested.put("inner", Map.of());

			Map<String, Object> result = Maps.convertKeys(nested, TO_UPPER);

			Map<?, ?> innerMap = (Map<?, ?>) result.get("INNER");

			assertThat(innerMap, is(anEmptyMap()));
		}

		@Test
		void shouldNotConvertNestedMapWhenKeyTypesAreDifferent() {
			Map<Object, Object> nestedInner = new LinkedHashMap<>();
			nestedInner.put(1, "value"); // Integer key

			Map<String, Object> nested = new LinkedHashMap<>();
			nested.put("inner", nestedInner);

			Map<String, Object> result = Maps.convertKeys(nested, TO_UPPER);

			Map<?, ?> innerMap = (Map<?, ?>) result.get("INNER");

			// Keys should remain unchanged
			assertThat(innerMap, hasEntry(1, "value"));
		}

		@Test
		void shouldLeaveNonMapValuesUnchanged() {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("key", 123);

			Map<String, Object> result = Maps.convertKeys(map, TO_UPPER);

			assertThat(result, hasEntry("KEY", 123));
		}
	}
}
