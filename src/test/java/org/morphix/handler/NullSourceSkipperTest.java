package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

/**
 * Test class for {@link NullSourceSkipper}.
 *
 * @author Radu Sebastian LAZIN
 */
class NullSourceSkipperTest {

	public static class Source {
		Integer s;
	}

	public static class Destination {
		String s;
	}

	@Test
	void shouldReturnTrueOnConditionNoMatterTheArguments() {
		boolean result = new NullSourceSkipper().condition(null, null);

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnBreakOnHandleForNullSource() throws Exception {
		Source src = new Source();
		Destination dst = new Destination();

		Field sField = Source.class.getDeclaredField("s");
		Field dField = Destination.class.getDeclaredField("s");

		FieldHandlerResult result = new NullSourceSkipper().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(BREAK));
	}

	@Test
	void shouldReturnFalseWhenTestingEqualityWithNull() {
		boolean result = new NullSourceSkipper().equals(null);

		assertThat(result, equalTo(false));
	}
}
