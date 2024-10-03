package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

/**
 * Tests conversions from any to char array (char[]).
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToCharArrayTest {

	public static class Source {
		Long id;
		boolean good;
	}

	public static class Destination {
		char[] id;
		char[] good;
	}

	@Test
	void shouldConsiderTheFieldHandledEvenIfTheSourceHasNullValue() throws Exception {
		Source src = new Source();
		Destination dst = new Destination();

		Field sField = Source.class.getDeclaredField("id");
		Field dField = Destination.class.getDeclaredField("id");

		FieldHandlerResult result = new AnyToCharArray().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

}
