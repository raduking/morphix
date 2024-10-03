package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.Converted.convert;

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
class ConverterGetterMethodsNoFieldsTest {

	public static class B {
		String x;
		String y;
	}

	public static class C {
		private Map<String, String> map = new HashMap<>();

		public void setMap(final Map<String, String> map) {
			this.map = map;
		}

		public final Map<String, String> getMap() {
			return map;
		}
	}

	public static class D {
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

	public static class Dst {

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

	public static class Src1 {

		String b;

		public String getA() {
			return b;
		}
	}

	public static class Dst1 {

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
