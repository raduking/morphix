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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.morphix.convert.Converter.convert;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Converter}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterTest {

	public static class A {
		int x;
		int y;

		String s;

		public String getS() {
			return s;
		}
	}

	public static class B {
		String x;
		String y;

		String t;

		public void setT(final String t) {
			this.t = t;
		}
	}

	@Test
	void shouldConvertAtoB() {
		A a = new A();
		a.x = 13;

		B b = convert(a).to(B::new);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo("13"));
	}

	@Test
	void shouldConvertAtoBAndExtraConvertFunction() {
		A a = new A();
		a.x = 13;

		B b = convert(a).to(B::new, (s, d) -> d.x = "x=" + d.x);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo("x=13"));
	}

	@Test
	void shouldConvertAtoExistingB() {
		A a = new A();
		a.x = 13;

		B existingB = new B();
		B b = convert(a).to(existingB);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo("13"));
	}

	@Test
	void shouldConvertAtoBWithClassSyntax() {
		A a = new A();
		a.x = 13;

		B b = convert(a).to(B.class);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo("13"));
	}

	@Test
	void shouldConvertAtoBWithExtraConvertFunctionV1() {
		A a = new A();
		a.x = 13;
		a.y = 17;

		B b = convert(a)
				.with((final A s, final B d) -> d.x = "x=" + d.x)
				.to(B::new);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo("x=13"));
		assertThat(b.y, equalTo("17"));
	}

	@Test
	void shouldConvertAtoBWithExtraConvertFunctionV2() {
		A a = new A();
		a.x = 13;
		a.y = 17;

		B b = convert(a)
				.<B>with((s, d) -> d.x = "x=" + d.x)
				.to(B::new);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo("x=13"));
		assertThat(b.y, equalTo("17"));
	}

	@Test
	void shouldThrowExceptionIfExtraConvertHasWrongTypes() {
		A a = new A();

		ClassCastException result = null;
		try {
			convert(a)
					.with((final A s, final A d) -> d.x = 1 + d.x)
					.to(B::new);
		} catch (ClassCastException e) {
			result = e;
		}
		assertThat(result, notNullValue());
	}

	@Test
	void shouldKeepDeclaredOrderForExtraConvertFunctions() {
		List<Integer> order = new ArrayList<>();

		convert(new A())
				.with((final A s, final B d) -> {
					order.add(1);
				})
				.with((final A s, final B d) -> {
					order.add(2);
				})
				.to(B::new);

		List<Integer> expected = List.of(1, 2);

		assertThat(order, equalTo(expected));
	}

}
