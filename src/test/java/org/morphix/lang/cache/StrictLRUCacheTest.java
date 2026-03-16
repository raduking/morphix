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
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.cache.StrictLRUCache.Node;

/**
 * Test class for {@link StrictLRUCache}.
 *
 * @author Radu Sebastian LAZIN
 */
class StrictLRUCacheTest extends LRUCacheTest {

	private static final Logger LOGGER = Logger.getLogger(StrictLRUCacheTest.class.getName());

	StrictLRUCache<String, String> cache;

	@Override
	LRUCache<String, String> newCache() {
		return new StrictLRUCache<>(CACHE_CAPACITY);
	}

	@Override
	@BeforeEach
	void setUp() {
		super.setUp();
		cache = JavaObjects.cast(cache());
	}

	@Nested
	class ConstructionTests {

		@Test
		void shouldThrowExceptionForNonPositiveCapacity() {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new StrictLRUCache<String, String>(0));

			assertThat(exception.getMessage(), is(equalTo("Cache capacity must be greater than 0")));
		}
	}

	@Nested
	class OrderingTests {

		@Test
		void shouldMoveGetAccessedHeadEntryToTail() {
			int initialIndex = 1;
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = initialIndex + i;
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

			initialIndex = CACHE_CAPACITY;

			// check from head
			Node<String, String> current = cache.head();
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
			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			int accessedIndex = 2;
			// move "key2" to tail by accessing middle key with get()
			cache.get("key" + accessedIndex);

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			assertThat(cache.head().value(), is(equalTo("value" + initialIndex)));

			int newIndex = CACHE_CAPACITY + 1;
			// remove head ("key1") and add new entry at tail
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			initialIndex = CACHE_CAPACITY;

			// check from head
			Node<String, String> current = cache.head();
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
		void shouldKeepGetAccessedTailEntryToTail() {
			int initialIndex = 1;
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = initialIndex + i;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}
			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			int accessedIndex = CACHE_CAPACITY;
			// keep tail the same by accessing tail with get()
			cache.get("key" + accessedIndex);

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			assertThat(cache.head().value(), is(equalTo("value" + initialIndex)));

			int newIndex = CACHE_CAPACITY + 1;
			// remove head ("key1") and add new entry at tail
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			initialIndex = 2;

			// check from head
			Node<String, String> current = cache.head();
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				assertThat(current.key(), is(equalTo("key" + (i + initialIndex))));
				current = current.next();
			}

			// check from tail
			current = cache.tail();
			for (int i = CACHE_CAPACITY - 1; i >= 0; --i) {
				assertThat(current.key(), is(equalTo("key" + (i + initialIndex))));
				current = current.prev();
			}
		}

		@Test
		void shouldMoveComputeIfAbsentAccessedHeadEntryToTail() {
			int initialIndex = 1;
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = initialIndex + i;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}
			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			int accessedIndex = 1;
			// move "key1" to tail by accessing head with get()
			cache.computeIfAbsent("key" + accessedIndex, k -> "doNotCompute");

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			assertThat(cache.head().value(), is(equalTo("value" + (accessedIndex + 1))));

			int newIndex = CACHE_CAPACITY + 1;
			// remove head ("key2") and add new entry at tail
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			initialIndex = CACHE_CAPACITY;

			// check from head
			Node<String, String> current = cache.head();
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
		void shouldMoveComputeIfAbsentAccessedMiddleEntryToTail() {
			int initialIndex = 1;
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = initialIndex + i;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}
			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			int accessedIndex = 2;
			// move "key2" to tail by accessing middle key with get()
			cache.computeIfAbsent("key" + accessedIndex, k -> "doNotCompute");

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			assertThat(cache.head().value(), is(equalTo("value" + initialIndex)));

			int newIndex = CACHE_CAPACITY + 1;
			// remove head ("key1") and add new entry at tail
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			initialIndex = CACHE_CAPACITY;

			// check from head
			Node<String, String> current = cache.head();
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
		void shouldKeepComputeIfAbsentAccessedTailEntryToTail() {
			int initialIndex = 1;
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = initialIndex + i;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}
			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			int accessedIndex = CACHE_CAPACITY;
			// keep tail the same by accessing tail with get()
			cache.computeIfAbsent("key" + accessedIndex, k -> "doNotCompute");

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			assertThat(cache.head().value(), is(equalTo("value" + initialIndex)));

			int newIndex = CACHE_CAPACITY + 1;
			// remove head ("key1") and add new entry at tail
			cache.computeIfAbsent("key" + newIndex, k -> "value" + newIndex);

			LOGGER.info("LRU cache: " + StrictLRUCacheTest.toString(cache.head()));

			initialIndex = 2;

			// check from head
			Node<String, String> current = cache.head();
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				assertThat(current.key(), is(equalTo("key" + (i + initialIndex))));
				current = current.next();
			}

			// check from tail
			current = cache.tail();
			for (int i = CACHE_CAPACITY - 1; i >= 0; --i) {
				assertThat(current.key(), is(equalTo("key" + (i + initialIndex))));
				current = current.prev();
			}
		}
	}

	@Nested
	class RemoveHeadTests {

		@Test
		void shouldRemoveHeadEntry() {
			for (int i = 0; i < CACHE_CAPACITY; ++i) {
				int index = i + 1;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}

			cache.removeHead();

			assertThat(cache.size(), is(equalTo(CACHE_CAPACITY - 1)));
			assertThat(cache.get("key1"), is(nullValue()));
			for (int i = 1; i < CACHE_CAPACITY; ++i) {
				int index = i + 1;
				assertThat(cache.get("key" + index), is(equalTo("value" + index)));
			}
		}

		@Test
		void shouldNotRemoveHeadWhenCacheIsEmpty() {
			assertThat(cache.size(), is(equalTo(0)));

			cache.removeHead();

			assertThat(cache.size(), is(equalTo(0)));
			assertThat(cache.head(), is(nullValue()));
			assertThat(cache.tail(), is(nullValue()));
		}

		@Test
		void shouldRemoveHeadWhenCacheHasOneEntry() {
			cache.computeIfAbsent("key1", k -> "value1");

			assertThat(cache.size(), is(equalTo(1)));

			cache.removeHead();

			assertThat(cache.size(), is(equalTo(0)));
			assertThat(cache.get("key1"), is(nullValue()));

			assertThat(cache.size(), is(equalTo(0)));
			assertThat(cache.head(), is(nullValue()));
			assertThat(cache.tail(), is(nullValue()));
		}
	}

	@Nested
	class ToTailTests {

		@Test
		void shouldAddNewNodeToTail() {
			Node<String, String> node = new Node<>("key1", "value1");
			cache.setHead(node);
			cache.setTail(node);

			cache.toTail(node);

			assertThat(cache.head(), sameInstance(node));
			assertThat(cache.tail(), sameInstance(node));
		}

		@Test
		void shouldAddNewNodeToTailOnTwoNodes() {
			Node<String, String> node1 = new Node<>("key1", "value1");
			Node<String, String> node2 = new Node<>("key2", "value2");
			node1.setNext(node2);
			node2.setPrev(node1);

			cache.setHead(node1);
			cache.setTail(node2);

			cache.toTail(node1);

			assertThat(cache.head(), sameInstance(node2));
			assertThat(cache.tail(), sameInstance(node1));
		}

		@Test
		void shouldAddHeadNodeToTail() {
			Node<String, String> node1 = new Node<>("key1", "value1");
			Node<String, String> node2 = new Node<>("key2", "value2");
			Node<String, String> node3 = new Node<>("key3", "value3");
			node1.setNext(node2);
			node2.setPrev(node1);
			node2.setNext(node3);
			node3.setPrev(node2);

			cache.setHead(node1);
			cache.setTail(node3);

			cache.toTail(node1);

			assertThat(cache.head(), sameInstance(node2));
			assertThat(cache.tail(), sameInstance(node1));
		}

		@Test
		void shouldAddMiddleNodeToTail() {
			Node<String, String> node1 = new Node<>("key1", "value1");
			Node<String, String> node2 = new Node<>("key2", "value2");
			Node<String, String> node3 = new Node<>("key3", "value3");
			node1.setNext(node2);
			node2.setPrev(node1);
			node2.setNext(node3);
			node3.setPrev(node2);

			cache.setHead(node1);
			cache.setTail(node3);

			cache.toTail(node2);

			assertThat(cache.head(), sameInstance(node1));
			assertThat(cache.tail(), sameInstance(node2));
		}

		@Test
		void shouldAddTailNodeToTail() {
			Node<String, String> node1 = new Node<>("key1", "value1");
			Node<String, String> node2 = new Node<>("key2", "value2");
			Node<String, String> node3 = new Node<>("key3", "value3");
			node1.setNext(node2);
			node2.setPrev(node1);
			node2.setNext(node3);
			node3.setPrev(node2);

			cache.setHead(node1);
			cache.setTail(node3);

			cache.toTail(node3);

			assertThat(cache.head(), sameInstance(node1));
			assertThat(cache.tail(), sameInstance(node3));
		}

		@Test
		void shouldNotAddNullNodeToTail() {
			cache.setHead(null);
			cache.setTail(null);

			cache.toTail(null);

			assertThat(cache.head(), is(nullValue()));
			assertThat(cache.tail(), is(nullValue()));
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
