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

import java.util.function.Consumer;

import org.morphix.lang.Unchecked;

/**
 * Functional interface to re-throw unchecked exceptions in functional calls. This class specifically accommodates any
 * {@link Consumer}.
 *
 * @param <T> argument type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param t the input argument
	 * @throws Throwable on any error
	 */
	void accept(T t) throws Throwable; // NOSONAR this declaration is only to accommodate all cases

	/**
	 * Removes the need to surround code with try/catch for checked exceptions effectively working like an unchecked
	 * exception.
	 *
	 * @param <T> argument type
	 *
	 * @param c throwing consumer
	 * @return the consumer
	 */
	static <T> Consumer<T> unchecked(final ThrowingConsumer<T> c) {
		return t -> {
			try {
				c.accept(t);
			} catch (Throwable e) {
				Unchecked.reThrow(e);
			}
		};
	}
}
