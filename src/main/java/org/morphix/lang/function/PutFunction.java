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
package org.morphix.lang.function;

import java.util.Map;

/**
 * Put function for maps.
 *
 * @param <K> key type
 * @param <V> value type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface PutFunction<K, V> {

	/**
	 * Put a value into the map.
	 *
	 * @param map map
	 * @param key key
	 * @param value value
	 * @return previous value associated with key, or null if there was no mapping for key
	 */
	V put(Map<K, V> map, K key, V value);

	/**
	 * Creates a put function for maps.
	 *
	 * @param <K> key type
	 * @param <V> value type
	 *
	 * @return put function
	 */
	static <K, V> PutFunction<K, V> of() {
		return Map::put;
	}

	/**
	 * Composes this put function with another put function.
	 *
	 * @param after put function to be executed after this put function
	 * @return composed put function
	 */
	default PutFunction<K, V> andThen(final PutFunction<K, V> after) {
		return compose(this, after);
	}

	/**
	 * Composes two put functions for maps.
	 *
	 * @param <K> key type
	 * @param <V> value type
	 *
	 * @param first first put function
	 * @param second second put function
	 * @return composed put function
	 */
	static <K, V> PutFunction<K, V> compose(final PutFunction<K, V> first, final PutFunction<K, V> second) {
		return (map, key, value) -> {
			first.put(map, key, value);
			return second.put(map, key, value);
		};
	}

	/**
	 * Creates a put function for maps that only puts non-null values.
	 *
	 * @param <K> key type
	 * @param <V> value type
	 *
	 * @return put function
	 */
	static <K, V> PutFunction<K, V> ifNotNullValue() {
		return (map, key, value) -> null != value ? map.put(key, value) : null;
	}

	/**
	 * Creates a put function for maps that only puts non-null keys.
	 *
	 * @param <K> key type
	 * @param <V> value type
	 *
	 * @return put function
	 */
	static <K, V> PutFunction<K, V> ifNotNullKey() {
		return (map, key, value) -> null != key ? map.put(key, value) : null;
	}

	/**
	 * Creates a put function for maps that only puts non-null keys and non-null values.
	 *
	 * @param <K> key type
	 * @param <V> value type
	 *
	 * @return put function
	 */
	static <K, V> PutFunction<K, V> ifNotNullKeyAndValue() {
		return (map, key, value) -> (key != null && value != null) ? map.put(key, value) : null;
	}
}
