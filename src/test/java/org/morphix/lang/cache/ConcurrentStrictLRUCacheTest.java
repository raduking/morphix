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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.cache.StrictLRUCache.Node;
import org.morphix.utils.ConcurrencyTestProperties;
import org.morphix.utils.ConcurrencyTestResults;

/**
 * Test class for {@link TestStrictLRUCache}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConcurrentStrictLRUCacheTest extends StrictLRUCacheTest {

	private static final Logger LOGGER = Logger.getLogger(ConcurrentStrictLRUCacheTest.class.getName());

	@Override
	StrictLRUCache<String, String> newCache() {
		return new ConcurrentStrictLRUCache<>(CACHE_CAPACITY);
	}

	@Test
	void shouldHaveTheCorrectCacheInstance() {
		assertThat(cache, is(instanceOf(ConcurrentStrictLRUCache.class)));
	}

	@Test
	void shouldHaveConcurrentHashMapAsStorage() {
		assertThat(cache.storage(), is(instanceOf(ConcurrentHashMap.class)));
	}

	@Test
	@Timeout(5)
	void shouldHandleConcurrentAccess() throws InterruptedException {
		ConcurrencyTestProperties properties = new ConcurrencyTestProperties.Builder()
				.threadCount(100)
				.iterationsPerThread(1000)
				.keySpace(7)
				.timeout(Duration.ofSeconds(1))
				.logger(LOGGER)
				.build();

		ConcurrencyTestResults result = stressTest(cache, properties);

		assertTrue(result.finished().get(), "All threads should complete in reasonable time");
		assertThat(result.failedThreads().get(), is(0L));

		assertThat(cache.size(), is(equalTo(CACHE_CAPACITY)));
	}

	@Test
	@Timeout(5)
	void shouldSurviveHighContentionSize() throws Exception {
		int capacity = 16;
		ConcurrentStrictLRUCache<Integer, Integer> cache = new ConcurrentStrictLRUCache<>(capacity);
		ConcurrencyTestProperties properties = new ConcurrencyTestProperties.Builder()
				.threadCount(32)
				.iterationsPerThread(200_000)
				.cacheCapacity(capacity)
				.keySpace(64)
				.cacheConsumer(ConcurrentStrictLRUCacheTest::assertSizeInvariant)
				.timeout(Duration.ofSeconds(5))
				.frequencyModulo(1023)
				.logger(LOGGER)
				.build();

		stateStressTest(cache, properties);

		assertThat(cache.size(), lessThanOrEqualTo(capacity));
	}

	static <K, V> void assertSizeInvariant(final Cache<?, ?> cache, final ConcurrencyTestProperties properties) {
		ConcurrentStrictLRUCache<K, V> cc = JavaObjects.cast(cache);
		assertThat(cc.size(), lessThanOrEqualTo(properties.cacheCapacity()));
	}

	@Test
	@Timeout(5)
	void shouldSurviveHighContentionState() throws Exception {
		int capacity = 16;
		ConcurrentStrictLRUCache<Integer, Integer> cache = new ConcurrentStrictLRUCache<>(capacity);
		ConcurrencyTestProperties properties = new ConcurrencyTestProperties.Builder()
				.threadCount(32)
				.iterationsPerThread(200_000)
				.cacheCapacity(capacity)
				.keySpace(64)
				.cacheConsumer(ConcurrentStrictLRUCacheTest::assertInternalState)
				.timeout(Duration.ofSeconds(5))
				.frequencyModulo(4095)
				.logger(LOGGER)
				.build();

		stateStressTest(cache, properties);

		assertThat(cache.size(), lessThanOrEqualTo(capacity));
	}

	static <K, V> void assertInternalState(final Cache<?, ?> cache, final ConcurrencyTestProperties properties) {
		ConcurrentStrictLRUCache<K, V> cc = JavaObjects.cast(cache);
		cc.locked(() -> {
			Node<K, V> head = cc.head();
			Node<K, V> tail = cc.tail();

			// head/tail consistency
			if (head == null || tail == null) {
				if (head != tail) {
					throw new IllegalStateException("Head/tail mismatch: one null, one not");
				}
				if (cc.size() != 0) {
					throw new IllegalStateException("Size not zero but list empty");
				}
				return;
			}
			if (head.prev() != null) {
				throw new IllegalStateException("Head.prev != null");
			}
			if (tail.next() != null) {
				throw new IllegalStateException("Tail.next != null");
			}

			// cycle detection
			Node<K, V> slow = cc.head();
			Node<K, V> fast = cc.head().next();
			while (fast != null && fast.next() != null) {
				slow = slow.next();
				fast = fast.next();
				if (slow == fast) {
					throw new IllegalStateException("Cycle detected in LRU list");
				}
				fast = fast.next();
				if (slow == fast) {
					throw new IllegalStateException("Cycle detected in LRU list");
				}
			}

			// traversal consistency
			int count = 0;
			Node<K, V> prev = null;
			Node<K, V> node = head;
			while (node != null) {
				if (node.prev() != prev) {
					throw new IllegalStateException("Broken prev pointer at key=" + node.key());
				}
				if (!cc.storage().containsKey(node.key())) {
					throw new IllegalStateException("Node in list but not in map: " + node.key());
				}
				prev = node;
				node = node.next();
				++count;
			}

			// tail consistency
			if (prev != tail) {
				throw new IllegalStateException("Tail mismatch after traversal");
			}

			// size consistency
			if (count != cc.size()) {
				throw new IllegalStateException("List size (" + count + ") != cache size (" + cc.size() + ")");
			}
			if (cc.size() > properties.cacheCapacity()) {
				throw new IllegalStateException("Cache size exceeds capacity: " + cc.size() + " > " + properties.cacheCapacity());
			}
		});
	}
}
