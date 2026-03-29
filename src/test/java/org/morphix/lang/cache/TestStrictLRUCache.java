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

public class TestStrictLRUCache<K, V> implements LRUCache<K, V> {

	static class Node<K, V> {

		private final K key;
		private final V value;
		private Node<K, V> prev;
		private Node<K, V> next;

		Node(final K key, final V value) {
			this.key = key;
			this.value = value;
		}

		K key() {
			return key;
		}

		V value() {
			return value;
		}

		Node<K, V> prev() {
			return prev;
		}

		Node<K, V> next() {
			return next;
		}

		void setNext(final Node<K, V> next) {
			this.next = next;
		}

		void setPrev(final Node<K, V> prev) {
			this.prev = prev;
		}
	}

	private final int capacity;
	private Node<K, V> head;
	private Node<K, V> tail;
	private final Map<K, Node<K, V>> cache;

	public TestStrictLRUCache(final int capacity, final InstanceFunction<Map<K, Node<K, V>>> mapInstanceFunction) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Cache capacity must be greater than 0");
		}
		this.capacity = capacity;
		this.cache = mapInstanceFunction.instance();
	}

	public TestStrictLRUCache(final int capacity) {
		this(capacity, HashMap::new);
	}

	@Override
	public int size() {
		return cache.size();
	}

	@Override
	public int capacity() {
		return capacity;
	}

	@Override
	public V get(final K key) {
		Node<K, V> node = cache.get(key);
		if (null == node) {
			return null;
		}
		addToTail(node);
		return node.value;
	}

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

	Node<K, V> head() {
		return head;
	}

	Node<K, V> tail() {
		return tail;
	}

	void setHead(final Node<K, V> head) {
		this.head = head;
	}

	void setTail(final Node<K, V> tail) {
		this.tail = tail;
	}

	Map<K, Node<K, V>> storage() {
		return cache;
	}

	@Override
	public void clear() {
		cache.clear();
		head = null;
		tail = null;
	}
}
