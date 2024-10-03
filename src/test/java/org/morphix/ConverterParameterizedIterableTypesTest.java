package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.morphix.Conversion.convertFrom;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.morphix.extra.ExcludedFields;
import org.morphix.function.ExtraConvertFunction;

/**
 * Test class for {@link ParameterizedType}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterParameterizedIterableTypesTest {

	private static final String PIT_COVERAGE_PROBES_FIELD = "$$pitCoverageProbes";

	public static class SrcWithGenericListValues {
		private List<List<List<LocalDateTime>>> list;

		public List<List<List<LocalDateTime>>> getList() {
			return list;
		}
	}

	public static class DstWithGenericListValues {
		private List<List<List<String>>> list;

		public List<List<List<String>>> getList() {
			return list;
		}
	}

	@Test
	void shouldConvertListWithGenericListValues() {
		SrcWithGenericListValues src = new SrcWithGenericListValues();
		src.list = new ArrayList<>();
		LocalDateTime value = LocalDateTime.now();
		src.list.add(Collections.singletonList(Collections.singletonList(value)));

		DstWithGenericListValues dst = convertFrom(src, DstWithGenericListValues::new);

		assertThat(dst.list, notNullValue());
		assertThat(dst.list.get(0).get(0), contains(value.toString()));
	}

	public static class SrcWithGenericMapValues {
		private List<Map<LocalDateTime, LocalDateTime>> list;

		public List<Map<LocalDateTime, LocalDateTime>> getList() {
			return list;
		}
	}

	public static class DstWithGenericMapValues {
		private List<Map<String, String>> list;

		public List<Map<String, String>> getList() {
			return list;
		}
	}

	@Test
	void shouldConvertListWithGenericMapValues() {
		SrcWithGenericMapValues src = new SrcWithGenericMapValues();
		src.list = new ArrayList<>();
		LocalDateTime value = LocalDateTime.now();
		src.list.add(Collections.singletonMap(value, value));

		DstWithGenericMapValues dst = convertFrom(src, DstWithGenericMapValues::new);

		assertThat(dst.list, notNullValue());
		Map<String, String> map = dst.list.get(0);
		assertThat(map.keySet().iterator().next(), equalTo(value.toString()));
		assertThat(map.values().iterator().next(), equalTo(value.toString()));
	}

	public static class A<T> {
		T t;
		String s;
	}

	public static class Src {
		List<A<Integer>> list;

		public List<A<Integer>> getList() {
			return list;
		}
	}

	public static class Dst {
		List<A<String>> list;

		public List<A<String>> getList() {
			return list;
		}
	}

	@Test
	void shouldConvertListWithGenericType() {
		Src src = new Src();
		src.list = new ArrayList<>();
		A<Integer> a1 = new A<>();
		a1.t = 17;
		a1.s = "x";
		src.list.add(a1);

		Dst dst = convertFrom(src, Dst::new, ExtraConvertFunction.empty(),
				Configuration.of(ExcludedFields.exclude(PIT_COVERAGE_PROBES_FIELD)));

		A<String> a2 = dst.list.get(0);
		assertThat(a2.t, equalTo("17"));
		assertThat(a2.s, equalTo("x"));
	}

	public static class B<T> {
		T t;
		String s;

		public B(final String s) {
			this.s = s;
		}
	}

	public static class Src1 {
		List<B<Integer>> list;

		public List<B<Integer>> getList() {
			return list;
		}
	}

	public static class Dst1 {
		List<B<String>> list;

		public List<B<String>> getList() {
			return list;
		}
	}

	@Test
	void shouldConvertListWithGenericTypeWithoutDefaultConstructor() {
		Src1 src = new Src1();
		src.list = new ArrayList<>();
		B<Integer> b1 = new B<>("y");
		b1.t = 17;
		src.list.add(b1);

		Dst1 dst = convertFrom(src, Dst1::new);

		B<String> b2 = dst.list.get(0);
		assertThat(b2.t, equalTo("17"));
		assertThat(b2.s, equalTo("y"));
	}

	public static class Src2 {
		Set<A<Integer>> list;

		public Set<A<Integer>> getList() {
			return list;
		}
	}

	@Test
	void shouldConvertSetWithGenericType() {
		Src2 src = new Src2();
		src.list = new HashSet<>();
		A<Integer> a1 = new A<>();
		a1.t = 17;
		a1.s = "x";
		src.list.add(a1);

		Dst dst = convertFrom(src, Dst::new);

		A<String> a2 = dst.list.get(0);
		assertThat(a2.t, equalTo("17"));
		assertThat(a2.s, equalTo("x"));
	}

	public static class Src3 {
		Queue<A<Integer>> list;

		public Queue<A<Integer>> getList() {
			return list;
		}
	}

	@Test
	void shouldConvertQueueWithGenericType() {
		Src3 src = new Src3();
		src.list = new LinkedList<>();
		A<Integer> a1 = new A<>();
		a1.t = 17;
		a1.s = "x";
		src.list.add(a1);

		Dst dst = convertFrom(src, Dst::new);

		A<String> a2 = dst.list.get(0);
		assertThat(a2.t, equalTo("17"));
		assertThat(a2.s, equalTo("x"));
	}

}
