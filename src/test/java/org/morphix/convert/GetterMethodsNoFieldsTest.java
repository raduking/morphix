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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.convert.Converter.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Getters with no fields test.
 *
 * @author Radu Sebastian LAZIN
 */
class GetterMethodsNoFieldsTest {

	static class B {
		String x;
		String y;
	}

	static class C {

		private Map<String, String> map = new HashMap<>();

		public void setMap(final Map<String, String> map) {
			this.map = map;
		}

		public final Map<String, String> getMap() {
			return map;
		}
	}

	static class D {

		private List<B> list = new ArrayList<>();

		public Map<String, String> getMap() {
			Map<String, String> map = new HashMap<>();
			for (B b : list) {
				map.put(b.x, b.y);
			}
			return map;
		}

		public void setMap(final Map<String, String> map) {
			this.list = new ArrayList<>();
			for (Map.Entry<String, String> entry : map.entrySet()) {
				B b = new B();
				b.x = entry.getKey();
				b.y = entry.getValue();
				list.add(b);
			}
		}
	}

	@Test
	void shouldConvertMapToListThroughGetters() {
		C c = new C();
		c.map = new HashMap<>();
		c.map.put("a", "b");

		D result = convert(c).to(D::new);

		assertThat(result.list, hasSize(1));
		assertThat(result.list.get(0).x, equalTo("a"));
		assertThat(result.list.get(0).y, equalTo("b"));
	}

	@Test
	void shouldConvertListToMapThroughGetters() {
		D d = new D();
		d.list = new ArrayList<>();
		B b = new B();
		b.x = "a";
		b.y = "b";
		d.list.add(b);

		C result = convert(d).to(C::new);

		assertThat(result.map.size(), equalTo(1));
		assertThat(result.map.get("a"), equalTo("b"));
	}

	public static class Src {

		String b;

		public String getA() {
			return b;
		}
	}

	static class Dst {

		String c;

		public String getA() {
			return c;
		}

		public void setA(final String a) {
			this.c = a;
		}
	}

	@Test
	void shouldConvertGettersToGetters() {
		Src src = new Src();
		src.b = "a";

		Dst dst = convert(src).to(Dst::new);
		assertThat(dst.c, equalTo("a"));
	}

	static class Src1 {

		String b;

		public String getA() {
			return b;
		}
	}

	static class Dst1 {

		String c;

		public String getA() {
			return c;
		}

		public void setA(final String a) {
			this.c = a;
		}

		public String get() {
			throw new RuntimeException("Should not call method");
		}

		public String is() {
			throw new RuntimeException("Should not call method");
		}
	}

	@Test
	void shouldConvertGettersToGettersAndSkipMethodsWithPrefixeNames() {
		Src1 src = new Src1();
		src.b = "a";

		Dst1 dst = convert(src).to(Dst1::new);
		assertThat(dst.c, equalTo("a"));
	}

}
