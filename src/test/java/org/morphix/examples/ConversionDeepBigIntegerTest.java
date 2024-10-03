package org.morphix.examples;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

/**
 * {@link BigInteger} conversion method. Will convert every {@link BigInteger}
 * to {@link BigInteger} found in the object tree by using the supplied
 * conversion method toBigInteger defined statically here.
 *
 * @author Radu Sebastian LAZIN
 */
class ConversionDeepBigIntegerTest {

	public static BigInteger toBigInteger(final BigInteger src) {
		return new BigInteger(src.toString());
	}

	public static class A {
		public BigInteger i;
	}

	public static class B {
		public BigInteger i;
	}

	public static class Src {
		public A a;
		public BigInteger id;
	}

	public static class Dst {
		public B a;
		public BigInteger id;
	}

	@Test
	void shouldConvertBigIntegers() {
		Src src = new Src();
		src.id = BigInteger.ONE;
		A a = new A();
		a.i = BigInteger.TEN;
		src.a = a;

		Dst dst = convertFrom(src, Dst::new, ConversionDeepBigIntegerTest::toBigInteger);

		assertThat(dst.id, equalTo(BigInteger.ONE));
		assertThat(dst.a.i, equalTo(BigInteger.TEN));
	}

	public static class C {
		public long i;
	}

	@Test
	void shouldConvertLongToBigInteger() {
		// because BigInteger has a:
		// BigInteger valueOf(long l);
		// method.
		C c = new C();
		c.i = 1L;

		B b = convertFrom(c, B::new);

		assertThat(b.i, equalTo(BigInteger.ONE));
	}
}
