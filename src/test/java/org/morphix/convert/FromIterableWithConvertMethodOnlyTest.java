/*
 * Copyright 2026 the original author or authors.
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
package org.morphix.convert;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.IterableConversions.convertIterable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.convert.function.SimpleConverter;

/**
 * Test class for {@link IterableConversions#convertIterable(Iterable, SimpleConverter)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FromIterableWithConvertMethodOnlyTest {

	private static final String TEST_STRING = "testString";

	private static final long TEST_PRIMITIVE_LONG = 17L;
	private static final Long TEST_LONG = TEST_PRIMITIVE_LONG;

	private static final String LOCAL_DATE_STRING = "2016-11-28";
	private static final LocalDate LOCAL_DATE = LocalDate.parse(LOCAL_DATE_STRING);

	public static class Src {
		Long lng;
		String date;
		String noConversion;
	}

	public static class NoConversion {
		String test;
	}

	public static class Dst {
		Long lng;
		LocalDate date;
		NoConversion noConversion;
	}

	private static Dst convertMethod(final Src src) {
		return convertFrom(src, Dst::new, (s, d) -> {
			NoConversion nc = new NoConversion();
			nc.test = s.noConversion;
			d.noConversion = nc;
		});
	}

	@Test
	void shouldCallConvertMethodOnIterables() {
		final int listSize = 7;
		List<Src> src7List = new ArrayList<>(listSize);
		for (int i = 0; i < listSize; ++i) {
			Src src = new Src();
			src.lng = TEST_LONG;
			src.date = LOCAL_DATE_STRING;
			src.noConversion = TEST_STRING;
			src7List.add(src);
		}

		List<Dst> result = convertIterable(src7List, FromIterableWithConvertMethodOnlyTest::convertMethod).toList();

		assertThat(result, hasSize(equalTo(src7List.size())));

		for (int i = 0; i < listSize; ++i) {
			Dst dst = result.get(i);

			assertThat(dst.lng, equalTo(TEST_LONG));
			assertThat(dst.date, equalTo(LOCAL_DATE));
			assertThat(dst.noConversion.test, equalTo(TEST_STRING));
		}
	}

}
