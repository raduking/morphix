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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Iterables}.
 *
 * @author Radu Sebastian LAZIN
 */
class IterablesTest {

	private static final String ELEMENT = "element";

	@Test
	void shouldReturnEmptyIterableIfNull() {
		Iterable<String> iterable = null;

		assertNotNull(Iterables.safe(iterable));
		assertFalse(Iterables.safe(iterable).iterator().hasNext());
	}

	@Test
	void shouldReturnEmptyCollectionIfNull() {
		Collection<String> collection = null;

		assertNotNull(Iterables.safe(collection));
		assertFalse(Iterables.safe(collection).iterator().hasNext());
	}

	@Test
	void shouldReturnSameIterableIfNotNull() {
		Iterable<String> iterable = List.of(ELEMENT);

		assertNotNull(Iterables.safe(iterable));

		Iterator<String> iterator = Iterables.safe(iterable).iterator();
		assertTrue(iterator.hasNext());
		assertThat(iterator.next(), equalTo(ELEMENT));
	}

	@Test
	void shouldReturnSameCollectionIfNotNull() {
		Collection<String> collection = Lists.asList(ELEMENT);

		assertNotNull(Iterables.safe(collection));

		Iterator<String> iterator = Iterables.safe(collection).iterator();
		assertTrue(iterator.hasNext());
		assertThat(iterator.next(), equalTo(ELEMENT));
	}

	@Nested
	class PartitionTest {

		@Test
		void shouldPartitionIntoEqualSizedChunks() {
			Iterable<List<Integer>> result = Iterables.partition(List.of(1, 2, 3, 4, 5, 6), 3);

			List<List<Integer>> partitions = toList(result);

			assertThat(partitions, contains(
					List.of(1, 2, 3),
					List.of(4, 5, 6)));
		}

		@Test
		void shouldCreateFinalPartialPartition() {
			Iterable<List<Integer>> result = Iterables.partition(List.of(1, 2, 3, 4, 5), 3);

			List<List<Integer>> partitions = toList(result);

			assertThat(partitions, contains(
					List.of(1, 2, 3),
					List.of(4, 5)));
		}

		@Test
		void shouldReturnSinglePartitionWhenSizeGreaterThanInput() {
			Iterable<List<Integer>> result = Iterables.partition(List.of(1, 2, 3), 10);

			List<List<Integer>> partitions = toList(result);

			assertThat(partitions, contains(List.of(1, 2, 3)));
		}

		@Test
		void shouldHandleSingleElement() {
			Iterable<List<Integer>> result = Iterables.partition(List.of(1), 3);

			List<List<Integer>> partitions = toList(result);

			assertThat(partitions, contains(List.of(1)));
		}

		@Test
		void shouldReturnNoPartitionsForEmptyInput() {
			Iterable<List<Integer>> result = Iterables.partition(Collections.emptyList(), 3);

			List<List<Integer>> partitions = toList(result);

			assertThat(partitions, empty());
		}

		@Test
		void shouldReturnPartitionForEachElementWhenSizeIsOne() {
			Iterable<List<Integer>> result = Iterables.partition(List.of(1, 2, 3), 1);

			List<List<Integer>> partitions = toList(result);

			assertThat(partitions, contains(List.of(1), List.of(2), List.of(3)));
		}

		@Test
		void shouldThrowExceptionForNonPositiveSize() {
			List<Integer> input = List.of(1, 2, 3);

			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Iterables.partition(input, 0));

			assertThat(exception.getMessage(), equalTo("Size must be greater than 0"));
		}

		@Test
		void shouldNotRejectNullIterable() {
			Iterable<List<Integer>> result = Iterables.partition(null, 3);

			List<List<Integer>> partitions = toList(result);

			assertThat(partitions, empty());
		}

		@Test
		void shouldThrowWhenNextCalledAfterIteratorExhausted() {
			Iterator<List<Integer>> it = Iterables.partition(List.of(1, 2), 2).iterator();

			// consume the only partition
			it.next();

			assertThrows(NoSuchElementException.class, it::next);
		}

		private static <T> List<List<T>> toList(final Iterable<List<T>> iterable) {
			List<List<T>> out = new ArrayList<>();
			iterable.forEach(out::add);
			return out;
		}
	}
}
