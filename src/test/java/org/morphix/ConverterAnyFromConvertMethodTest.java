package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.Conversion.convertFromIterable;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.handler.AnyToAnyFromConversionMethod;

/**
 * Tests {@link AnyToAnyFromConversionMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterAnyFromConvertMethodTest {

	private static final Long TEST_LONG = 13L;
	private static final int TEST_INT = 17;
	private static final String TEST_STRING_LONG = TEST_LONG.toString();
	private static final String PREFIX = "X=";
	private static final String TEST_CLASS_CAST_EXCEPTION_MESSAGE = "Test ClassCastException.";

	public static class A {
		Long x;
	}

	public static class B {
		String x;
	}

	public static class Source {
		A a;
	}

	public static class Destination {
		B a;
	}

	public static class AConverter {
		B convertFromA(final A a) {
			B b = new B();
			b.x = PREFIX + a.x.toString();
			return b;
		}
	}

	@Test
	void shouldUseConvertMethod() {
		Source src = createSource(TEST_LONG);

		AConverter aConverter = new AConverter();

		Destination dst = convertFrom(src, Destination::new, aConverter::convertFromA);

		assertThat(dst.a.x, equalTo(PREFIX + TEST_STRING_LONG));
	}

	@Test
	void shouldSkipNullValues() {
		Source src = new Source();

		AConverter aConverter = new AConverter();

		Destination dst = convertFrom(src, Destination::new, aConverter::convertFromA);

		assertThat(dst.a, equalTo(null));
	}

	public static class Src {
		List<Source> iterable;
	}

	public static class Dst {
		List<Destination> iterable;

		public List<Destination> getIterable() {
			return iterable;
		}
	}

	@Test
	void shouldConvertIterablesWithConvertMethod() {
		Src src = new Src();

		int listSize = 2;

		List<Source> list = new ArrayList<>();
		for (int i = 0; i < listSize; ++i) {
			list.add(createSource(TEST_LONG + i));
		}
		src.iterable = list;

		AConverter aConverter = new AConverter();

		Dst dst = convertFrom(src, Dst::new, aConverter::convertFromA);

		for (int i = 0; i < listSize; ++i) {
			assertThat(dst.iterable.get(i).a.x, equalTo(PREFIX + (TEST_LONG + i)));
		}
	}

	public static class C {
		int x;
		C c;

		@Override
		public String toString() {
			String result = "" + x;
			if (null != c) {
				result += ":" + c + " ";
			}
			return result;
		}
	}

	public static class D {
		String x;
		D c;

		@Override
		public String toString() {
			String result = x;
			if (null != c) {
				result += ":" + c + " ";
			}
			return result;
		}
	}

	public static class DConverter {
		D convert(final C c) {
			return convertFrom(c, D::new, (src, dst) -> {
				dst.x = PREFIX + src.x;
			}, this::convert);
		}
	}

	public static class Src1 {
		C c;
	}

	public static class Dst1 {
		D c;
	}

	@Test
	void shouldConvertInnerObjects() {
		final int depth = 10;
		Src1 src = new Src1();
		src.c = createC(0, depth);

		DConverter dConverter = new DConverter();

		Dst1 dst = convertFrom(src, Dst1::new, dConverter::convert);

		D d = dst.c;
		for (int i = 0; i < depth; ++i) {
			assertThat(d.x, equalTo(PREFIX + (TEST_INT + i)));
			d = d.c;
		}
	}

	public static class DConverterStatic {
		static D convert(final C c) {
			return convertFrom(c, D::new, (src, dst) -> {
				dst.x = PREFIX + src.x;
			}, DConverterStatic::convert);
		}
	}

	@Test
	void shouldConvertInnerObjectsStatic() {
		final int depth = 10;
		Src1 src = new Src1();
		src.c = createC(0, depth);

		Dst1 dst = convertFrom(src, Dst1::new, DConverterStatic::convert);

		D d = dst.c;
		for (int i = 0; i < depth; ++i) {
			assertThat(d.x, equalTo(PREFIX + (TEST_INT + i)));
			d = d.c;
		}
	}

	@Test
	void shouldConvertInnerObjectsForIterables() {
		final int depth = 10;
		Src1 src = new Src1();
		src.c = createC(0, depth);

		DConverter dConverter = new DConverter();

		List<Dst1> dstList = convertFromIterable(List.of(src), Dst1::new, dConverter::convert);

		D d = dstList.get(0).c;
		for (int i = 0; i < depth; ++i) {
			assertThat(d.x, equalTo(PREFIX + (TEST_INT + i)));
			d = d.c;
		}
	}

	public static class DConverterCast1 {
		D convert(final C c) {
			return convertFrom(c, D::new, (src, dst) -> {
				dst.x = PREFIX + src.x;
				throw new ClassCastException(TEST_CLASS_CAST_EXCEPTION_MESSAGE);
			});
		}
	}

	@Test
	void shouldAvoidConvertorInnerClassCastException1() {
		final int depth = 2;
		Src1 src = new Src1();
		src.c = createC(0, depth);

		DConverterCast1 dConverter = new DConverterCast1();

		assertThrows(ConverterException.class, () -> convertFrom(src, Dst1::new, dConverter::convert));
	}

	public static class DConverterCast2 {
		D convert(final C c) {
			D d = new D();
			d.x = PREFIX + c.x;
			throw new ClassCastException(TEST_CLASS_CAST_EXCEPTION_MESSAGE);
		}
	}

	@Test
	void shouldAvoidConvertorInnerClassCastException2() {
		final int depth = 2;
		Src1 src = new Src1();
		src.c = createC(0, depth);

		DConverterCast2 dConverter = new DConverterCast2();

		assertThrows(ConverterException.class, () -> convertFrom(src, Dst1::new, dConverter::convert));
	}

	public static class DConverterNull {
		@SuppressWarnings("unused")
		D convert(final C c) {
			return null;
		}
	}

	@Test
	void shouldSkipNullConverterResult() {
		final int depth = 2;
		Src1 src = new Src1();
		src.c = createC(0, depth);

		DConverterNull dConverter = new DConverterNull();

		Dst1 dst = convertFrom(src, Dst1::new, dConverter::convert);

		assertThat(dst.c, equalTo(null));
	}

	@Test
	void shouldNotConvertInnerObjectsLambdaMethod() {
		final int depth = 2;
		Src1 src = new Src1();
		src.c = createC(0, depth);

		Dst1 dst = convertFrom(src, Dst1::new, (final C c) -> {
			D d = new D();
			d.x = PREFIX + c.x;
			return d;
		});

		D d = dst.c;
		assertThat(d.x, equalTo(PREFIX + TEST_INT));
		d = d.c;
		assertThat(d, equalTo(null));
	}

	@Test
	void shouldNotSwallowClassCastExceptionOnNullLambdaMethod() {
		final int depth = 2;
		Src1 src = new Src1();
		src.c = createC(0, depth);

		assertThrows(ConverterException.class, () -> convertFrom(src, Dst1::new, (final C c) -> {
			D d = new D();
			d.x = PREFIX + c.x;
			throw new ClassCastException(TEST_CLASS_CAST_EXCEPTION_MESSAGE);
		}));
	}

	@Test
	void shouldNotConvertAnythingForNotEncounteredTypes() {
		final int depth = 2;
		Src1 src = new Src1();
		src.c = createC(0, depth);

		Dst1 dst = convertFrom(src, Dst1::new, (final A a) -> {
			D d = new D();
			d.x = PREFIX + a.x;
			return d;
		});

		D d = dst.c;
		for (int i = 0; i < depth; ++i) {
			assertThat(d.x, equalTo(String.valueOf(TEST_INT + i)));
			d = d.c;
		}
	}

	public static class AConverterWithMethods {
		@SuppressWarnings("unused")
		public void a(final B b, final A a) {
			// empty
		}

		public B convertFromA(final A a) {
			B b = new B();
			b.x = PREFIX + a.x.toString();
			return b;
		}
	}

	@Test
	void shouldSkipNonSimpleConverterMethodsFromConverterClass() {
		Source src = createSource(TEST_LONG);

		AConverterWithMethods aConverter = new AConverterWithMethods();

		Destination dst = convertFrom(src, Destination::new, aConverter::convertFromA);

		assertThat(dst.a.x, equalTo(PREFIX + TEST_STRING_LONG));
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

	private static Source createSource(final Long l) {
		Source src = new Source();
		A a = new A();
		a.x = l;
		src.a = a;
		return src;
	}

}
