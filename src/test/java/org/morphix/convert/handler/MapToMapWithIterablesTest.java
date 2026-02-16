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
package org.morphix.convert.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.morphix.convert.Conversions.convertFrom;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for map conversions.
 *
 * @author Radu Sebastian LAZIN
 */
class MapToMapWithIterablesTest {

	public static class SrcWithGenericMapValues {
		private Map<String, List<LocalDateTime>> map;

		public Map<String, List<LocalDateTime>> getMap() {
			return map;
		}
	}

	public static class DestWithGenericMapValues {
		private Map<String, List<String>> map;

		public Map<String, List<String>> getMap() {
			return map;
		}
	}

	@Test
	void shouldConvertMapWithGenericValues() {
		SrcWithGenericMapValues src = new SrcWithGenericMapValues();
		src.map = new HashMap<>();
		LocalDateTime value = LocalDateTime.now();
		src.map.put("aa", Collections.singletonList(value));

		DestWithGenericMapValues dest = convertFrom(src, DestWithGenericMapValues::new);

		assertThat(dest.map, notNullValue());
		assertThat(dest.map.keySet(), hasSize(1));
		assertThat(dest.map.keySet(), contains("aa"));
		assertThat(dest.map.get("aa"), containsInAnyOrder(value.toString()));
	}
}
