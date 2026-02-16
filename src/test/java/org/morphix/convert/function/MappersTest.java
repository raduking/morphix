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
package org.morphix.convert.function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.morphix.convert.Converter.convert;

import org.junit.jupiter.api.Test;
import org.morphix.convert.annotation.Src;

/**
 * Test class for {@link Src} annotation.
 *
 * @author Radu Sebastian LAZIN
 */
class MappersTest {

	static class A {

		String s;
		String t;

		public String getS() {
			return s;
		}

		public String getT() {
			return t;
		}
	}

	static class B {

		String i;
		String j;

		public void setI(final String i) {
			this.i = i;
		}

		public void setJ(final String j) {
			this.j = j;
		}
	}

	@Test
	void shouldFindTheSourceFieldMultipleExtraConvertFunctions() {
		A a = new A();
		a.s = "13";
		a.t = "17";

		B b = convert(a)
				.with((final A s, final B d) -> {
					Mappers.map(s::getS, d::setI);
				})
				.with((final A s, final B d) -> {
					Mappers.map(s::getT, d::setJ);
				})
				.to(B::new);

		assertThat(b.i, equalTo("13"));
		assertThat(b.j, equalTo("17"));
	}

	@Test
	void shouldMapNonNullFieldsExtraConvertFunctions() {
		A a = new A();
		a.s = "13";
		a.t = "17";

		B b = new B();

		b = convert(a)
				.with((final A s, final B d) -> {
					Mappers.mapNonNull(s::getS, d::setI);
				})
				.with((final A s, final B d) -> {
					Mappers.mapNonNull(d::setJ, s::getT);
				})
				.to(b);

		assertThat(b.i, equalTo("13"));
		assertThat(b.j, equalTo("17"));
	}

	@Test
	void shouldNotMapNullFieldsExtraConvertFunctions() {
		A a = new A();

		B b = new B();
		b.i = "13";
		b.j = "17";

		b = convert(a)
				.with((final A s, final B d) -> {
					Mappers.mapNonNull(s::getS, d::setI);
				})
				.with((final A s, final B d) -> {
					Mappers.mapNonNull(d::setJ, s::getT);
				})
				.to(b);

		assertThat(b.i, equalTo("13"));
		assertThat(b.j, equalTo("17"));
	}
}
