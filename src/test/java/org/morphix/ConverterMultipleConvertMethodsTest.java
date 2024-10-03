package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.ConversionFromIterable.convertIterable;
import static org.morphix.Converted.convert;
import static org.morphix.extra.SimpleConverters.empty;
import static org.morphix.extra.SimpleConverters.of;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for converting objects with multiple conversion methods.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterMultipleConvertMethodsTest {

	private static final String PREFIX = "X=";
	private static final int TEST_INT = 0;

	private static final int DEPTH = 20;

	public static class A {
		int x;
		A a;

		@Override
		public String toString() {
			return x + " - " + a;
		}
	}

	public static class B {
		String x;
		B a;

		@Override
		public String toString() {
			return x + " - " + a;
		}
	}

	public static class C {
		int x;
		C c;

		@Override
		public String toString() {
			return x + " - " + c;
		}
	}

	public static class D {
		String x;
		D c;

		@Override
		public String toString() {
			return x + " - " + c;
		}
	}

	public static class BConverter {
		public B convert(final A a) {
			return convertFrom(a, B::new, (src, dst) -> {
				dst.x = PREFIX + src.x;
			}, this::convert);
		}
	}

	public static class DConverter {
		public static D convert(final C c) {
			return convertFrom(c, D::new, (src, dst) -> {
				dst.x = PREFIX + src.x;
			}, DConverter::convert);
		}
	}

	public static class Src {
		A a;
		C c;
	}

	public static class Dst {
		B a;
		D c;
	}

	@Test
	void shouldConvertInnerObjects() {
		final int depth = DEPTH;
		Src src = new Src();
		src.a = createA(0, depth);
		src.c = createC(0, depth);

		BConverter bConverter = new BConverter();

		Dst dst = convertFrom(src, Dst::new, of(DConverter::convert, of(bConverter::convert)));

		D d = dst.c;
		B b = dst.a;
		for (int i = 0; i < depth; ++i) {
			assertThat(d.x, equalTo(PREFIX + (TEST_INT + i)));
			assertThat(b.x, equalTo(PREFIX + (TEST_INT + i)));
			d = d.c;
			b = b.a;
		}

		Dst dstNew = convert(src)
				.with(DConverter::convert)
				.with(bConverter::convert)
				.to(Dst::new);

		d = dstNew.c;
		b = dstNew.a;
		for (int i = 0; i < depth; ++i) {
			String expected = PREFIX + (TEST_INT + i);
			assertThat(d.x, equalTo(expected));
			assertThat(b.x, equalTo(expected));
			d = d.c;
			b = b.a;
		}
	}

	@Test
	void shouldConvertInnerObjectsWithConverted() {
		final int depth = DEPTH;
		Src src = new Src();
		src.a = createA(0, depth);
		src.c = createC(0, depth);

		BConverter bConverter = new BConverter();

		Dst dst = convert(src)
				.with(DConverter::convert)
				.with(bConverter::convert)
				.to(Dst::new);

		D d = dst.c;
		B b = dst.a;
		for (int i = 0; i < depth; ++i) {
			String expected = PREFIX + (TEST_INT + i);
			assertThat(d.x, equalTo(expected));
			assertThat(b.x, equalTo(expected));
			d = d.c;
			b = b.a;
		}
	}

	@Test
	void shouldConvertInnerObjectsForIterables() {
		final int depth = DEPTH;
		Src src = new Src();
		src.a = createA(0, depth);
		src.c = createC(0, depth);

		BConverter bConverter = new BConverter();

		List<Dst> dstList = convertIterable(List.of(src), Dst::new, of(bConverter::convert, of(DConverter::convert))).toList();

		D d = dstList.get(0).c;
		B b = dstList.get(0).a;
		for (int i = 0; i < depth; ++i) {
			assertThat(d.x, equalTo(PREFIX + (TEST_INT + i)));
			assertThat(b.x, equalTo(PREFIX + (TEST_INT + i)));
			d = d.c;
			b = b.a;
		}
	}

	@Test
	void shouldNotConvertInnerObjectsIfNoConvertMethodsAreSupplied() {
		final int depth = DEPTH;
		Src src = new Src();
		src.a = createA(0, depth);
		src.c = createC(0, depth);

		Dst dst = convertFrom(src, Dst::new, of(null, empty()));

		D d = dst.c;
		B b = dst.a;
		for (int i = 0; i < depth; ++i) {
			assertThat(d.x, equalTo("" + i));
			assertThat(b.x, equalTo("" + i));
			d = d.c;
			b = b.a;
		}
	}

	private static A createA(final int x, final int depth) {
		A a = new A();
		a.x = TEST_INT + x;
		if (x == depth) {
			return a;
		}
		a.a = createA(x + 1, depth);
		return a;
	}

	private static C createC(final int x, final int depth) {
		C c = new C();
		c.x = TEST_INT + x;
		if (x == depth) {
			return c;
		}
		c.c = createC(x + 1, depth);
		return c;
	}

}
