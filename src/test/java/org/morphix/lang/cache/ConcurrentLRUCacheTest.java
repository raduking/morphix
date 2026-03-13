package org.morphix.lang.cache;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ConcurrentLRUCache}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConcurrentLRUCacheTest {

	@Test
	void shouldFillTheCacheAndNotEvictIfSizeLessOrEqual() {
		ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(2);
		cache.computeIfAbsent("key1", k -> "value1");
		cache.computeIfAbsent("key2", k -> "value2");

		assertThat(cache.get("key1"), equalTo("value1"));
		assertThat(cache.get("key2"), equalTo("value2"));
	}

	@Test
	void shouldEvictLeastRecentlyUsed() {
		ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(2);
		cache.computeIfAbsent("key1", k -> "value1");
		cache.computeIfAbsent("key2", k -> "value2");
		cache.get("key1");
		cache.computeIfAbsent("key3", k -> "value3");

		assertNull(cache.get("key2"));
		assertThat(cache.get("key1"), equalTo("value1"));
		assertThat(cache.get("key3"), equalTo("value3"));
	}

	@Test
	void shouldClearAllEntries() {
		ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(4);
		IntStream.range(0, 6).forEach(i -> cache.computeIfAbsent("key" + i, k -> "value" + i));

		assertThat(cache.size(), equalTo(4));

		cache.clear();

		assertThat(cache.size(), is(0));
		assertThat(cache.get("key0"), nullValue());
	}
}
