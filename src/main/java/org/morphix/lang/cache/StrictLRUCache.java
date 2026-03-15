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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.morphix.lang.function.InstanceFunction;

/**
 * A non thread-safe strict LRU (Least Recently Used) cache implementation. This cache maintains a fixed maximum size
 * and evicts the least recently used entry when the cache exceeds this size. The cache is implemented using a
 * combination of a hash map for fast access to entries and a doubly linked list to maintain the order of access. When
 * an entry is accessed or added, it is moved to the tail of the list to indicate that it was recently used. When the
 * cache exceeds its maximum size, the entry at the head of the list (the least recently used entry) is removed from
 * both the list and the hash map.
 * <p>
 * This implementation is not thread-safe and should be used in single-threaded contexts or with external
 * synchronization if accessed from multiple threads. It provides O(1) time complexity for both get and put operations
 * under non-concurrent access, and it guarantees strict LRU eviction order, meaning that the least recently used entry
 * will always be evicted when the cache exceeds its maximum size.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 *
 * @author Radu Sebastian LAZIN
 */
public class StrictLRUCache<K, V> implements LRUCache<K, V> {

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
		 * A reference to the previous node in the doubly linked list. This is used to maintain the order of access in the LRU
		 * cache, allowing for efficient addition and removal of nodes as entries are accessed or evicted.
		 */
		private Node<K, V> prev;

		/**
		 * A reference to the next node in the doubly linked list. This is used to maintain the order of access in the LRU
		 * cache, allowing for efficient addition and removal of nodes as entries are accessed or evicted.
		 */
		private Node<K, V> next;

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

		/**
		 * Returns the key associated with this node. This method is primarily intended for testing purposes to verify the
		 * internal state of the cache and ensure that the eviction order is maintained correctly.
		 *
		 * @return the key associated with this node
		 */
		K key() {
			return key;
		}

		/**
		 * Returns the value associated with this node. This method is primarily intended for testing purposes to verify the
		 * internal state of the cache and ensure that the eviction order is maintained correctly.
		 *
		 * @return the value associated with this node
		 */
		V value() {
			return value;
		}

		/**
		 * Returns the previous node in the doubly linked list. This method is primarily intended for testing purposes to verify
		 * the internal state of the cache and ensure that the eviction order is maintained correctly.
		 *
		 * @return the previous node in the doubly linked list
		 */
		Node<K, V> prev() {
			return prev;
		}

		/**
		 * Returns the next node in the doubly linked list. This method is primarily intended for testing purposes to verify the
		 * internal state of the cache and ensure that the eviction order is maintained correctly.
		 *
		 * @return the next node in the doubly linked list
		 */
		Node<K, V> next() {
			return next;
		}

		/**
		 * Sets the next node in the doubly linked list. This method is primarily intended for testing purposes to verify the
		 * internal state of the cache and ensure that the eviction order is maintained correctly.
		 *
		 * @param next the next node in the doubly linked list
		 */
		void setNext(final Node<K, V> next) {
			this.next = next;
		}

		/**
		 * Sets the previous node in the doubly linked list. This method is primarily intended for testing purposes to verify
		 * the internal state of the cache and ensure that the eviction order is maintained correctly.
		 *
		 * @param prev the previous node in the doubly linked list
		 */
		void setPrev(final Node<K, V> prev) {
			this.prev = prev;
		}
	}

	/**
	 * The maximum size of the cache. This determines how many key-value pairs can be stored in the cache before evicting
	 * the least recently used entry. When the cache exceeds this size, the entry that has been accessed least recently will
	 * be removed to make room for new entries.
	 */
	private final int capacity;

	/**
	 * The head of the doubly linked list used to maintain the order of access in the LRU cache. The head represents the
	 * least recently used entry in the cache. When an entry is accessed, it is moved to the tail of the list, and when an
	 * entry is evicted, the head is removed.
	 */
	private Node<K, V> head;

	/**
	 * The tail of the doubly linked list used to maintain the order of access in the LRU cache. The tail represents the
	 * most recently used entry in the cache. When an entry is accessed, it is moved to the tail of the list, and when a new
	 * entry is added, it is also added to the tail.
	 */
	private Node<K, V> tail;

	/**
	 * The hash map used to store the key-value pairs in the cache.
	 */
	private final Map<K, Node<K, V>> cache;

	/**
	 * Constructor for the strict LRU cache. Initializes the cache with the specified maximum size and a function to create
	 * the underlying map instance. This constructor allows for flexibility in choosing the type of map used to store the
	 * cache entries, which can be useful for testing or for specific performance characteristics.
	 *
	 * @param capacity the maximum size of the cache
	 * @param mapInstanceFunction a function that creates an instance of the map used to store the cache entries
	 */
	public StrictLRUCache(final int capacity, final InstanceFunction<Map<K, Node<K, V>>> mapInstanceFunction) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Cache capacity must be greater than 0");
		}
		this.capacity = capacity;
		this.cache = mapInstanceFunction.instance();
	}

	/**
	 * Constructor for the concurrent LRU cache. Initializes the cache with the specified maximum size and uses a default
	 * hash map to store the cache nodes. This constructor is a convenience method for creating a strict LRU cache with a
	 * standard hash map implementation, which is suitable for most use cases where thread safety is not a concern.
	 *
	 * @param capacity the maximum size of the cache
	 */
	public StrictLRUCache(final int capacity) {
		this(capacity, HashMap::new);
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
		addToTail(node);
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
		addToTail(node);
		return node.value;
	}

	/**
	 * Adds the specified node to the tail of the doubly linked list. If the list is empty, the node becomes both the head
	 * and tail. Otherwise, the node is added after the current tail, and the tail reference is updated to point to the new
	 * node. If the cache size exceeds the maximum size after adding the new node, the head node (the least recently used
	 * entry) is removed from the list and the cache to maintain the size constraint.
	 *
	 * @param node the node to be added to the tail of the list
	 */
	void addToTail(final Node<K, V> node) {
		if (null == node || tail == node) {
			return;
		}
		// unlink the node from its current position first
		if (head == node) {
			head = node.next;
			if (null != head) {
				head.prev = null;
			}
		} else {
			if (null != node.prev) {
				node.prev.next = node.next;
			}
			if (null != node.next) {
				node.next.prev = node.prev;
			}
		}
		if (null == tail) {
			head = node;
			tail = node;
			node.prev = null;
			node.next = null;
		} else {
			tail.next = node;
			node.prev = tail;
			node.next = null;
			tail = node;
		}
		if (cache.size() > capacity) {
			removeHead();
		}
	}

	/**
	 * Removes the head node from the doubly linked list. If the list is empty, this method does nothing. Otherwise, the
	 * head reference is updated to point to the next node in the list, and the previous reference of the new head is set to
	 * null. If the list becomes empty after removal, the tail reference is also set to null. The removed node's key is also
	 * removed from the cache.
	 */
	void removeHead() {
		if (null == head) {
			return;
		}
		Node<K, V> oldHead = head;
		head = oldHead.next;

		if (null == head) {
			tail = null;
		} else {
			head.prev = null;
		}
		cache.remove(oldHead.key, oldHead);
	}

	/**
	 * Returns the head node of the doubly linked list. This method is primarily intended for testing purposes to verify the
	 * internal state of the cache and ensure that the eviction order is maintained correctly.
	 *
	 * @return the head node of the doubly linked list
	 */
	Node<K, V> head() {
		return head;
	}

	/**
	 * Returns the tail node of the doubly linked list. This method is primarily intended for testing purposes to verify the
	 * internal state of the cache and ensure that the eviction order is maintained correctly.
	 *
	 * @return the tail node of the doubly linked list
	 */
	Node<K, V> tail() {
		return tail;
	}

	/**
	 * Sets the head node of the doubly linked list. This method is primarily intended for testing purposes to verify the
	 * internal state of the cache and ensure that the eviction order is maintained correctly.
	 *
	 * @param head the new head node of the doubly linked list
	 */
	void setHead(final Node<K, V> head) {
		this.head = head;
	}

	/**
	 * Sets the tail node of the doubly linked list. This method is primarily intended for testing purposes to verify the
	 * internal state of the cache and ensure that the eviction order is maintained correctly.
	 *
	 * @param tail the new tail node of the doubly linked list
	 */
	void setTail(final Node<K, V> tail) {
		this.tail = tail;
	}

	/**
	 * Returns the internal cache map that stores the key-node pairs. This method is primarily intended for testing purposes
	 * to verify the internal state of the cache and ensure that the eviction order is maintained correctly.
	 *
	 * @return the internal cache map that stores the key-node pairs
	 */
	Map<K, Node<K, V>> storage() {
		return cache;
	}

	/**
	 * Clears the cache by removing all key-value pairs and resetting the head and tail of the doubly linked list.
	 */
	public void clear() {
		cache.clear();
		head = null;
		tail = null;
	}
}
