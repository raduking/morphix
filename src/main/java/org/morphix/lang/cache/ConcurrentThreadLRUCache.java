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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * A thread-safe concurrent fast LRU (Least Recently Used) cache implementation. Since concurrency is the main focus of
 * this implementation, it does guarantee strict LRU eviction order, but it trades it for lower performance in the
 * eviction process when the cache reached its maximum size which is O(n) in the worst case. This trade-off allows for
 * O(1) concurrent access to the cache without blocking read/write operations.
 * <p>
 * Under non-concurrent access, this cache behaves like a standard LRU cache, evicting the least recently used entry
 * when the cache exceeds its maximum size.
 * <ul>
 * <li>read - O(1)</li>
 * <li>write - O(1)</li>
 * <li>eviction - O(n) in the worst case</li>
 * </ul>
 * Eviction is performed asynchronously in a separate thread to avoid blocking write operations. When the cache size
 * exceeds the maximum size, a new thread is started to perform eviction of the least recently used entries until the
 * cache size is less than or equal to the maximum size. This allows for non-blocking write operations while still
 * ensuring that the cache does not exceed its maximum size, albeit with a potential delay in eviction under high
 * concurrency.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 *
 * @author Radu Sebastian LAZIN
 */
public class ConcurrentThreadLRUCache<K, V> implements LRUCache<K, V> {

	/**
	 * A node in the doubly linked list used to maintain the order of access in the LRU cache. Each node contains a key, a
	 * value, and references to the previous and next nodes in the list.
	 *
	 * @param <K> the type of keys maintained by this cache
	 * @param <V> the type of mapped values
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class Node<K, V> {

		/**
		 * The key associated with this node. This is used to identify the entry in the cache and to remove it when it is
		 * evicted.
		 */
		private final K key;

		/**
		 * The value associated with this node. This is the actual data stored in the cache that can be retrieved using the key.
		 */
		private final V value;

		/**
		 * Last access time of this node in nanoseconds. This is used to determine the least recently used entry in the cache
		 * for eviction purposes. When an entry is accessed, this timestamp is updated to the current time, allowing the cache
		 * to identify which entry has been accessed least recently when the cache exceeds its maximum size.
		 */
		private volatile long lastAccessTime = System.nanoTime();

		/**
		 * Constructor for the Node class. Initializes a new node with the specified key and value.
		 *
		 * @param key the key associated with this node
		 * @param value the value associated with this node
		 */
		Node(final K key, final V value) {
			this.key = key;
			this.value = value;
		}
	}

	/**
	 * The maximum size of the cache. This determines how many key-value pairs can be stored in the cache before evicting
	 * the least recently used entry. When the cache exceeds this size, the entry that has been accessed least recently will
	 * be removed to make room for new entries.
	 */
	private final int capacity;

	/**
	 * The concurrent hash map used to store the key-value pairs in the cache.
	 */
	private final Map<K, Node<K, V>> cache = new ConcurrentHashMap<>();

	/**
	 * A lock used to synchronize write operations on the cache.
	 */
	private final ReentrantLock writeLock = new ReentrantLock();

	/**
	 * The number of entries to sample when evicting the least recently used entry. This is used to limit the number of
	 * entries that need to be checked for eviction when the cache exceeds its maximum size, improving eviction performance
	 * under high concurrency at the cost of potentially evicting a more recently used entry.
	 * <p>
	 * If sample size is set to 0, all entries will be checked for eviction, ensuring strict LRU eviction order but with
	 * potentially higher eviction time under high concurrency.
	 */
	private final int sampleSize;

	/**
	 * Constructor for the concurrent LRU cache. Initializes the cache with the specified maximum size.
	 *
	 * @param capacity the maximum size of the cache
	 * @param sampleSize the number of entries to sample when evicting the least recently used entry, if set to 0 then all
	 *     entries are checked for eviction
	 */
	public ConcurrentThreadLRUCache(final int capacity, final int sampleSize) {
		this.capacity = capacity;
		this.sampleSize = sampleSize;
	}

	/**
	 * Constructor for the concurrent LRU cache. Initializes the cache with the specified maximum size.
	 * <p>
	 * WARNING: This constructor uses a default sample size of 0, which means that all entries will be checked for eviction
	 * when the cache exceeds its maximum size. This ensures strict LRU eviction order but may lead to higher eviction time
	 * under high concurrency. Consider using the constructor with a specified sample size for better eviction performance
	 * under high concurrency.
	 *
	 * @param capacity the maximum size of the cache
	 */
	public ConcurrentThreadLRUCache(final int capacity) {
		this(capacity, 0);
	}

	/**
	 * Returns the number of key-value pairs currently stored in the cache.
	 *
	 * @return the number of key-value pairs currently stored in the cache
	 */
	@Override
	public int size() {
		return cache.size();
	}

	/**
	 * Returns the maximum size of the cache.
	 *
	 * @return the maximum size of the cache
	 */
	@Override
	public int capacity() {
		return capacity;
	}

	/**
	 * Retrieves the value associated with the specified key from the cache. If the key is found, the corresponding node is
	 * moved to the tail of the doubly linked list to indicate that it was recently accessed. If the key is not found, null
	 * is returned.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the value associated with the specified key, or null if the key is not found in the cache
	 */
	@Override
	public V get(final K key) {
		Node<K, V> node = cache.get(key);
		if (null == node) {
			return null;
		}
		node.lastAccessTime = System.nanoTime();
		return node.value;
	}

	/**
	 * Retrieves the value associated with the specified key from the cache. If the key is found, the corresponding node is
	 * moved to the tail of the doubly linked list to indicate that it was recently accessed. If the key is not found, the
	 * mapping function is applied to the key to compute a new value, which is then added to the cache and returned.
	 * <p>
	 * If the mapping function returns null, no entry is added to the cache and null is returned.
	 *
	 * @param key the key whose associated value is to be returned or computed
	 * @param valueFunction the function to compute a value if the key is not already present in the cache
	 * @return the value associated with the specified key, null otherwise
	 */
	@Override
	public V computeIfAbsent(final K key, final Function<? super K, ? extends V> valueFunction) {
		Node<K, V> node = cache.computeIfAbsent(key, k -> {
			V v = valueFunction.apply(k);
			if (null == v) {
				return null;
			}
			return new Node<>(k, v);
		});
		if (null == node) {
			return null;
		}

		if (cache.size() > capacity) {
			if (!writeLock.tryLock()) {
				return node.value;
			}
			try {
				Thread.ofVirtual().start(this::evict);
			} finally {
				writeLock.unlock();
			}
		}
		return node.value;
	}

	/**
	 * Evicts entries from the cache until the cache size is less than or equal to the maximum size.
	 */
	void evict() {
		if (!writeLock.tryLock()) {
			return;
		}
		try {
			while (cache.size() > capacity) {
				evictLastRecentlyUsed();
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Evicts the least recently used entry from the cache. This method iterates through the entries in the cache to find
	 * the node with the oldest last access time and removes it from the cache.
	 */
	void evictLastRecentlyUsed() {
		Node<K, V> leastRecentlyUsed = null;
		int count = 0;
		for (Node<K, V> node : cache.values()) {
			if (null == leastRecentlyUsed || node.lastAccessTime < leastRecentlyUsed.lastAccessTime) {
				leastRecentlyUsed = node;
			}
			if (sampleSize > 0 && ++count >= sampleSize) {
				break;
			}
		}
		if (null != leastRecentlyUsed) {
			// remove only if the same node is still present in the cache to avoid removing a recently accessed node
			cache.remove(leastRecentlyUsed.key, leastRecentlyUsed);
		}
	}

	/**
	 * Returns the internal storage of the cache, which is a concurrent hash map that maps keys to nodes containing the
	 * key-value pairs and their last access times. This method is intended for testing purposes to allow inspection of the
	 * internal state of the cache.
	 *
	 * @return the internal storage of the cache as a map of keys to nodes
	 */
	Map<K, Node<K, V>> storage() {
		return cache;
	}

	/**
	 * Clears the cache by removing all key-value pairs and resetting the head and tail of the doubly linked list.
	 */
	@Override
	public void clear() {
		writeLock.lock();
		try {
			cache.clear();
		} finally {
			writeLock.unlock();
		}
	}
}
