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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;

/**
 * Utility methods for consumer objects.
 *
 * @author Radu Sebastian LAZIN
 */
public class Consumers {

	/**
	 * An empty consumer.
	 */
	private static final Consumer<?> EMPTY_CONSUMER = t -> {
		// empty
	};

	/**
	 * An empty bi-consumer.
	 */
	private static final BiConsumer<?, ?> EMPTY_BI_CONSUMER = (t, u) -> {
		// empty
	};

	/**
	 * Private constructor.
	 */
	private Consumers() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a consumer that does nothing.
	 *
	 * @param <T> the type of the input to the operation
	 *
	 * @return a consumer that does nothing
	 */
	public static <T> Consumer<T> noConsumer() {
		return JavaObjects.cast(EMPTY_CONSUMER);
	}

	/**
	 * Alias for {@link #noConsumer()}.
	 *
	 * @param <T> the type of the input to the operation
	 *
	 * @return a consumer that does nothing
	 */
	public static <T> Consumer<T> consumeNothing() {
		return noConsumer();
	}

	/**
	 * Returns a consumer that does nothing.
	 *
	 * @param <T> the type of the first argument to the operation
	 * @param <U> the type of the second argument to the operation
	 *
	 * @return a consumer that does nothing
	 */
	public static <T, U> BiConsumer<T, U> noBiConsumer() {
		return JavaObjects.cast(EMPTY_BI_CONSUMER);
	}

}
