package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;

import org.junit.jupiter.api.Test;

/**
 * Tests conversions from any to {@link String}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterAnyToStringTest {

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

}
