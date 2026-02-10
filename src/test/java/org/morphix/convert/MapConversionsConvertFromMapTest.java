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
package org.morphix.convert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link MapConversions#convertFromMap(Map, InstanceFunction)</li> </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class MapConversionsConvertFromMapTest {

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
			return Objects.hash(s);
		}
	}

	@Test
	void shouldConvertMapToOneLevelObject() {
		Map<String, Object> srcMap = Map.of("s", Integer.valueOf(1));

		Dst expected = new Dst();
		expected.s = "1";

		Dst result = MapConversions.convertFromMap(srcMap, Dst::new);

		assertThat(result, equalTo(expected));
	}

	static class DstComplex {

		public String s;

		public Dst dst;

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof DstComplex that) {
				return Objects.equals(this.s, that.s)
						&& Objects.equals(this.dst, that.dst);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(s, dst);
		}
	}

	@Test
	void shouldConvertMapToDeepObject() {
		Map<String, Object> srcMap = Map.of(
				"s", Integer.valueOf(1),
				"dst", Map.of("s", Integer.valueOf(2)));

		DstComplex expected = new DstComplex();
		expected.s = "1";
		expected.dst = new Dst();
		expected.dst.s = "2";

		DstComplex result = MapConversions.convertFromMap(srcMap, DstComplex::new);

		assertThat(result, equalTo(expected));
	}

}
