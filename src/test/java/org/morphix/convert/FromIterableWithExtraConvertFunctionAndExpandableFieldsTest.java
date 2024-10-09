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
package org.morphix.convert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.morphix.convert.Conversions.convertFrom;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.convert.annotation.Expandable;
import org.morphix.convert.function.ConvertFunction;
import org.morphix.lang.function.InstanceFunction;

/**
 * Test class for {@link Conversions#convertFrom(Object, InstanceFunction, ConvertFunction, List)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FromIterableWithExtraConvertFunctionAndExpandableFieldsTest {

	private static final int TEST_INT1 = 666;
	private static final int TEST_INT2 = 667;
	private static final int TEST_INT3 = 668;
	private static final int TEST_INT4 = 669;

	private static final long TEST_LONG1 = 42L;

	private static final String TEST_INT_STRING1 = String.valueOf(TEST_INT1);
	private static final String TEST_INT_STRING2 = String.valueOf(TEST_INT2);

	private static final int ADDED_INT = 100;

	private static final String PREFIX = "Some Prefix ";

	static class Src {

		@Expandable
		List<String> strings;

		@Expandable
		List<Integer> integers;

		List<Long> longs;

		List<String> getStrings() {
			return strings;
		}

		void setStrings(final List<String> strings) {
			this.strings = strings;
		}

		List<Integer> getIntegers() {
			return integers;
		}

		void setIntegers(final List<Integer> integers) {
			this.integers = integers;
		}

		List<Long> getLongs() {
			return longs;
		}

		void setLongs(final List<Long> longs) {
			this.longs = longs;
		}
	}

	static class Dst {

		List<Integer> strings;

		List<String> integers;

		List<String> longs;

		List<Integer> getStrings() {
			return strings;
		}

		List<String> getIntegers() {
			return integers;
		}

		List<String> getLongs() {
			return longs;
		}
	}

	@Test
	void shouldConvertExpandableFieldsAndExtraConvertFunction() {
		Src src = new Src();
		src.setStrings(List.of(TEST_INT_STRING1, TEST_INT_STRING2));
		src.setIntegers(List.of(TEST_INT3, TEST_INT4));
		src.setLongs(List.of(TEST_LONG1));

		List<String> expandedFields = Collections.singletonList("integers");

		Dst dst = convertFrom(src, Dst::new, (final Src s, final Dst d) -> {
			for (Integer integerValue : s.getIntegers()) {
				d.getIntegers().add(PREFIX + integerValue);
			}
			for (String stringValue : s.getStrings()) {
				d.getStrings().add(Integer.valueOf(stringValue) + ADDED_INT);
			}
		}, expandedFields);

		assertNotNull(dst);
		assertThat(dst.getIntegers(), hasSize(4));
		assertThat(dst.getStrings(), hasSize(2));
		assertThat(dst.getLongs(), hasSize(1));

		List<String> convertedWithExtraFunction = dst.getIntegers().stream().filter(e -> e.startsWith(PREFIX)).toList();
		assertThat(convertedWithExtraFunction, hasSize(2));
	}

	@Test
	void shouldConvertWithNoExpandableFields() {
		Src src = new Src();
		src.setStrings(List.of(TEST_INT_STRING1, TEST_INT_STRING2));
		src.setIntegers(List.of(TEST_INT3, TEST_INT4));
		src.setLongs(List.of(TEST_LONG1));

		Dst dst = convertFrom(src, Dst::new, (final Src s, final Dst d) -> {
			for (Long longValue : s.getLongs()) {
				d.getLongs().add(PREFIX + longValue);
			}
		}, (List<String>) null);

		assertNotNull(dst);
		assertThat(dst.getStrings(), hasSize(2));
		assertThat(dst.getIntegers(), hasSize(2));
		assertThat(dst.getLongs(), hasSize(2));

		List<String> convertedWithExtraFunction = dst.getLongs().stream().filter(e -> e.startsWith(PREFIX)).toList();
		assertThat(convertedWithExtraFunction, hasSize(1));
	}

}
