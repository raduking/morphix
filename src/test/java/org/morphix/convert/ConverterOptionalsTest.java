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
package org.morphix.convert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.morphix.convert.Converter.convert;

import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Converter} with {@link Optional}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterOptionalsTest {

	private static final String TEST = "test";

	static class A {

		String s;

		public String getS() {
			return s;
		}

		public void setS(final String s) {
			this.s = s;
		}
	}

	static class B {

		Optional<String> s;

		public Optional<String> getS() {
			return s;
		}

		public void setS(final Optional<String> s) {
			this.s = s;
		}
	}

	@Test
	void shouldConvertOptionalToValue() {
		B b = new B();
		b.s = Optional.of(TEST);

		A c = convert(b).to(A.class);

		assertThat(c.s, equalTo(TEST));
	}

	@Test
	void shouldConvertEmptyOptionalToNull() {
		B b = new B();
		b.s = Optional.empty();

		A c = convert(b).to(A.class);

		assertThat(c.s, equalTo(null));
	}

	@Test
	void shouldConvertNullOptionalToNull() {
		B b = new B();

		A c = convert(b).to(A.class);

		assertThat(c.s, equalTo(null));
	}

	@Test
	void shouldConvertValueToOptional() {
		A a = new A();
		a.s = TEST;

		B b = convert(a).to(B.class);

		assertThat(b.s, equalTo(Optional.of(TEST)));
	}

	@Test
	void shouldConvertNullValueToEmptyOptional() {
		A a = new A();

		B b = convert(a).to(B.class);

		assertThat(b.s, equalTo(Optional.empty()));
	}

	static class C {
		Integer i;
	}

	static class D {
		String i;
	}

	static class E {
		C c;
	}

	static class F {
		Optional<D> c;
	}

	@Test
	void shouldConvertNestedOptionalToValue() {
		F f = new F();
		D d = new D();
		d.i = "13";
		f.c = Optional.of(d);

		E e = convert(f).to(E.class);

		assertThat(e.c.i, equalTo(13));
	}

	@Test
	void shouldConvertNestedOptionalToExistingInstance() {
		F f = new F();
		D d = new D();
		d.i = "13";
		f.c = Optional.of(d);

		C cref = new C();
		E e = new E();
		e.c = cref;

		e = convert(f).to(e);

		assertThat(e.c.i, equalTo(13));
	}

	@Test
	void shouldConvertEmptyNestedOptionalToNull() {
		F f = new F();
		f.c = Optional.empty();

		E e = convert(f).to(E.class);

		assertThat(e.c, equalTo(null));
	}

	@Test
	void shouldNotConvertToOptionalWithoutAGetterMethod() {
		C c = new C();
		c.i = 13;
		E e = new E();
		e.c = c;

		F f = convert(e).to(F.class);

		assertThat(f.c, equalTo(Optional.empty()));
	}
}
