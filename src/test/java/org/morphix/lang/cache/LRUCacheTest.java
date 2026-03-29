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
import static org.morphix.utils.Tests.waitUntil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.thread.Threads;
import org.morphix.utils.ConcurrencyTestProperties;
import org.morphix.utils.ConcurrencyTestResults;

/**
 * Test class for {@link LRUCache}.
 *
 * @author Radu Sebastian LAZIN
 */
class LRUCacheTest {

	static final int CACHE_CAPACITY = 3;

	LRUCache<String, String> cache;

	LRUCache<String, String> newCache() {
		return new TestStrictLRUCache<>(CACHE_CAPACITY);
	}

	<T extends LRUCache<String, String>> T cache() {
		return JavaObjects.cast(cache);
	}

	@BeforeEach
	void setUp() {
		this.cache = newCache();
	}

	@Nested
	class SizeTests {

		@Test
		void shouldHaveCorrectCapacity() {
			assertThat(cache.capacity(), is(equalTo(CACHE_CAPACITY)));
		}
	}

	@Nested
	class CapacityTests {

		@Test
		void shouldHaveInitialSizeZero() {
			assertThat(cache.size(), is(equalTo(0)));
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
		@Timeout(5)
		void shouldEvictHeadWhenAddingBeyondCapacity() {
			for (int i = 0; i <= CACHE_CAPACITY; ++i) {
				int index = i + 1;
				cache.computeIfAbsent("key" + index, k -> "value" + index);
			}

			waitUntil(() -> cache.size() == CACHE_CAPACITY, Duration.ofSeconds(3));

			assertThat(cache.size(), is(equalTo(CACHE_CAPACITY)));
			assertThat(cache.get("key1"), is(nullValue()));

			for (int i = 1; i <= CACHE_CAPACITY; ++i) {
				int index = i + 1;
				assertThat(cache.get("key" + index), is(equalTo("value" + index)));
			}
		}

		@Test
		@Timeout(5)
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

			waitUntil(() -> cache.size() == CACHE_CAPACITY, Duration.ofSeconds(3));

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

	@SuppressWarnings("resource")
	static ConcurrencyTestResults stressTest(final LRUCache<String, String> cache, final ConcurrencyTestProperties properties)
			throws InterruptedException {
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(properties.threadCount());

		ConcurrencyTestResults result = new ConcurrencyTestResults();

		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
		try {
			for (int i = 0; i < properties.threadCount(); ++i) {
				final String prefix = "key" + i + "-";
				executor.submit(() -> {
					try {
						start.await();
						for (int j = 0; j < properties.iterationsPerThread(); ++j) {
							String key = prefix + (j % properties.keySpace());
							cache.computeIfAbsent(key, k -> "value" + (Math.random() * 1000));
							cache.get(key);
						}
					} catch (Exception e) {
						result.failedThreads().incrementAndGet();
						properties.logger().log(Level.SEVERE, "Exception in thread", e);
						Threads.handleInterruptedException(e);
					} finally {
						done.countDown();
					}
				});
			}

			start.countDown();
			result.finished().set(done.await(properties.timeout().toMillis(), TimeUnit.MILLISECONDS));
		} finally {
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		}
		return result;
	}

	@SuppressWarnings("resource")
	static ConcurrencyTestResults stateStressTest(final LRUCache<Integer, Integer> cache, final ConcurrencyTestProperties properties)
			throws InterruptedException, ExecutionException {
		ConcurrencyTestResults results = new ConcurrencyTestResults();

		ExecutorService executor = Executors.newFixedThreadPool(properties.threadCount());
		List<Future<?>> futures = new ArrayList<>();
		for (int t = 0; t < properties.threadCount(); ++t) {
			futures.add(executor.submit(() -> {
				ThreadLocalRandom rnd = ThreadLocalRandom.current();
				for (int i = 0; i < properties.iterationsPerThread(); ++i) {
					int key = rnd.nextInt(properties.keySpace());
					if ((i & 3) == 0) {
						// 25% writes
						cache.computeIfAbsent(key, k -> k * 31);
					} else {
						// 75% reads
						cache.get(key);
					}
					if ((i & properties.frequencyModulo()) == 0) {
						properties.cacheConsumer().accept(cache, properties);
					}
				}
			}));
		}
		for (Future<?> future : futures) {
			future.get();
		}
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);

		return results;
	}
}
