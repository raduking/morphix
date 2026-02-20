/*
 * Copyright 2026 the original author or authors.
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
package org.morphix.convert.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;
import org.morphix.convert.Converter;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Test for {@link NumberToNumber}.
 *
 * @author Radu Sebastian LAZIN
 */
class NumberToNumberTest {

	private static final String BIG_NUMBER_STRING = "12345678901234567890";

	static class A {
		byte x1;
		short x2;
		char x3;
		int x4;
		long x5;
		float x6;
	}

	static class B {
		float x1;
		double x2;
		float x3;
		double x4;
		float x5;
		double x6;
	}

	@Test
	void shouldCoerceTypes() {
		A a = new A();
		a.x1 = 1;
		a.x2 = 2;
		a.x3 = 3;
		a.x4 = 4;
		a.x5 = 5;
		a.x6 = 6;

		B b = Converter.convert(a).to(B.class);

		assertThat(b.x1, equalTo(1f));
		assertThat(b.x2, equalTo(2d));
		assertThat(b.x3, equalTo(3f));
		assertThat(b.x4, equalTo(4d));
		assertThat(b.x5, equalTo(5f));
		assertThat(b.x6, equalTo(6d));
	}

	static class C {
		Integer x1;
	}

	@Test
	void shouldCoerceTypesWithNull() {
		C c = new C();
		c.x1 = null;

		B b = Converter.convert(c).to(B.class);

		assertThat(b.x1, equalTo(0f));
	}

	@Test
	void shouldReturnConvertedForNull() {
		NumberToNumber handler = new NumberToNumber();

		ExtendedField sfo = ExtendedField.of(null);
		ExtendedField dfo = ExtendedField.of(null);

		FieldHandlerResult result = handler.handle(sfo, dfo);

		assertThat(result, equalTo(FieldHandlerResult.CONVERTED));
	}

	static class D {
		BigInteger x1;
	}

	static class E {
		BigDecimal x1;
	}

	@Test
	void shouldConvertBigIntegerToBigDecimal() {
		D d = new D();
		d.x1 = new BigInteger(BIG_NUMBER_STRING);

		E e = Converter.convert(d).to(E.class);

		assertThat(e.x1, equalTo(new BigDecimal(BIG_NUMBER_STRING)));
	}

	static class F {
		Long x1;
	}

	static class G {
		BigInteger x1;
	}

	@Test
	void shouldConvertLongToBigInteger() {
		F f = new F();
		f.x1 = 123l;

		G g = Converter.convert(f).to(G.class);

		assertThat(g.x1, equalTo(BigInteger.valueOf(123)));
	}

	static class H {
		Long x1;
	}

	static class I {
		Byte x1;
	}

	@Test
	void shouldNotConvertLongToByte() {
		H h = new H();
		h.x1 = 123l;

		I i = Converter.convert(h).to(I.class);

		assertThat(i.x1, equalTo(null));
	}

	static class J {
		Byte x1;
		Byte x2;
		Byte x3;
		Byte x4;
		Byte x5;
	}

	static class K {
		Short x1;
		Integer x2;
		Long x3;
		Float x4;
		Double x5;
	}

	@Test
	void shouldConvertByteToOtherTypes() {
		J j = new J();
		j.x1 = 1;
		j.x2 = 2;
		j.x3 = 3;
		j.x4 = 4;
		j.x5 = 5;

		K k = Converter.convert(j).to(K.class);

		assertThat(k.x1, equalTo((short) 1));
		assertThat(k.x2, equalTo(2));
		assertThat(k.x3, equalTo(3l));
		assertThat(k.x4, equalTo(4f));
		assertThat(k.x5, equalTo(5d));
	}

	static class L {
		Character x1;
		Character x2;
		Character x3;
		Character x4;
	}

	static class M {
		Integer x1;
		Long x2;
		Float x3;
		Double x4;
	}

	@Test
	void shouldConvertCharacterToOtherTypes() {
		L l = new L();
		l.x1 = 1;
		l.x2 = 2;
		l.x3 = 3;
		l.x4 = 4;

		M m = Converter.convert(l).to(M.class);

		assertThat(m.x1, equalTo(1));
		assertThat(m.x2, equalTo(2l));
		assertThat(m.x3, equalTo(3f));
		assertThat(m.x4, equalTo(4d));
	}

	static class N {
		Short x1;
		Short x2;
		Short x3;
		Short x4;
	}

	static class O {
		Integer x1;
		Long x2;
		Float x3;
		Double x4;
	}

	@Test
	void shouldConvertShortToOtherTypes() {
		N n = new N();
		n.x1 = 1;
		n.x2 = 2;
		n.x3 = 3;
		n.x4 = 4;

		O o = Converter.convert(n).to(O.class);

		assertThat(o.x1, equalTo(1));
		assertThat(o.x2, equalTo(2l));
		assertThat(o.x3, equalTo(3f));
		assertThat(o.x4, equalTo(4d));
	}

	static class P {
		Integer x1;
		Integer x2;
		Integer x3;
	}

	static class Q {
		Long x1;
		Float x2;
		Double x3;
	}

	@Test
	void shouldConvertIntegerToOtherTypes() {
		P p = new P();
		p.x1 = 1;
		p.x2 = 2;
		p.x3 = 3;

		Q q = Converter.convert(p).to(Q.class);

		assertThat(q.x1, equalTo(1l));
		assertThat(q.x2, equalTo(2f));
		assertThat(q.x3, equalTo(3d));
	}

	static class R {
		Long x1;
		Long x2;
	}

	static class S {
		Float x1;
		Double x2;
	}

	@Test
	void shouldConvertLongToOtherTypes() {
		R r = new R();
		r.x1 = 1l;
		r.x2 = 2l;

		S s = Converter.convert(r).to(S.class);

		assertThat(s.x1, equalTo(1f));
		assertThat(s.x2, equalTo(2d));
	}
}
