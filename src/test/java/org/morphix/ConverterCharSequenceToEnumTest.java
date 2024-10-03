package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.morphix.Conversion.convertFrom;

import org.junit.jupiter.api.Test;

/**
 * Tests conversions from {@link CharSequence} to {@link Enum}. Also tests
 * {@link Enum} to {@link String}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterCharSequenceToEnumTest {

	public enum E {
		TEST_ENUM_1, TEST_ENUM_2
	}

	public enum E2 {
		TEST_ENUM2_1, TEST_ENUM2_2;

		@SuppressWarnings("unused")
		public static E2 fromString(final String s) {
			return E2.TEST_ENUM2_2;
		}
	}

	public enum E3 {
		TEST_ENUM3_1, TEST_ENUM3_2;

		@SuppressWarnings("unused")
		public static E3 fromString(final String s) {
			return null;
		}
	}

	public static final String TEST_ENUM_STRING_1 = E.TEST_ENUM_1.toString();
	public static final String TEST_ENUM_STRING_2 = E.TEST_ENUM_2.toString();
	public static final String TEST_ENUM_STRING_3 = "Undefined";

	public static final String TEST_ENUM2_STRING_1 = E2.TEST_ENUM2_1.toString();
	public static final String TEST_ENUM2_STRING_2 = E2.TEST_ENUM2_2.toString();

	public static final String TEST_ENUM3_STRING_2 = E3.TEST_ENUM3_2.toString();

	public static class Source {
		String testEnum;
		E testString;
	}

	public static class Destination {
		E testEnum;
		String testString;
	}

	@Test
	void shouldConvertEnumFields() {
		Source src = new Source();
		src.testEnum = TEST_ENUM_STRING_1;
		src.testString = E.TEST_ENUM_2;

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.testEnum, equalTo(E.TEST_ENUM_1));
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

		assertThrows(ConverterException.class, () -> convertFrom(src, Destination::new));
	}

	@Test
	void shouldFailWhenValueOfFailsAndShowFailedFieldsInExceptionMessage() {
		Source src = new Source();
		src.testEnum = TEST_ENUM_STRING_3;

		ConverterException exception = null;
		try {
			convertFrom(src, Destination::new);
		} catch (ConverterException e) {
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
}
