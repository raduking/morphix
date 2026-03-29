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
package org.morphix.lang.collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Lists}.
 *
 * @author Radu Sebastian LAZIN
 */
class ListsTest {

	private static final int TEST_INT = 10;

	@Test
	void shouldReturnTheSameListOnSafe() {
		List<Integer> list = List.of(1, 2, 3);

		List<Integer> result = Lists.safe(list);

		assertThat(result, equalTo(list));
	}

	@Test
	void shouldReturnEmptyListOnSafeWhenParameterIsNull() {
		List<Integer> result = Lists.safe(null);

		assertThat(result, equalTo(Collections.emptyList()));
	}

	@Test
	void shouldReturnTheFirstElementFromAList() {
		List<Integer> list = List.of(1, 2, 3);

		Integer first = Lists.first(list);

		assertThat(first, equalTo(list.getFirst()));
	}

	@Test
	void shouldReturnNullOnFirstIfListIsNull() {
		Integer first = Lists.first(null);

		assertThat(first, nullValue());
	}

	@Test
	void shouldReturnNullOnFirstIfListIsEmpty() {
		Integer first = Lists.first(List.of());

		assertThat(first, nullValue());
	}

	@Test
	void shouldReturnTheFirstElementFromAListIfListIsNotEmptyWithDefaultValue() {
		List<Integer> list = List.of(1, 2, 3);

		Integer first = Lists.first(list, TEST_INT);

		assertThat(first, equalTo(list.getFirst()));
	}

	@Test
	void shouldReturnDefaultValueOnFirstIfListIsNull() {
		Integer first = Lists.first(null, TEST_INT);

		assertThat(first, equalTo(TEST_INT));
	}

	@Test
	void shouldReturnDefaultValueOnFirstIfListIsEmpty() {
		Integer first = Lists.first(List.of(), TEST_INT);

		assertThat(first, equalTo(TEST_INT));
	}

	@Test
	void shouldReturnTheLastElementFromAList() {
		List<Integer> list = List.of(1, 2, 3);

		Integer first = Lists.last(list);

		assertThat(first, equalTo(list.getLast()));
	}

	@Test
	void shouldReturnNullOnLastIfListIsNull() {
		Integer first = Lists.last(null);

		assertThat(first, nullValue());
	}

	@Test
	void shouldReturnNullOnLastIfListIsEmpty() {
		Integer first = Lists.last(List.of());

		assertThat(first, nullValue());
	}

	@Test
	void shouldBuildListFromArray() {
		String[] array = new String[] {
				"mumu",
				"bubu",
				"cucu"
		};

		List<String> list = Lists.asList(array);

		assertThat(list, hasSize(array.length));
		for (int i = 0; i < array.length; ++i) {
			assertThat(list.get(i), equalTo(array[i]));
		}
	}

	@Test
	void shouldBuildEmptyListFromNull() {
		Integer[] array = null;

		List<Integer> list = Lists.asList(array);

		assertThat(list, hasSize(0));
	}

	@Nested
	class MergeTest {

		@Test
		void shouldMerge2SortedLists() {
			List<Integer> l1 = IntStream.rangeClosed(1, 21)
					.filter(n -> n % 2 != 0)
					.boxed()
					.toList();
			List<Integer> l2 = IntStream.rangeClosed(1, 21)
					.filter(n -> n % 2 == 0)
					.boxed()
					.toList();

			List<Integer> expected = IntStream.rangeClosed(1, 21)
					.boxed()
					.toList();

			List<Integer> result = Lists.merge(l1, l2);

			assertThat(result, equalTo(expected));

			result = Lists.merge(l2, l1);

			assertThat(result, equalTo(expected));
		}

		@Test
		void shouldMerge2SortedListsWhenElementsAreNotInterleaved() {
			List<Integer> l1 = IntStream.rangeClosed(1, 20)
					.boxed()
					.toList();
			List<Integer> l2 = IntStream.rangeClosed(21, 40)
					.boxed()
					.toList();

			List<Integer> expected = IntStream.rangeClosed(1, 40)
					.boxed()
					.toList();

			List<Integer> result = Lists.merge(l1, l2);

			assertThat(result, equalTo(expected));

			result = Lists.merge(l2, l1);

			assertThat(result, equalTo(expected));
		}

		@Test
		void shouldSkipNullOrEmptyListAndReturnACopyOfTheOtherOnMerge() {
			List<Integer> l1 = IntStream.rangeClosed(1, 21)
					.filter(n -> n % 2 != 0)
					.boxed()
					.toList();

			List<Integer> result = Lists.merge(l1, null);
			assertThat(result, equalTo(l1));
			assertThat(result, not(sameInstance(l1)));

			result = Lists.merge(null, l1);
			assertThat(result, equalTo(l1));
			assertThat(result, not(sameInstance(l1)));

			result = Lists.merge(null, null);
			assertThat(result, hasSize(0));
		}
	}

	@Nested
	class PartitionTest {

		@Test
		void shouldPartitionIntoEqualSizedChunks() {
			List<List<Integer>> result = Lists.partition(List.of(1, 2, 3, 4, 5, 6), 3);

			assertThat(result, contains(
					List.of(1, 2, 3),
					List.of(4, 5, 6)));
		}

		@Test
		void shouldCreateFinalPartialPartition() {
			List<List<Integer>> result = Lists.partition(List.of(1, 2, 3, 4, 5), 3);

			assertThat(result, contains(
					List.of(1, 2, 3),
					List.of(4, 5)));
		}

		@Test
		void shouldReturnSinglePartitionWhenSizeGreaterThanInput() {
			List<List<Integer>> result = Lists.partition(List.of(1, 2, 3), 10);

			assertThat(result, contains(List.of(1, 2, 3)));
		}

		@Test
		void shouldHandleSingleElement() {
			List<List<Integer>> result = Lists.partition(List.of(1), 3);

			assertThat(result, contains(List.of(1)));
		}

		@Test
		void shouldReturnNoPartitionsForEmptyInput() {
			List<List<Integer>> result = Lists.partition(Collections.emptyList(), 3);

			assertThat(result, empty());
		}

		@Test
		void shouldReturnPartitionForEachElementWhenSizeIsOne() {
			List<List<Integer>> result = Lists.partition(List.of(1, 2, 3), 1);

			assertThat(result, contains(
					List.of(1),
					List.of(2),
					List.of(3)));
		}

		@Test
		void shouldNoPartitionsForZeroPositiveSize() {
			List<List<Integer>> result = Lists.partition(List.of(1, 2, 3), 0);

			assertThat(result, empty());
		}

		@Test
		void shouldNoPartitionsForNonPositiveSize() {
			List<List<Integer>> result = Lists.partition(List.of(1, 2, 3), -10);

			assertThat(result, empty());
		}
	}
}
