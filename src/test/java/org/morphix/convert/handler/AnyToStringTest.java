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
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.convert.FieldHandlerContext;
import org.morphix.convert.FieldHandlerResult;

/**
 * Test class for {@link AnyToString}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToStringTest {

	private static final Long TEST_LONG = 19L;
	private static final String TEST_STRING_LONG = "19";

	public static class Source {
		Long testLong;
	}

	public static class Destination {
		String testLong;
	}

	@Test
	void shouldConvertAnyToString() {
		Source s = new Source();
		s.testLong = TEST_LONG;

		Destination d = convertFrom(s, Destination::new);

		assertThat(d.testLong, equalTo(TEST_STRING_LONG));
	}

	public static class A {
		public Integer i;
	}

	public static class B {
		public String i;
	}

	@Test
	void shouldReturnHandledIfValueIsNull() throws Exception {
		A src = new A();
		B dst = new B();

		Field sField = A.class.getDeclaredField("i");
		Field dField = B.class.getDeclaredField("i");

		FieldHandlerResult result = new AnyToString().handle(of(sField, src), of(dField, dst), new FieldHandlerContext());

		assertThat(result, equalTo(CONVERTED));
	}

}
