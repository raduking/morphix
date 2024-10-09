/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.examples;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.Conversions.convertFrom;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

/**
 * {@link BigInteger} conversion method. Will convert every {@link BigInteger} to {@link BigInteger} found in the object
 * tree by using the supplied conversion method toBigInteger defined statically here.
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
