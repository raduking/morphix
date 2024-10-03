package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

class StaticFieldSkipperTest {

	static class Source {
		public static Integer x = 10;
	}

	static class Destination {
		public static Integer x = 11;
	}

	@Test
	void shouldReturnBreakOnHandleForStaticSourceField() throws Exception {
		Source src = new Source();
		Destination dst = new Destination();

		Field sField = Source.class.getDeclaredField("x");
		Field dField = Destination.class.getDeclaredField("x");

		FieldHandlerResult result = new StaticFieldSkipper().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(BREAK));
	}
}
