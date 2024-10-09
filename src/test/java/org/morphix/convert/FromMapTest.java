/*
 * Copyright 2025 the original author or authors.
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
package org.morphix.convert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.MapConversions.convertMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Test class for map conversions.
 *
 * @author Radu Sebastian LAZIN
 */
class FromMapTest {

	private static final int SIZE = 10;

	static class Src {
		public Integer s;

		public Src(final Integer s) {
			this.s = s;
		}

		public Integer getS() {
			return s;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof Src src) {
				return Objects.equals(s, src.s);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return s == null ? super.hashCode() : s.hashCode();
		}
	}

	static class Dst {
		public String s;

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof Dst dst) {
				return Objects.equals(s, dst.s);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return s == null ? super.hashCode() : s.hashCode();
		}
	}

	@Test
	void shouldConvertMapToMapWithInstanceFunctions() {
		List<Src> list = IntStream.range(0, SIZE).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Map<String, Dst> result = convertMap(srcMap, String::new, Dst::new).toMap();

		assertThat(result.entrySet(), hasSize(SIZE));

		for (Src src : list) {
			String s = String.valueOf(src.getS());
			Dst dst = result.get(s);
			assertThat(dst.s, equalTo(s));
		}
	}

	@Test
	void shouldConvertMapToMapWithConverterFunctions() {
		List<Src> list = IntStream.range(0, SIZE).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Map<String, Dst> result = convertMap(srcMap, String::valueOf, src -> convertFrom(src, Dst.class)).toMap();

		assertThat(result.entrySet(), hasSize(SIZE));

		for (Src src : list) {
			String s = String.valueOf(src.getS());
			Dst dst = result.get(s);
			assertThat(dst.s, equalTo(s));
		}
	}

}
