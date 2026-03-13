package org.morphix.lang.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * A thread-safe concurrent strict LRU (Least Recently Used) cache implementation.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 *
 * @author Radu Sebastian LAZIN
 */
public class ConcurrentLRUCache<K, V> {

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
	}

	/**
	 * The maximum size of the cache. This determines how many key-value pairs can be stored in the cache before evicting
	 * the least recently used entry. When the cache exceeds this size, the entry that has been accessed least recently will
	 * be removed to make room for new entries.
	 */
	private final int maxSize;

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
	 * The concurrent hash map used to store the key-value pairs in the cache.
	 */
	private final ConcurrentHashMap<K, Node<K, V>> cache = new ConcurrentHashMap<>();

	/**
	 * A lock used to synchronize write operations on the cache.
	 */
	private final ReentrantLock writeLock = new ReentrantLock();

	/**
	 * Constructor for the concurrent LRU cache. Initializes the cache with the specified maximum size.
	 *
	 * @param maxSize the maximum size of the cache
	 */
	public ConcurrentLRUCache(final int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * Returns the number of key-value pairs currently stored in the cache.
	 *
	 * @return the number of key-value pairs currently stored in the cache
	 */
	public int size() {
		return cache.size();
	}

	/**
	 * Returns the maximum size of the cache.
	 *
	 * @return the maximum size of the cache
	 */
	public int maxSize() {
		return maxSize;
	}

	/**
	 * Retrieves the value associated with the specified key from the cache. If the key is found, the corresponding node is
	 * moved to the tail of the doubly linked list to indicate that it was recently accessed. If the key is not found, null
	 * is returned.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the value associated with the specified key, or null if the key is not found in the cache
	 */
	public V get(final K key) {
		Node<K, V> node = cache.get(key);
		if (null == node) {
			return null;
		}

		writeLock.lock();
		try {
			moveToTail(node);
		} finally {
			writeLock.unlock();
		}
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
	 * @param mappingFunction the function to compute a value if the key is not already present in the cache
	 * @return the value associated with the specified key, null otherwise
	 */
	public V computeIfAbsent(final K key, final Function<K, V> mappingFunction) {
		Node<K, V> node = cache.computeIfAbsent(key, k -> {
			V v = mappingFunction.apply(k);
			if (null == v) {
				return null;
			}
			return new Node<>(k, v);
		});
		if (null == node) {
			return null;
		}

		writeLock.lock();
		try {
			if (null == node.prev && null == node.next && node != head) {
				addToTail(node);
			}
			moveToTail(node);
			while (cache.size() > maxSize) {
				removeHead();
			}
		} finally {
			writeLock.unlock();
		}
		return node.value;
	}

	/**
	 * Adds the specified node to the tail of the doubly linked list. If the list is empty, the node becomes both the head
	 * and tail. Otherwise, the node is added after the current tail, and the tail reference is updated to point to the new
	 * node.
	 *
	 * @param node the node to be added to the tail of the list
	 */
	protected void addToTail(final Node<K, V> node) {
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
	}

	/**
	 * Removes the head node from the doubly linked list. If the list is empty, this method does nothing. Otherwise, the
	 * head reference is updated to point to the next node in the list, and the previous reference of the new head is set to
	 * null. If the list becomes empty after removal, the tail reference is also set to null. The removed node's key is also
	 * removed from the cache.
	 */
	protected void removeHead() {
		if (null == head) {
			return;
		}
		Node<K, V> oldHead = head;
		head = oldHead.next;

		if (null != head) {
			head.prev = null;
		} else {
			tail = null;
		}
		cache.remove(oldHead.key);
	}

	/**
	 * Moves the specified node to the tail of the doubly linked list. If the node is already at the tail, this method does
	 * nothing. If the node is at the head, the head reference is updated to point to the next node, and the previous
	 * reference of the new head is set to null. If the node is in the middle of the list, its previous and next nodes are
	 * linked together to bypass it. Finally, the node is added to the tail of the list.
	 *
	 * @param node the node to be moved to the tail of the list
	 */
	protected void moveToTail(final Node<K, V> node) {
		if (node == tail) {
			return;
		}
		if (node == head) {
			head = node.next;
			if (null != head) {
				head.prev = null;
			}
		} else {
			node.prev.next = node.next;
			node.next.prev = node.prev;
		}
		addToTail(node);
	}

	/**
	 * Clears the cache by removing all key-value pairs and resetting the head and tail of the doubly linked list.
	 */
	public void clear() {
		writeLock.lock();
		try {
			cache.clear();
			head = null;
			tail = null;
		} finally {
			writeLock.unlock();
		}
	}
}
