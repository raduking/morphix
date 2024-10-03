package org.morphix;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.morphix.Conversion.convertFrom;

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
class ConverterMapToMapWithIterablesTest {

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
