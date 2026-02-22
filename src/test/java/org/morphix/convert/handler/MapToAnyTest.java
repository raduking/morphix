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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.morphix.convert.FieldHandlerResult.SKIPPED;
import static org.morphix.convert.MapConversions.convertFromMap;
import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.morphix.convert.Conversions;
import org.morphix.convert.FieldHandlerContext;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.lang.function.InstanceFunction;

/**
 * Test class for {@link MapToAny}.
 *
 * @author Radu Sebastian LAZIN
 */
class MapToAnyTest {

	private static final String TEST_STRING = "testString";
	private static final Long TEST_LONG = 11L;
	private static final int SIZE = 5;

	public static class Dst1 {
		String x;
		Long y;
		BigInteger z;
	}

	@Test
	void shouldSimpleConvertFromMap() {
		Map<String, Object> srcMap = new HashMap<>();
		srcMap.put("x", TEST_STRING);
		srcMap.put("y", TEST_LONG);
		srcMap.put("z", BigInteger.TEN);

		Dst1 dst1 = convertFromMap(srcMap, Dst1::new);

		assertThat(dst1.x, equalTo(TEST_STRING));
		assertThat(dst1.y, equalTo(TEST_LONG));
		assertThat(dst1.z, equalTo(BigInteger.TEN));
	}

	public static class Dst2 {
		String x;
		String y;
		String z;
	}

	@Test
	void shouldConvertFromMap() {
		Map<String, Object> srcMap = new HashMap<>();
		srcMap.put("x", TEST_STRING);
		srcMap.put("y", TEST_LONG);
		srcMap.put("z", BigInteger.TEN);

		Dst2 dst2 = convertFromMap(srcMap, Dst2::new);

		assertThat(dst2.x, equalTo(TEST_STRING));
		assertThat(dst2.y, equalTo(TEST_LONG.toString()));
		assertThat(dst2.z, equalTo(BigInteger.TEN.toString()));
	}

	@Test
	void shouldConvertFromMapWithLambdaExtraConvertFunction() {
		Map<String, Object> srcMap = new HashMap<>();
		srcMap.put("a", TEST_STRING);
		srcMap.put("y", TEST_LONG);
		srcMap.put("c", BigInteger.TEN);

		Dst2 dst2 = convertFromMap(srcMap, Dst2::new, (map, dst) -> {
			dst.x = (String) map.get("a");
			dst.z = map.get("c").toString();
		});

		assertThat(dst2.x, equalTo(TEST_STRING));
		assertThat(dst2.y, equalTo(TEST_LONG.toString()));
		assertThat(dst2.z, equalTo(BigInteger.TEN.toString()));
	}

	static class Src {

		@SuppressWarnings("unused")
		private final Map<String, String> map = new LinkedHashMap<>();
	}

	static class Dst {

		private final HashMap<String, String> map = new HashMap<>();

		void put(final String key, final String value) {
			map.put(key, value);
		}

		Map<String, String> getMap() {
			return map;
		}
	}

	@Test
	void shouldConvertFromLinkedHashMapToHashMap() {
		Src srcMap = new Src();
		Dst old = new Dst();

		old.put("aaa", "111");
		old.put("bbb", "222");

		Dst dst = Conversions.convertFrom(srcMap, InstanceFunction.to(old));

		assertThat(dst.getMap().size(), equalTo(0));
		assertThat(dst.getMap().keySet(), empty());
		assertThat(dst.getMap().containsKey("aaa"), is(false));
		assertThat(dst.getMap().containsKey("bbb"), is(false));
		assertThat(dst.getMap().get("aaa"), is(nullValue()));
		assertThat(dst.getMap().get("bbb"), is(nullValue()));
	}

	public static class Dst3 {
		String x;
		String y;
		List<String> z;

		public List<String> getZ() {
			return z;
		}
	}

	@Test
	void shouldConvertFromMapWhenDestinationHasList() {
		Map<String, Object> srcMap = new HashMap<>();
		srcMap.put("x", TEST_STRING + 1);
		srcMap.put("y", TEST_STRING + 2);
		srcMap.put("z", TEST_STRING + 3);

		Dst3 dst3 = convertFromMap(srcMap, Dst3::new);

		assertThat(dst3.x, equalTo(TEST_STRING + 1));
		assertThat(dst3.y, equalTo(TEST_STRING + 2));
		assertThat(dst3.z, hasSize(1));
		assertThat(dst3.z.get(0), equalTo(TEST_STRING + 3));
	}

	public static class Dst4 {
		List<String> z;

		public List<String> getZ() {
			return z;
		}
	}

	@Test
	void shouldConvertFromMapWhenDestinationHasOneList() {
		Map<String, Object> srcMap = new HashMap<>();
		srcMap.put("z", TEST_STRING);

		Dst4 dst4 = convertFromMap(srcMap, Dst4::new);

		assertThat(dst4.z, hasSize(1));
		assertThat(dst4.z.get(0), equalTo(TEST_STRING));
	}

	@Test
	void shouldConvertFromMapWhenSourceHasArrayAndDestinationHasList() {
		Map<String, Object> srcMap = new HashMap<>();
		String[] stringArray = IntStream.range(0, SIZE).boxed().map(i -> TEST_STRING + i).toArray(String[]::new);
		srcMap.put("z", stringArray);

		Dst4 dst4 = convertFromMap(srcMap, Dst4::new);

		assertThat(dst4.z, hasSize(SIZE));
		for (int i = 0; i < SIZE; ++i) {
			assertThat(dst4.z.get(i), equalTo(TEST_STRING + i));
		}
	}

	@Test
	void shouldReturnSkipOnHandleIfSourceIsNull() {
		FieldHandlerResult result = new MapToAny().handle(of((Field) null), of((Field) null), new FieldHandlerContext());

		assertThat(result, equalTo(SKIPPED));
	}

}
