package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.ConversionFromIterable.convertIterable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Test class for extended iterable conversions.
 *
 * @author Radu Sebastian LAZIN
 */
class FromIterableTest {

	public static class Src {
		public Integer s;

		public Src(final Integer s) {
			this.s = s;
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
	void shouldConvertIterablesToList() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).collect(Collectors.toList());

		List<Dst> result = convertIterable(list, Dst::new).toList();

		assertThat(result, hasSize(list.size()));

		for (int i = 0; i < size; ++i) {
			assertThat(result.get(i).s, equalTo(String.valueOf(i)));
		}
	}

	@Test
	void shouldConvertIterablesToSet() {
		List<Src> list = IntStream.of(1, 1, 1).boxed().map(Src::new).collect(Collectors.toList());

		Set<Dst> resultSet = convertIterable(list, Dst::new).toSet();

		assertThat(resultSet, hasSize(1));

		assertThat(resultSet.iterator().next().s, equalTo("1"));
	}

}
