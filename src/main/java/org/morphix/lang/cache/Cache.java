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

import java.util.function.Function;

/**
 * An interface representing a cache. A cache is a data structure that stores a limited number of key-value pairs and
 * provides fast access to them. This interface defines the basic operations for retrieving and adding entries to the
 * cache, as well as methods for monitoring the size and capacity of the cache.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 *
 * @author Radu Sebastian LAZIN
 */
public interface Cache<K, V> {

	/**
	 * Retrieves the value associated with the specified key from the cache. If the key is not present in the cache, this
	 * method returns {@code null}.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the value associated with the specified key, or {@code null} if the key is not present in the cache
	 */
	V get(K key);

	/**
	 * Computes a value for the specified key using the given mapping function and enters it into the cache if it is not
	 * already present. If the cache exceeds its maximum size after adding the new entry, it evicts the least recently used
	 * entry to make room for the new one.
	 *
	 * @param key the key for which a value is to be computed and entered into the cache
	 * @param valueFunction a function that computes a value for the specified key
	 * @return the current (existing or computed) value associated with the specified key
	 */
	V computeIfAbsent(K key, Function<? super K, ? extends V> valueFunction);

	/**
	 * Returns the current number of entries in the cache. This method can be used to monitor the size of the cache and to
	 * determine when it is approaching its maximum capacity.
	 *
	 * @return the current number of entries in the cache
	 */
	int size();

	/**
	 * Returns the maximum number of entries that the cache can hold. This method can be used to determine the capacity of
	 * the cache and to monitor when it is approaching its maximum size.
	 *
	 * @return the maximum number of entries that the cache can hold
	 */
	int capacity();
}
