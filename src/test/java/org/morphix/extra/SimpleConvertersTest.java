package org.morphix.extra;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.morphix.function.SimpleConverter;

/**
 * Test class for simple converters.
 *
 * @author Radu Sebastian LAZIN
 */
class SimpleConvertersTest {

	@Test
	void shouldReturnEmptySimpleConverterWhenNullIsGivenAsParameter() {
		SimpleConverters result = SimpleConverters.of((SimpleConverter<?, ?>[]) null);

		assertThat(result, equalTo(SimpleConverters.empty()));
	}

	@Test
	void shouldReturnEmptySimpleConverterWhenArrayIsEmptyAsParameter() {
		SimpleConverters result = SimpleConverters.of(new SimpleConverter<?, ?>[] {});

		assertThat(result, equalTo(SimpleConverters.empty()));
	}

	@Test
	void shouldReturnFalseOnNullEquals() {
		SimpleConverters sc = SimpleConverters.of(new SimpleConverter<?, ?>[] {});

		boolean result = sc.equals(null);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseOnOtherObjectEquals() {
		SimpleConverters sc = SimpleConverters.of(new SimpleConverter<?, ?>[] {});
		Object mumu = "mumu";

		boolean result = sc.equals(mumu);

		assertThat(result, equalTo(false));
	}

	private static Long convert(final String s) {
		return Long.parseLong(s);
	}

	@Test
	void shouldReturnTrueWhenSimpleConvertersAreEqual() {
		SimpleConverters sc1 = SimpleConverters.of(SimpleConvertersTest::convert);
		SimpleConverters sc2 = SimpleConverters.of(SimpleConvertersTest::convert);

		boolean result = sc1.equals(sc2);

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnFalseWhenSimpleConvertersAreNotEqual() {
		SimpleConverters scs1 = SimpleConverters.of(SimpleConvertersTest::convert);
		SimpleConverter<String, Long> sc1 = SimpleConvertersTest::convert;
		SimpleConverter<String, Long> sc2 = SimpleConvertersTest::convert;
		SimpleConverters scs2 = SimpleConverters.of(sc1, sc2);

		boolean result = scs1.equals(scs2);

		assertThat(result, equalTo(false));
	}

}
