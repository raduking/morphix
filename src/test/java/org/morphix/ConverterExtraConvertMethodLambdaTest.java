package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;

import org.junit.jupiter.api.Test;

/**
 * Tests extra conversion lambdas.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterExtraConvertMethodLambdaTest {

	public static class Source {
		Long testLong1;
		Long testLong2;
	}

	public static class Destination {
		Long testLong;
	}

	@Test
	void shouldConvertExtraFields() {
		Source s = new Source();
		s.testLong1 = 17L;
		s.testLong2 = 19L;

		Long expected = s.testLong1 + s.testLong2;

		Destination d = convertFrom(s, Destination::new, (src, dst) -> {
			dst.testLong = src.testLong1 + src.testLong2;
		});

		assertThat(d.testLong, equalTo(expected));
	}

}
