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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe concurrent strict LRU (Least Recently Used) cache implementation. This implementation guarantees strict
 * LRU eviction order, meaning that the least recently used entry will always be evicted when the cache exceeds its
 * maximum size.
 * <p>
 * This implementation is what most closely resembles a standard LRU cache, with the addition of thread-safety features
 * to allow for concurrent access and this is exactly why it performs the worst in multi-threaded scenarios due to the
 * need for locking the entire access order list for both read and write operations. In a single-threaded scenario, this
 * cache behaves like a standard LRU cache, evicting the least recently used entry when the cache exceeds its maximum
 * size.
 * <p>
 * This implementation uses a single read-write lock to synchronize access to the doubly linked list that maintains the
 * order of access in the LRU cache.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 *
 * @author Radu Sebastian LAZIN
 */
public class ConcurrentStrictLRUCache<K, V> extends StrictLRUCache<K, V> {

	/**
	 * A lock used to synchronize write operations on the cache.
	 */
	private final ReentrantLock modifyLock = new ReentrantLock();

	/**
	 * Constructor for the concurrent LRU cache. Initializes the cache with the specified maximum size.
	 *
	 * @param capacity the maximum size of the cache
	 */
	public ConcurrentStrictLRUCache(final int capacity) {
		super(capacity, ConcurrentHashMap::new);
	}

	/**
	 * Concurrently adds or moves a node to the tail of the doubly linked list that maintains the order of access in the LRU
	 * cache. This method acquires the modify lock to ensure that only one thread can modify the access order list at a
	 * time, preventing race conditions and ensuring thread safety when adding new entries to the cache.
	 *
	 * @see TestStrictLRUCache#addToTail(Node)
	 */
	@Override
	void toTail(final Node<K, V> node) {
		modifyLock.lock();
		try {
			super.toTail(node);
		} finally {
			modifyLock.unlock();
		}
	}

	/**
	 * Clears the cache by removing all key-value pairs and resetting the head and tail of the doubly linked list.
	 */
	@Override
	public void clear() {
		modifyLock.lock();
		try {
			super.clear();
		} finally {
			modifyLock.unlock();
		}
	}
}
