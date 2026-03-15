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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.morphix.lang.cache.LRUCacheTest.ConcurrencyTestProperties;

/**
 * Test class for {@link StrictLRUCache}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConcurrentSingleLockLRUCacheTest extends StrictLRUCacheTest {

	private static final Logger LOGGER = Logger.getLogger(ConcurrentSingleLockLRUCacheTest.class.getName());

	@Override
	StrictLRUCache<String, String> newCache() {
		return new ConcurrentSingleLockLRUCache<>(CACHE_CAPACITY);
	}

	@Test
	void shouldHaveTheCorrectCacheInstance() {
		assertThat(cache, is(instanceOf(ConcurrentSingleLockLRUCache.class)));
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

		LRUCacheTest.ConcurrencyResult result = LRUCacheTest.hit(cache, properties);

		assertTrue(result.finished().get(), "All threads should complete in reasonable time");
		assertThat(result.failedThreads().get(), is(0L));

		assertThat(cache.size(), is(equalTo(CACHE_CAPACITY)));
	}
}
