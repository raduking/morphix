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
package org.morphix.lang.cache;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.lang.cache.StrictLRUCache.Node;

/**
 * Test class for {@link StrictLRUCache}.
 *
 * @author Radu Sebastian LAZIN
 */
class StrictLRUCacheTest {

	private static final Logger LOGGER = Logger.getLogger(StrictLRUCacheTest.class.getName());

	private static final int CACHE_CAPACITY = 3;

	private StrictLRUCache<String, String> cache;

	@BeforeEach
	void setUp() {
		cache = new StrictLRUCache<>(CACHE_CAPACITY);
	}

	@Nested
	class ConstructionTests {

		@Test
		void shouldHaveInitialSizeZero() {
			assertThat(cache.size(), is(equalTo(0)));
		}

		@Test
		void shouldHaveCorrectCapacity() {
			assertThat(cache.capacity(), is(equalTo(CACHE_CAPACITY)));
		}

		@Test
		void shouldThrowExceptionForNonPositiveCapacity() {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new StrictLRUCache<String, String>(0));

			assertThat(exception.getMessage(), is(equalTo("Cache capacity must be greater than 0")));
		}
	}

	@Nested
	class GetTests {

		@Test
		void shouldReturnNullForNonExistentKey() {
			assertThat(cache.get("missing"), is(nullValue()));
		}

		@Test
		void shouldReturnValueForExistingKey() {
			cache.computeIfAbsent("key1", k -> "value1");

			assertThat(cache.get("key1"), is(equalTo("value1")));
		}

		@Test
		void shouldNotChangeSizeWhenGettingExistingKey() {
			cache.computeIfAbsent("key1", k -> "value1");
			assertThat(cache.size(), is(equalTo(1)));

			cache.get("key1");
			assertThat(cache.size(), is(equalTo(1)));
		}
	}

	@Nested
	class ComputeIfAbsentTests {

		@Test
		void shouldAddNewKeyValuePair() {
			String result = cache.computeIfAbsent("key1", k -> "value1");

			assertThat(result, is(equalTo("value1")));
			assertThat(cache.get("key1"), is(equalTo("value1")));
			assertThat(cache.size(), is(equalTo(1)));
		}

		@Test
		void shouldNotAddEntryWhenMappingFunctionReturnsNull() {
			String result = cache.computeIfAbsent("key1", k -> null);

			assertThat(result, is(nullValue()));
			assertThat(cache.get("key1"), is(nullValue()));
			assertThat(cache.size(), is(equalTo(0)));
		}

		@Test
		void shouldReturnExistingValueWithoutRecomputing() {
			AtomicInteger computeCount = new AtomicInteger(0);
			Function<String, String> valueFunction = k -> {
				computeCount.incrementAndGet();
				return "value1";
			};

			cache.computeIfAbsent("key1", valueFunction);
			String result = cache.computeIfAbsent("key1", valueFunction);

			assertThat(result, is(equalTo("value1")));
			assertThat(computeCount.get(), is(equalTo(1)));
		}
	}

	@Nested
	class EvictionTests {

		@Test
		void shouldEvictHeadWhenAddingBeyondCapacity() {
			for (int i = 0; i <= CACHE_CAPACITY; ++i) {
				int index = i + 1;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}

			assertThat(cache.size(), is(equalTo(CACHE_CAPACITY)));
			assertThat(cache.get("key1"), is(nullValue()));

			for (int i = 1; i <= CACHE_CAPACITY; ++i) {
				int index = i + 1;
				assertThat(cache.get("key" + index), is(equalTo("value" + index)));
			}
		}

		@Test
		void shouldEvictLeastRecentlyUsedEntryWhenAddingBeyondCapacity() {
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = i + 1;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}
			int lruIndex = 1;
			cache.get("key" + lruIndex);

			int newIndex = CACHE_CAPACITY + 1;
			// should evict lruIndex + 1, which is the least recently used entry after accessing lruIndex
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			assertThat(cache.size(), is(equalTo(CACHE_CAPACITY)));
			assertThat(cache.get("key" + lruIndex), is(equalTo("value" + lruIndex)));
			assertThat(cache.get("key" + (lruIndex + 1)), is(nullValue()));
			for (int i = lruIndex + 2; i <= newIndex; ++i) {
				assertThat(cache.get("key" + i), is(equalTo("value" + i)));
			}
		}

		@Test
		void shouldNotEvictWhenAddingToNonFullCache() {
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = i + 1;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}

			assertThat(cache.size(), is(equalTo(CACHE_CAPACITY)));
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = i + 1;
				assertThat(cache.get("key" + index), is(equalTo("value" + index)));
			}
		}
	}

	@Nested
	class OrderingTests {

		@Test
		void shouldMoveGetAccessedHeadEntryToTail() {
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = i + 1;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}
			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			int accessedIndex = 1;
			// move "key1" to tail by accessing head with get()
			cache.get("key" + accessedIndex);

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			assertThat(cache.head().value(), is(equalTo("value" + (accessedIndex + 1))));

			int newIndex = CACHE_CAPACITY + 1;
			// remove head ("key2") and add new entry at tail
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			// check from head
			Node<String, String> current = cache.head();
			int initialIndex = 3;
			for (int i = 0, j = 0; i < CACHE_CAPACITY; ++i) {
				if (i + 1 == CACHE_CAPACITY - 1) {
					assertThat(current.key(), is(equalTo("key" + accessedIndex)));
				} else {
					assertThat(current.key(), is(equalTo("key" + (j + initialIndex))));
					++j;
				}
				current = current.next();
			}

			// check from tail
			current = cache.tail();
			for (int i = CACHE_CAPACITY - 1, j = CACHE_CAPACITY + 1; i >= 0; --i) {
				if (i + 1 == CACHE_CAPACITY - 1) {
					assertThat(current.key(), is(equalTo("key" + accessedIndex)));
				} else {
					assertThat(current.key(), is(equalTo("key" + j)));
					--j;
				}
				current = current.prev();
			}
		}

		@Test
		void shouldMoveGetAccessedMiddleEntryToTail() {
			int initialIndex = 1;
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = initialIndex + i;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}
			int accessedIndex = 2;
			// move "key2" to tail by accessing middle key with get()
			cache.get("key" + accessedIndex);

			assertThat(cache.head().value(), is(equalTo("value" + initialIndex)));

			int newIndex = CACHE_CAPACITY + 1;
			// remove head ("key1") and add new entry at tail
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			Node<String, String> current = cache.head();
			for (int i = 3; i <= CACHE_CAPACITY; ++i) {
				int index = i;
				assertThat(current.key(), is(equalTo("key" + index)));
				current = current.next();
			}
		}

		@Test
		void shouldKeepGetAccessedTailEntryToTail() {
			int initialIndex = 1;
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = initialIndex + i;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}
			int accessedIndex = CACHE_CAPACITY;
			// keep tail the same by accessing tail with get()
			cache.get("key" + accessedIndex);

			assertThat(cache.head().value(), is(equalTo("value" + initialIndex)));

			int newIndex = CACHE_CAPACITY + 1;
			// remove head ("key1") and add new entry at tail
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			Node<String, String> current = cache.head();
			for (int i = 2; i <= CACHE_CAPACITY; ++i) {
				int index = i;
				assertThat(current.key(), is(equalTo("key" + index)));
				current = current.next();
			}
		}

		@Test
		void shouldMoveComputeIfAbsentAccessedHeadEntryToTail() {
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = i + 1;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}
			int accessedIndex = 1;
			// move "key1" to tail by accessing head with get()
			cache.computeIfAbsent("key" + accessedIndex, k -> "doNotCompute");

			int newIndex = CACHE_CAPACITY + 1;
			// remove head ("key2") and add new entry at tail
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			Node<String, String> current = cache.head();
			for (int i = 3; i <= CACHE_CAPACITY; ++i) {
				int index = i;
				assertThat(current.key(), is(equalTo("key" + index)));
				current = current.next();
			}
		}

		@Test
		void shouldMoveComputeIfAbsentAccessedMiddleEntryToTail() {
			int initialIndex = 1;
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = initialIndex + i;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}
			int accessedIndex = 2;
			// move "key2" to tail by accessing middle key with get()
			cache.computeIfAbsent("key" + accessedIndex, k -> "doNotCompute");

			assertThat(cache.head().value(), is(equalTo("value" + initialIndex)));

			int newIndex = CACHE_CAPACITY + 1;
			// remove head ("key1") and add new entry at tail
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			Node<String, String> current = cache.head();
			for (int i = 3; i <= CACHE_CAPACITY; ++i) {
				int index = i;
				assertThat(current.key(), is(equalTo("key" + index)));
				current = current.next();
			}
		}

		@Test
		void shouldKeepComputeIfAbsentAccessedTailEntryToTail() {
			int initialIndex = 1;
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = initialIndex + i;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}
			int accessedIndex = CACHE_CAPACITY;
			// keep tail the same by accessing tail with get()
			cache.computeIfAbsent("key" + accessedIndex, k -> "doNotCompute");

			assertThat(cache.head().value(), is(equalTo("value" + initialIndex)));

			int newIndex = CACHE_CAPACITY + 1;
			// remove head ("key1") and add new entry at tail
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			Node<String, String> current = cache.head();
			for (int i = 2; i <= CACHE_CAPACITY; ++i) {
				int index = i;
				assertThat(current.key(), is(equalTo("key" + index)));
				current = current.next();
			}
		}
	}

	@Nested
	class ClearTests {

		@Test
		void shouldClearAllEntries() {
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = i + 1;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}

			cache.clear();

			assertThat(cache.size(), is(equalTo(0)));
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = i + 1;
				assertThat(cache.get("key" + index), is(nullValue()));
			}
		}
	}

	private static String toString(final Node<String, String> head) {
		Set<Node<String, String>> visited = new HashSet<>();
		StringBuilder sb = new StringBuilder();
		Node<String, String> current = head;
		while (current != null) {
			if (!visited.add(current)) {
				throw new IllegalStateException("Cycle detected in the linked list");
			}
			sb.append(current.key()).append("->");
			current = current.next();
		}
		sb.append("null");
		return sb.toString();
	}
}
