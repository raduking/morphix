package org.morphix;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.morphix.function.InstanceFunction.to;

import org.junit.jupiter.api.Test;
import org.morphix.handler.AnyToAnyFromStaticMethod;

/**
 * Test class for {@link AnyToAnyFromStaticMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterAnyFromStaticMethodTest {

	private static final int TEST_INT = 11;
	private static final String TEST_INT_STRING = "" + TEST_INT;
	private static final String ANY_STRING = "anyString";

	public static class A {
		int x;
	}

	public static class B {
		String y;

		public static B foo(final A a) {
			B b = new B();
			b.y = "" + a.x;
			return b;
		}
	}

	public static class Src {
		public A a;
	}

	public static class Dst {
		public B a;
	}

	@Test
	void shouldUseStaticMethod() {
		A a = new A();
		a.x = TEST_INT;
		Src src = new Src();
		src.a = a;

		Dst dst = Conversion.convertFrom(src, Dst::new);

		assertThat(dst.a.y, equalTo(TEST_INT_STRING));
	}

	public enum E {
		X;

		@SuppressWarnings("unused")
		public static E foo(final A a) {
			return X;
		}
	}

	public static class Dst1 {
		E a;
	}

	@Test
	void shouldUseStaticMethodForDstEnums() {
		A a = new A();
		a.x = TEST_INT;
		Src src = new Src();
		src.a = a;

		Dst1 dst = Conversion.convertFrom(src, Dst1::new);

		assertThat(dst.a, equalTo(E.X));
	}

	@Test
	void shouldSkipNullValuesOnFromStaticMethod() {
		Src src = new Src();
		src.a = null;

		Dst dst = Conversion.convertFrom(src, Dst::new);

		assertThat(dst.a, equalTo(null));
	}

	public static class D {
		String y;

		@SuppressWarnings("unused")
		public static D foo(final A a) {
			return null;
		}
	}

	public static class Dst2 {
		public D a;
	}

	@Test
	void shouldNotOverrideWithNullIfStaticMethodReturnsNull() {
		A a = new A();
		a.x = TEST_INT;
		Src src = new Src();
		src.a = a;

		Dst2 dst = new Dst2();
		D d = new D();
		d.y = ANY_STRING;
		dst.a = d;

		Dst2 result = Conversion.convertFrom(src, to(dst));

		assertThat(result.a.y, equalTo(ANY_STRING));
		assertThat(d.y, equalTo(ANY_STRING));
	}
}
