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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.morphix.utils.ConcurrencyTestProperties;
import org.morphix.utils.ConcurrencyTestResults;

/**
 * Test class for {@link TestStrictLRUCache}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConcurrentStrictLRUCacheTest extends StrictLRUCacheTest {

	private static final Logger LOGGER = Logger.getLogger(ConcurrentStrictLRUCacheTest.class.getName());

	private static final int THREADS = 32;
	private static final int ITERATIONS = 200_000;
	private static final int KEY_SPACE = 64;
	private static final int CAPACITY = 16;

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
	@SuppressWarnings("resource")
	void shouldSurviveHighContention() throws Exception {
		ConcurrentStrictLRUCache<Integer, Integer> cache = new ConcurrentStrictLRUCache<>(CAPACITY);

		ExecutorService executor = Executors.newFixedThreadPool(THREADS);
		List<Future<?>> futures = new ArrayList<>();

		for (int t = 0; t < THREADS; ++t) {
			futures.add(executor.submit(() -> {
				ThreadLocalRandom rnd = ThreadLocalRandom.current();
				for (int i = 0; i < ITERATIONS; ++i) {
					int key = rnd.nextInt(KEY_SPACE);
					if ((i & 3) == 0) {
						// 25% writes
						cache.computeIfAbsent(key, k -> k * 31);
					} else {
						// 75% reads
						cache.get(key);
					}
					if ((i & 1023) == 0) {
						assertThat(cache.size(), lessThanOrEqualTo(CAPACITY));
					}
				}
			}));
		}
		for (Future<?> f : futures) {
			f.get();
		}
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);

		assertThat(cache.size(), lessThanOrEqualTo(CAPACITY));
	}
}
