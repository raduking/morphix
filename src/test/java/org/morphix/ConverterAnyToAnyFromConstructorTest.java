package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

/**
 * Test class for any to any from constructors.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterAnyToAnyFromConstructorTest {

	public static class A {
		Long s;
	}

	public static class B {
		String x;

		public B(final A a) {
			this.x = a.s.toString();
		}
	}

	public static class Src {
		A a;
	}

	public static class Dst {
		B a;
	}

	@Test
	void shouldConvertWithConstructors() {
		Src src = new Src();
		src.a = new A();
		src.a.s = 11L;

		Dst dst = Conversion.convertFrom(src, Dst::new);

		assertThat(dst.a.x, equalTo("11"));
	}

	public static class C {
		String x;

		@SuppressWarnings("unused")
		public C(final A a) {
			throw new NullPointerException();
		}
	}

	public static class Src1 {
		A a;
	}

	public static class Dst1 {
		C a;
	}

	@Test
	void shouldNotConvertWithConstructorsIfConstructorCallFails() {
		Src1 src = new Src1();
		src.a = new A();
		src.a.s = 11L;

		Dst1 dst = Conversion.convertFrom(src, Dst1::new);

		assertThat(dst.a.x, nullValue());
	}

	public static class D {
		String x;

		public D(final D d) {
			this.x = d.x;
		}
	}

	public static class Src2 {
		A a;
	}

	public static class Dst2 {
		D a;
	}

	@Test
	void shouldNotConvertWithConstructorsIfConstructorIsNotFound() {
		Src2 src = new Src2();
		src.a = new A();
		src.a.s = 11L;

		Dst2 dst = Conversion.convertFrom(src, Dst2::new);

		assertThat(dst.a.x, nullValue());
	}

}
