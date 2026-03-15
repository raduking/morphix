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

/**
 * An interface representing a Least Recently Used (LRU) cache. An LRU cache is a data structure that stores a limited
 * number of key-value pairs and evicts the least recently used entry when the cache exceeds its maximum size. This
 * interface defines the basic operations for retrieving and adding entries to the cache.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 *
 * @author Radu Sebastian LAZIN
 */
public interface LRUCache<K, V> extends Cache<K, V> {

	// empty
}
