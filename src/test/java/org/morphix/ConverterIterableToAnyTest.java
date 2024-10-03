package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.ConversionFromIterable.convertIterable;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Tests conversions from {@link Iterable} to any.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterIterableToAnyTest {

	private static final Integer TEST_INTEGER_1 = 7;
	private static final Integer TEST_INTEGER_2 = 13;
	private static final String TEST_STRING_1 = "7";

	public static class A {
		int x;
	}

	public static class B {
		String x;

		@Override
		public boolean equals(final Object obj) {
			// basic equals implementation
			if (null == obj)
				return false;
			return Objects.equals(x, ((B) obj).x);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

	public static class Source {
		Long id;
		List<A> bees;
	}

	public static class Destination {
		String id;
		B[] bees;
	}

	@Test
	void shouldNotConvertIterablesToObjects() {
		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		List<A> as = List.of(a1, a2);

		B b = convertIterable(as, B::new).toAny(B.class);

		assertThat(b, equalTo(null));
	}

	@Test
	void shouldConvertIterablesToIterables() {
		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		List<A> as = List.of(a1);

		// List<B> bs = convertFrom(as, ArrayList::new);
		List<B> bs = convertIterable(as, B::new).toList();

		assertThat(bs.get(0).x, equalTo(TEST_STRING_1));
	}
}
