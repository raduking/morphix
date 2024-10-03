package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.Converted.convert;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests conversions from any to {@link Iterable}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterAnyToIterableTest {

	private static final Long TEST_LONG = 19L;
	private static final String TEST_STRING_LONG = "19";

	public static class Source {
		String testLong;
	}

	public static class Destination {
		List<Long> testLong;

		public List<Long> getTestLong() {
			return testLong;
		}
	}

	@Test
	void shouldConvertAnyToList() {
		Source s = new Source();
		s.testLong = TEST_STRING_LONG;

		Destination d = convertFrom(s, Destination::new);

		assertThat(d.testLong, hasSize(1));
		assertThat(d.testLong.get(0), equalTo(TEST_LONG));
	}

	@Test
	void shouldConvertAnyToListWithNewApi() {
		Source s = new Source();
		s.testLong = TEST_STRING_LONG;

		Destination d = convert(s).to(Destination::new);

		assertThat(d.testLong, hasSize(1));
		assertThat(d.testLong.get(0), equalTo(TEST_LONG));
	}

	@Test
	void shouldNotConvertNullSource() {
		Source s = new Source();

		Destination d = convertFrom(s, Destination::new);

		assertThat(d.testLong, nullValue());
	}

}
