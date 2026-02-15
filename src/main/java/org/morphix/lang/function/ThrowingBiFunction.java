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

import java.util.function.BiFunction;

import org.morphix.lang.Unchecked;

/**
 * Functional interface to re-throw unchecked exceptions in functional calls. This class specifically accommodates any
 * {@link BiFunction}.
 *
 * @param <T> first argument type
 * @param <U> second argument type
 * @param <R> return type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ThrowingBiFunction<T, U, R> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function first argument
	 * @param u the function second argument
	 * @return the function result
	 * @throws Throwable on any error
	 */
	R apply(T t, U u) throws Throwable; // NOSONAR this declaration is only to accommodate all cases

	/**
	 * Removes the need to surround code with try/catch for checked exceptions effectively working like an unchecked
	 * exception.
	 *
	 * @param <T> first argument type
	 * @param <U> second argument type
	 * @param <R> return type
	 *
	 * @param f throwing function
	 * @return the function
	 */
	static <T, U, R> BiFunction<T, U, R> unchecked(final ThrowingBiFunction<T, U, R> f) {
		return (t, u) -> {
			try {
				return f.apply(t, u);
			} catch (Throwable e) {
				return Unchecked.reThrow(e);
			}
		};
	}
}
