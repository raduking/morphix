/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.convert.extras;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.convert.function.SimpleConverter;

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
	void shouldReturnEmptyWhenBuildingSimpleConvertersFromNullList() {
		SimpleConverters result = SimpleConverters.of((List<SimpleConverter<?, ?>>) null);

		assertThat(result, equalTo(SimpleConverters.empty()));
	}

	@Test
	void shouldReturnEmptyWhenBuildingSimpleConvertersFromEmptyList() {
		SimpleConverters result = SimpleConverters.of(Collections.emptyList());

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
