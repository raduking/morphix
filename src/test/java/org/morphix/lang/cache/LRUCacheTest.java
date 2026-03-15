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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.junit.jupiter.api.Test;
import org.morphix.lang.thread.Threads;
import org.morphix.utils.ConcurrencyTestProperties;
import org.morphix.utils.ConcurrencyTestResults;

/**
 * Test class for {@link LRUCache}.
 *
 * @author Radu Sebastian LAZIN
 */
class LRUCacheTest {

	static final int THREAD_COUNT = 100;
	static final int ITERATIONS_PER_THREAD = 1000;
	static final int KEY_SPACE = 7;
	static final Duration TIMEOUT = Duration.ofSeconds(1);

	@Test
	void shouldCreateConcurrencyTestPropertiesWithDefaults() {
		ConcurrencyTestProperties properties = new ConcurrencyTestProperties.Builder().build();

		assertNotNull(properties);
		assertNotNull(properties.logger());
	}

	@SuppressWarnings("resource")
	static ConcurrencyTestResults hit(final LRUCache<String, String> cache, final ConcurrencyTestProperties properties)
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
}
