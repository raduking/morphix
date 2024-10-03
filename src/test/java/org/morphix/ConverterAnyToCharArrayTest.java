package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Tests conversions from any to char array (char[]).
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterAnyToCharArrayTest {

	private static final Long TEST_LONG = 1234321L;
	private static final char[] TEST_CHAR_ARRAY_LONG = { '1', '2', '3', '4', '3', '2', '1' };

	private static final boolean TEST_BOOLEAN = true;
	private static final char[] TEST_CHAR_ARRAY_BOOLEAN = { 't', 'r', 'u', 'e' };

	private static final char[] TEST_CHAR_ARRAY_BOOLEAN_DEFAULT = { 'f', 'a', 'l', 's', 'e' };

	public static class Source {
		Long id;
		boolean good;
	}

	public static class Destination {
		char[] id;
		char[] good;
	}

	@Test
	void shouldConvertAnyToCharArray() {
		Source src = new Source();
		src.id = TEST_LONG;
		src.good = TEST_BOOLEAN;

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(TEST_CHAR_ARRAY_LONG));
		assertThat(dst.good, equalTo(TEST_CHAR_ARRAY_BOOLEAN));
	}

	@Test
	void shouldSkipNullValues() {
		Source src = new Source();

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(null));
		assertThat(dst.good, equalTo(TEST_CHAR_ARRAY_BOOLEAN_DEFAULT));
	}

	@Test
	void shouldConsiderTheFieldHandledEvenIfTheSourceHasNullValue() throws Exception {
		Source src = new Source();
		Destination dst = new Destination();

		Field sField = Source.class.getDeclaredField("id");
		Field dField = Destination.class.getDeclaredField("id");

		FieldHandlerResult result = DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_CHAR_ARRAY.handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}
}
