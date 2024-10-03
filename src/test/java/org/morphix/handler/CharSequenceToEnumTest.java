package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

/**
 * Tests class for {@link CharSequenceToEnum}.
 *
 * @author Radu Sebastian LAZIN
 */
class CharSequenceToEnumTest {

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

	public static final String TEST_ENUM_STRING_1 = E.TEST_ENUM_1.toString();
	public static final String TEST_ENUM2_STRING_1 = E2.TEST_ENUM2_1.toString();

	public static class Source {
		String testEnum;
		E testString;
	}

	public static class Destination {
		E testEnum;
		String testString;
	}

	@Test
	void shouldReturnAsHandled() throws Exception {
		Source src = new Source();
		src.testEnum = TEST_ENUM_STRING_1;
		Destination dst = new Destination();

		Field sField = Source.class.getDeclaredField("testEnum");
		Field dField = Destination.class.getDeclaredField("testEnum");

		FieldHandlerResult result = new CharSequenceToEnum().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

	@Test
	void shouldReturnAsHandledEvenIfSourceIsNull() throws Exception {
		Source src = new Source();
		src.testEnum = null;
		Destination dst = new Destination();

		Field sField = Source.class.getDeclaredField("testEnum");
		Field dField = Destination.class.getDeclaredField("testEnum");

		FieldHandlerResult result = new CharSequenceToEnum().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(BREAK));
	}

	public static class Source2 {
		String testEnum;
	}

	public static class Destination2 {
		E2 testEnum;
	}

	@Test
	void shouldReturnAsHandledWithStaticMethod() throws Exception {
		Source2 src = new Source2();
		src.testEnum = TEST_ENUM2_STRING_1;
		Destination2 dst = new Destination2();

		Field sField = Source2.class.getDeclaredField("testEnum");
		Field dField = Destination2.class.getDeclaredField("testEnum");

		FieldHandlerResult result = new CharSequenceToEnum().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

}
