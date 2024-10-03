package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.morphix.Converted.convert;
import static org.morphix.FieldHandlerResult.SKIP;

import org.junit.jupiter.api.Test;
import org.morphix.Configuration;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Test class for {@link AnyToAnyFromConversionMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToAnyFromConversionMethodTest {

	private static final String PREFIX = "X=";
	private static final int TEST_INT = 13;
	private static final String TEST_STRING = "" + TEST_INT;

	public static class A {
		int x;
	}

	public static class B {
		String x;
	}

	public static class Src {
		A a;
	}

	public static class Dst {
		B a;
	}

	public static class ConverterMethodRefObject {
		public B convert(final A a) {
			B b = new B();
			b.x = PREFIX + a.x;
			return b;
		}
	}

	public static class ConverterMethodRef {
		public static B convert(final A a) {
			B b = new B();
			b.x = PREFIX + a.x;
			return b;
		}
	}

	@Test
	void shouldConvertWithSimpleConverterMethodRefObject() {
		Src src = new Src();
		src.a = new A();
		src.a.x = TEST_INT;

		ConverterMethodRefObject converterMethodRefObject = new ConverterMethodRefObject();

		Dst dst = convert(src)
				.with(converterMethodRefObject::convert)
				.to(Dst::new);

		assertThat(dst.a.x, equalTo(PREFIX + TEST_STRING));
	}

	@Test
	void shouldConvertWithSimpleConverterMethodRef() {
		Src src = new Src();
		src.a = new A();
		src.a.x = TEST_INT;

		Dst dst = convert(src)
				.with(ConverterMethodRef::convert)
				.to(Dst::new);

		assertThat(dst.a.x, equalTo(PREFIX + TEST_STRING));
	}

	@Test
	void shouldConvertWithSimpleConverterLambda() {
		Src src = new Src();
		src.a = new A();
		src.a.x = TEST_INT;

		Dst dst = convert(src)
				.with((final A a) -> {
					B b = new B();
					b.x = PREFIX + a.x;
					return b;
				})
				.to(Dst::new);

		assertThat(dst.a.x, equalTo(PREFIX + TEST_STRING));
	}

	@Test
	void shouldReturnFalseIfSourceIsNull() throws Exception {
		AnyToAnyFromConversionMethod<?, ?> handler = new AnyToAnyFromConversionMethod<>(Configuration.defaultConfiguration());

		B a = new B();
		B b = new B();

		ConverterField scf = ConverterField.of(B.class.getDeclaredField("x"), a);
		ConverterField dcf = ConverterField.of(B.class.getDeclaredField("x"), b);

		FieldHandlerResult result = handler.handle(scf, dcf);

		assertThat(result, equalTo(SKIP));
	}

	@Test
	void shouldNotHaveSimpleConvertersForDefaultConfiguration() throws Exception {
		AnyToAnyFromConversionMethod<?, ?> handler = new AnyToAnyFromConversionMethod<>(Configuration.defaultConfiguration());

		B a = new B();
		B b = new B();

		ConverterField scf = ConverterField.of(B.class.getDeclaredField("x"), a);
		ConverterField dcf = ConverterField.of(B.class.getDeclaredField("x"), b);

		boolean result = handler.condition(scf, dcf);

		assertFalse(result);
	}

}
