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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.convert.FieldHandlerResult.SKIPPED;

import org.junit.jupiter.api.Test;
import org.morphix.convert.Conversions;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Test class for {@link AnyToAnyFromConstructor}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToAnyFromConstructorTest {

	private AnyToAnyFromConstructor victim = new AnyToAnyFromConstructor();

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

		Dst dst = Conversions.convertFrom(src, Dst::new);

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

		Dst1 dst = Conversions.convertFrom(src, Dst1::new);

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

		Dst2 dst = Conversions.convertFrom(src, Dst2::new);

		assertThat(dst.a.x, nullValue());
	}

	public static class E {
		// empty
	}

	public static class F {
		public F(@SuppressWarnings("unused") final E a) {
			// empty
		}
	}

	public static class G {
		// empty
	}

	public static class H<T> {
		public H(@SuppressWarnings("unused") final T t) {
			// empty
		}
	}

	public static class Src3 {
		public E a;
		public E b;
		public E c;
	}

	public static class Dst3 {
		public F a;
		public G b;
		public H<E> c;
	}

	@Test
	void shouldReturnTrueOnConditionWhenTheConstructorIsAvailable() throws Exception {
		ExtendedField sfo = ExtendedField.of(Src3.class.getDeclaredField("a"));
		ExtendedField dfo = ExtendedField.of(Dst3.class.getDeclaredField("a"));

		boolean result = victim.condition(sfo, dfo);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnConditionWhenTheConstructorIsAvailable() throws Exception {
		ExtendedField sfo = ExtendedField.of(Src3.class.getDeclaredField("b"));
		ExtendedField dfo = ExtendedField.of(Dst3.class.getDeclaredField("b"));

		boolean result = victim.condition(sfo, dfo);

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueOnHandleWhenConstructorIsAvailable() throws Exception {
		Src3 src = new Src3();
		src.a = new E();
		Dst3 dst = new Dst3();
		ExtendedField sfo = ExtendedField.of(Src3.class.getDeclaredField("a"), src);
		ExtendedField dfo = ExtendedField.of(Dst3.class.getDeclaredField("a"), dst);

		FieldHandlerResult result = victim.handle(sfo, dfo);

		assertThat(result, equalTo(CONVERTED));
		assertThat(dst.a, notNullValue());
	}

	@Test
	void shouldReturnFalseOnHandleWhenConstructorIsAvailable() throws Exception {
		Src3 src = new Src3();
		src.b = new E();
		Dst3 dst = new Dst3();
		ExtendedField sfo = ExtendedField.of(Src3.class.getDeclaredField("b"), src);
		ExtendedField dfo = ExtendedField.of(Dst3.class.getDeclaredField("b"), dst);

		FieldHandlerResult result = victim.handle(sfo, dfo);

		assertThat(result, equalTo(SKIPPED));
		assertThat(dst.a, nullValue());
	}

	@Test
	void shouldReturnFalseOnConditionWhenGenericConstructorIsAvailable() throws Exception {
		// this test needs revision when we are able to determine the exact
		// generic types of the classes involved
		ExtendedField sfo = ExtendedField.of(Src3.class.getDeclaredField("c"));
		ExtendedField dfo = ExtendedField.of(Dst3.class.getDeclaredField("c"));

		boolean result = victim.condition(sfo, dfo);

		assertFalse(result);
	}

}
