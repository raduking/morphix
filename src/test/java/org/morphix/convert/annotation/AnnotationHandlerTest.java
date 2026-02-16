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
package org.morphix.convert.annotation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.Conversions.convertFrom;

import org.junit.jupiter.api.Test;

/**
 * Test class for annotated conversions.
 *
 * @author Radu Sebastian LAZIN
 */
class AnnotationHandlerTest {

	public static final String TEST_STRING = "13";
	public static final Long TEST_LONG = Long.valueOf(TEST_STRING);

	public static class A {
		String s;
		String b;
	}

	public static class B {
		@Src(name = "s")
		Long l;

		@Src("b")
		Long x;
	}

	@Test
	void shouldConvertWithAnnotation() {
		A a = new A();
		a.s = TEST_STRING;
		a.b = TEST_STRING;

		B b = convertFrom(a, B::new);

		assertThat(b.l, equalTo(TEST_LONG));
		assertThat(b.x, equalTo(TEST_LONG));
	}

	public static class C {
		String s;
	}

	public static class D {
		@Src
		Long l;
	}

	@Test
	void shouldNotConvertWhenAnnotationIsIncomplete() {
		C a = new C();
		a.s = TEST_STRING;

		D b = convertFrom(a, D::new);

		assertThat(b.l, equalTo(null));
	}

}
