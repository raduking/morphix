package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;

import org.junit.jupiter.api.Test;
import org.morphix.annotation.Src;

/**
 * Test class for annotated conversions.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterAnnotationHandlerTest {

	public static final String TEST_STRING = "13";
	public static final Long TEST_LONG = Long.valueOf(TEST_STRING);

	public static class A {
		String s;
		String b;
	}

	public static class B {
		@Src(name = "s")
		Long l;

		@Src("b")
		Long x;
	}

	@Test
	void shouldConvertWithAnnotation() {
		A a = new A();
		a.s = TEST_STRING;
		a.b = TEST_STRING;

		B b = convertFrom(a, B::new);

		assertThat(b.l, equalTo(TEST_LONG));
		assertThat(b.x, equalTo(TEST_LONG));
	}

	public static class C {
		String s;
	}

	public static class D {
		@Src
		Long l;
	}

	@Test
	void shouldNotConvertWhenAnnotationIsIncomplete() {
		C a = new C();
		a.s = TEST_STRING;

		D b = convertFrom(a, D::new);

		assertThat(b.l, equalTo(null));
	}

}
