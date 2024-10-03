package org.morphix;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.ConversionFromMap.convertMap;

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
class ConversionFromMapTest {

	public static class Src {
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
			if (!(obj instanceof Src)) {
				return false;
			}
			return Objects.equals(s, ((Src) obj).s);
		}

		@Override
		public int hashCode() {
			return s == null ? super.hashCode() : s.hashCode();
		}
	}

	public static class Dst {
		public String s;

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Dst)) {
				return false;
			}
			return Objects.equals(s, ((Dst) obj).s);
		}

		@Override
		public int hashCode() {
			return s == null ? super.hashCode() : s.hashCode();
		}
	}

	@Test
	void shouldConvertMapToMapWithInstanceFunctions() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).collect(Collectors.toList());
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Map<String, Dst> result = convertMap(srcMap, String::new, Dst::new).toMap();

		assertThat(result.entrySet(), hasSize(size));

		for (Src src : list) {
			String s = String.valueOf(src.getS());
			Dst dst = result.get(s);
			assertThat(dst.s, equalTo(s));
		}
	}

	@Test
	void shouldConvertMapToMapWithConverterFunctions() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).collect(Collectors.toList());
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Map<String, Dst> result = convertMap(srcMap, String::valueOf, src -> convertFrom(src, Dst.class)).toMap();

		assertThat(result.entrySet(), hasSize(size));

		for (Src src : list) {
			String s = String.valueOf(src.getS());
			Dst dst = result.get(s);
			assertThat(dst.s, equalTo(s));
		}
	}

}
