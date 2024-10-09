/*
 * Copyright 2025 the original author or authors.
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

import java.util.function.Supplier;

/**
 * Functional interface for a function that returns a value.
 *
 * @param <T> return type for the value function
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ValueFunction<T> extends Supplier<T> {

	/**
	 * Applies this function and returns a value to be used.
	 *
	 * @return a value
	 * @throws Exception any exception thrown from the function
	 */
	T value() throws Exception; // NOSONAR

	/**
	 * @see Supplier#get()
	 */
	@Override
	default T get() {
		try {
			return value();
		} catch (Exception e) {
			throw new IllegalStateException("Error returning value", e);
		}
	}

	/**
	 * Returns a {@link ValueFunction} from the given value.
	 *
	 * @param <T> value type
	 *
	 * @param value value to be returned
	 * @return a field value function
	 */
	static <T> ValueFunction<T> from(final T value) {
		return () -> value;
	}

}
