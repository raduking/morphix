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
package org.morphix.convert.annotation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.morphix.convert.Converter.convert;

import org.junit.jupiter.api.Test;
import org.morphix.convert.Converter;
import org.morphix.convert.annotation.SrcTest.A2.A3;

/**
 * Test class for {@link Src} annotation.
 *
 * @author Radu Sebastian LAZIN
 */
class SrcTest {

	public static class A {

		String s;

		public String getS() {
			return s;
		}
	}

	public static class B {

		@Src("s")
		Integer i;

		public Integer getI() {
			return i;
		}
	}

	@Test
	void shouldFindTheSourceFieldWithAnnotation() {
		A a = new A();
		a.s = "13";

		B b = Converter.convert(a).to(B::new);

		assertThat(b.i, equalTo(13));
	}

	public static class A1 {

		String s;

		public String getS() {
			return s;
		}
	}

	public static class B1 {

		Integer i;

		@Src("s")
		public Integer getI() {
			return i;
		}
	}

	@Test
	void shouldFindTheSourceFieldWithAnnotationOnGetter() {
		A1 a = new A1();
		a.s = "13";

		B1 b = convert(a).to(B1::new);

		assertThat(b.i, equalTo(13));
	}

	public static class A2 {

		A3 x;

		public A3 getX() {
			return x;
		}

		public static class A3 {
			String x;
		}
	}

	public static class B2 {

		@Src(
				from = {
						@From(type = A1.class, path = "s"),
						@From(type = A2.class, path = "x.x")
				})
		Integer i;

		public Integer getI() {
			return i;
		}
	}

	@Test
	void shouldFindTheSourceFieldWithFromAnnotation() {
		A2 a2 = new A2();
		A3 a3 = new A3();
		a3.x = "13";
		a2.x = a3;

		B2 b = convert(a2).to(B2::new);

		assertThat(b.i, equalTo(13));

		A1 a1 = new A1();
		a1.s = "17";
		b = convert(a1).to(B2::new);

		assertThat(b.i, equalTo(17));
	}
}
