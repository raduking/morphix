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
import static org.morphix.convert.IterableConversions.convertIterable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Test class for extended iterable conversions.
 *
 * @author Radu Sebastian LAZIN
 */
class FromIterableTest {

	private static final int SIZE = 10;

	static class Src {
		public Integer s;

		public Src(final Integer s) {
			this.s = s;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof Src src) {
				return Objects.equals(s, src.s);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return s == null ? super.hashCode() : s.hashCode();
		}
	}

	static class Dst {
		public String s;

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof Dst dst) {
				return Objects.equals(s, dst.s);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return s == null ? super.hashCode() : s.hashCode();
		}
	}

	@Test
	void shouldConvertIterablesToList() {
		List<Src> list = IntStream.range(0, SIZE).boxed().map(Src::new).toList();

		List<Dst> result = convertIterable(list, Dst::new).toList();

		assertThat(result, hasSize(list.size()));

		for (int i = 0; i < SIZE; ++i) {
			assertThat(result.get(i).s, equalTo(String.valueOf(i)));
		}
	}

	@Test
	void shouldConvertIterablesToSet() {
		final int value = 1;
		List<Src> list = IntStream.generate(() -> value).limit(SIZE).boxed().map(Src::new).toList();

		Set<Dst> resultSet = convertIterable(list, Dst::new).toSet();

		assertThat(resultSet, hasSize(1));

		assertThat(resultSet.iterator().next().s, equalTo(String.valueOf(value)));
	}

}
