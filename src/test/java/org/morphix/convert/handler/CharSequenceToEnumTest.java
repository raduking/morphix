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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.FieldHandlerResult.BREAK;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.convert.ObjectConverterException;

/**
 * Tests class for {@link CharSequenceToEnum}.
 *
 * @author Radu Sebastian LAZIN
 */
class CharSequenceToEnumTest {

	public enum E1 {
		TEST_ENUM_1,
		TEST_ENUM_2
	}

	public enum E2 {
		TEST_ENUM2_1,
		TEST_ENUM2_2;

		@SuppressWarnings("unused")
		public static E2 fromString(final String s) {
			return E2.TEST_ENUM2_2;
		}
	}

	public enum E3 {
		TEST_ENUM3_1,
		TEST_ENUM3_2;

		@SuppressWarnings("unused")
		public static E3 fromString(final String s) {
			return null;
		}
	}

	public static final String TEST_ENUM_STRING_1 = E1.TEST_ENUM_1.toString();
	public static final String TEST_ENUM_STRING_2 = E1.TEST_ENUM_2.toString();
	public static final String TEST_ENUM_STRING_3 = "Undefined";

	public static final String TEST_ENUM2_STRING_1 = E2.TEST_ENUM2_1.toString();
	public static final String TEST_ENUM2_STRING_2 = E2.TEST_ENUM2_2.toString();

	public static final String TEST_ENUM3_STRING_2 = E3.TEST_ENUM3_2.toString();

	public static class Source {
		String testEnum;
		E1 testString;
	}

	public static class Destination {
		E1 testEnum;
		String testString;
	}

	@Test
	void shouldConvertEnumFields() {
		Source src = new Source();
		src.testEnum = TEST_ENUM_STRING_1;
		src.testString = E1.TEST_ENUM_2;

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.testEnum, equalTo(E1.TEST_ENUM_1));
		assertThat(dst.testString, equalTo(TEST_ENUM_STRING_2));
	}

	@Test
	void shouldSkipNullValuesInSource() {
		Source src = new Source();

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.testEnum, equalTo(null));
		assertThat(dst.testString, equalTo(null));
	}

	@Test
	void shouldFailWhenValueOfFails() {
		Source src = new Source();
		src.testEnum = TEST_ENUM_STRING_3;

		assertThrows(ObjectConverterException.class, () -> convertFrom(src, Destination::new));
	}

	@Test
	void shouldFailWhenValueOfFailsAndShowFailedFieldsInExceptionMessage() {
		Source src = new Source();
		src.testEnum = TEST_ENUM_STRING_3;

		ObjectConverterException exception = null;
		try {
			convertFrom(src, Destination::new);
		} catch (ObjectConverterException e) {
			assertThat(e.getMessage(), startsWith("Error converting fields: "));
			assertThat(e.getMessage(), containsString("srcField: testEnum"));
			assertThat(e.getMessage(), containsString("dstField: testEnum"));
			exception = e;
		}
		assertNotNull(exception);
	}

	public static class Source2 {
		String testEnum;
	}

	public static class Destination2 {
		E2 testEnum;
	}

	@Test
	void shouldUseStaticStringConverterMethod() {
		Source2 src = new Source2();
		src.testEnum = TEST_ENUM2_STRING_1;

		Destination2 dst = convertFrom(src, Destination2::new);

		assertThat(dst.testEnum, equalTo(E2.TEST_ENUM2_2));
	}

	public static class Destination3 {
		E3 testEnum;
	}

	@Test
	void shouldSkipNullValuesFromStaticStringConverterMethod() {
		Source src = new Source();
		src.testEnum = TEST_ENUM3_STRING_2;

		Destination3 dst = convertFrom(src, Destination3::new);

		assertThat(dst.testEnum, equalTo(E3.TEST_ENUM3_2));
	}

	public enum E4 {
		TEST_ENUM4_1,
		TEST_ENUM4_2
	}

	public enum E5 {
		TEST_ENUM5_1,
		TEST_ENUM5_2;

		@SuppressWarnings("unused")
		public static E5 fromString(final String s) {
			return E5.TEST_ENUM5_2;
		}
	}

	public static final String TEST_ENUM4_STRING_1 = E4.TEST_ENUM4_1.toString();
	public static final String TEST_ENUM5_STRING_1 = E5.TEST_ENUM5_1.toString();

	public static class Source4 {
		String testEnum;
		E4 testString;
	}

	public static class Destination4 {
		E4 testEnum;
		String testString;
	}

	@Test
	void shouldReturnAsHandled() throws Exception {
		Source4 src = new Source4();
		src.testEnum = TEST_ENUM4_STRING_1;
		Destination4 dst = new Destination4();

		Field sField = Source4.class.getDeclaredField("testEnum");
		Field dField = Destination4.class.getDeclaredField("testEnum");

		FieldHandlerResult result = new CharSequenceToEnum().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

	@Test
	void shouldReturnAsHandledEvenIfSourceIsNull() throws Exception {
		Source4 src = new Source4();
		src.testEnum = null;
		Destination4 dst = new Destination4();

		Field sField = Source4.class.getDeclaredField("testEnum");
		Field dField = Destination4.class.getDeclaredField("testEnum");

		FieldHandlerResult result = new CharSequenceToEnum().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(BREAK));
	}

	public static class Source5 {
		String testEnum;
	}

	public static class Destination5 {
		E5 testEnum;
	}

	@Test
	void shouldReturnAsHandledWithStaticMethod() throws Exception {
		Source5 src = new Source5();
		src.testEnum = TEST_ENUM5_STRING_1;
		Destination5 dst = new Destination5();

		Field sField = Source5.class.getDeclaredField("testEnum");
		Field dField = Destination5.class.getDeclaredField("testEnum");

		FieldHandlerResult result = new CharSequenceToEnum().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

}
