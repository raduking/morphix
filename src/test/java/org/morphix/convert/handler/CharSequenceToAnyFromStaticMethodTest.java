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
import static org.morphix.convert.FieldHandlerResult.HANDLED;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.convert.FieldHandlerResult.SKIPPED;
import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.convert.FieldHandlerResult;

/**
 * Test class for {@link CharSequenceToAnyFromStaticMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
class CharSequenceToAnyFromStaticMethodTest {

	public static final String TEST_STRING = "testString";

	public static class A {
		String x;

		public static A fromString(final String s) {
			A a = new A();
			a.x = s;
			return a;
		}
	}

	public static class Src {
		String a;
		Integer b;
		String s;
		String c;
	}

	public static class Dst {
		A a;
		A b;
		String s;
		C c;
	}

	public static class C {
		// empty
	}

	@Test
	void shouldReturnAsNotHandledEvenIfSourceIsNull() throws Exception {
		Src src = new Src();
		src.a = null;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("a");
		Field dField = Dst.class.getDeclaredField("a");

		FieldHandlerResult result = new CharSequenceToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(HANDLED));
	}

	@Test
	void shouldReturnAsHandledWithStaticMethod() throws Exception {
		Src src = new Src();
		src.a = TEST_STRING;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("a");
		Field dField = Dst.class.getDeclaredField("a");

		FieldHandlerResult result = new CharSequenceToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

	@Test
	void shouldReturnAsNotHandledWithStaticMethodIfNoStaticConvertMethodWasFound() throws Exception {
		Src src = new Src();
		src.c = TEST_STRING;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("c");
		Field dField = Dst.class.getDeclaredField("c");

		FieldHandlerResult result = new CharSequenceToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIPPED));
	}

	@Test
	void shouldReturnAsNotHandledWithStaticMethodIfNoStaticConvertMethodWasFoundAndFieldIsNull() throws Exception {
		Src src = new Src();
		src.c = null;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("c");
		Field dField = Dst.class.getDeclaredField("c");

		FieldHandlerResult result = new CharSequenceToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIPPED));
	}

}
