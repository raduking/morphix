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
package org.morphix.lang;

import java.util.Map;

/**
 * Generic pair record.
 *
 * @param <L> left value type
 * @param <R> right value type
 * @param left left value
 * @param right right value
 *
 * @author Radu Sebastian LAZIN
 */
public record Pair<L, R>(L left, R right) {

	/**
	 * Factory method to create a new pair with the given left and right values.
	 *
	 * @param <L> left value type
	 * @param <R> right value type
	 *
	 * @param left left value
	 * @param right right value
	 * @return a new pair with the given left and right values
	 */
	public static <L, R> Pair<L, R> of(final L left, final R right) {
		return new Pair<>(left, right);
	}

	/**
	 * Transforms this pair into a {@link Map}.
	 *
	 * @return a map with one (key, value) with (left, right)
	 */
	public Map<L, R> toMap() {
		return Map.of(left, right);
	}

	/**
	 * Transforms this pair into a {@link Map.Entry}.
	 *
	 * @return a map entry with key as left and value as right
	 */
	public Map.Entry<L, R> toEntry() {
		return Map.entry(left, right);
	}
}
