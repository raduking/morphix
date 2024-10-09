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
package org.morphix.convert.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.FieldHandlerResult.BREAK;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.convert.FieldHandlerResult.SKIP;
import static org.morphix.lang.function.InstanceFunction.to;
import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.morphix.convert.Conversions;
import org.morphix.convert.FieldHandlerResult;

/**
 * Test class {@link AnyToAnyFromStaticMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToAnyFromStaticMethodTest {

	private static final Integer TEST_INTEGER = 11;
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

		Dst dst = Conversions.convertFrom(src, Dst::new);

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

		Dst1 dst = Conversions.convertFrom(src, Dst1::new);

		assertThat(dst.a, equalTo(E.X));
	}

	@Test
	void shouldSkipNullValuesOnFromStaticMethod() {
		Src src = new Src();
		src.a = null;

		Dst dst = Conversions.convertFrom(src, Dst::new);

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

		Dst2 result = Conversions.convertFrom(src, to(dst));

		assertThat(result.a.y, equalTo(ANY_STRING));
		assertThat(d.y, equalTo(ANY_STRING));
	}

	public static class F {
		Integer x;
	}

	public static class G {
		String y;

		public static G fromA(final F a) {
			G b = new G();
			b.y = a.x.toString();
			return b;
		}
	}

	public static class C {
		// empty
	}

	public static class Src3 {
		String x;
		Long l;
		F f;
		F g;
	}

	public static class Dst3 {
		Locale.Category x;
		BigInteger l;
		G f;
		C g;
	}

	@Test
	void shouldConvertNonCharSequenceToEnum() throws Exception {
		Field sField = Src3.class.getDeclaredField("l");
		Field dField = Dst3.class.getDeclaredField("l");

		boolean result = new AnyToAnyFromStaticMethod().condition(of(sField), of(dField));

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnAsHandledEvenIfSourceIsNullWithStaticMethod() throws Exception {
		Src3 src = new Src3();
		Dst3 dst = new Dst3();

		Field sField = Src3.class.getDeclaredField("f");
		Field dField = Dst3.class.getDeclaredField("f");

		FieldHandlerResult result = new AnyToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(BREAK));
	}

	@Test
	void shouldReturnAsNotHandledEvenIfSourceIsNull() throws Exception {
		Src3 src = new Src3();
		Dst3 dst = new Dst3();

		Field sField = Src3.class.getDeclaredField("g");
		Field dField = Dst3.class.getDeclaredField("g");

		FieldHandlerResult result = new AnyToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIP));
	}

	@Test
	void shouldReturnAsHandledWithStaticMethod() throws Exception {
		Src3 src = new Src3();
		src.f = new F();
		src.f.x = TEST_INTEGER;
		Dst3 dst = new Dst3();

		Field sField = Src3.class.getDeclaredField("f");
		Field dField = Dst3.class.getDeclaredField("f");

		FieldHandlerResult result = new AnyToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

	@Test
	void shouldReturnAsNotHandledIfNoStaticMethodWasFound() throws Exception {
		Src3 src = new Src3();
		src.g = new F();
		src.g.x = TEST_INTEGER;
		Dst3 dst = new Dst3();

		Field sField = Src3.class.getDeclaredField("g");
		Field dField = Dst3.class.getDeclaredField("g");

		FieldHandlerResult result = new AnyToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIP));
	}

}
