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

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;

/**
 * Predicates utility methods.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Predicates {

	/**
	 * A predicate that is always true.
	 */
	private static final Predicate<Object> ALWAYS_TRUE = t -> true;

	/**
	 * A predicate that is always false.
	 */
	private static final Predicate<Object> ALWAYS_FALSE = t -> false;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Predicates() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a negative predicate.
	 *
	 * @param <T> the type of the input to the predicate
	 *
	 * @param predicate predicate to negate
	 * @return a negative predicate
	 */
	public static <T> Predicate<T> not(final Predicate<T> predicate) {
		return predicate.negate();
	}

	/**
	 * Returns a predicate that is always true.
	 *
	 * @param <T> the type of the input to the predicate
	 *
	 * @return a predicate that is always true
	 */
	public static <T> Predicate<T> alwaysTrue() {
		return acceptAll();
	}

	/**
	 * Returns a predicate that accepts all inputs.
	 *
	 * @param <T> the type of the input to the predicate
	 *
	 * @return a predicate that accepts all inputs
	 */
	public static <T> Predicate<T> acceptAll() {
		return JavaObjects.cast(ALWAYS_TRUE);
	}

	/**
	 * Returns a predicate that is always false.
	 *
	 * @param <T> the type of the input to the predicate
	 *
	 * @return a predicate that is always false
	 */
	public static <T> Predicate<T> alwaysFalse() {
		return rejectAll();
	}

	/**
	 * Returns a predicate that rejects all inputs.
	 *
	 * @param <T> the type of the input to the predicate
	 *
	 * @return a predicate that rejects all inputs
	 */
	public static <T> Predicate<T> rejectAll() {
		return JavaObjects.cast(ALWAYS_FALSE);
	}

	/**
	 * Returns a predicate with and operator between all elements of the input collection.
	 *
	 * @param <T> the type of the input to the predicate
	 *
	 * @param predicates collection of predicates
	 * @return a predicate with and operator between all elements of the input collection
	 */
	public static <T> Predicate<T> allOf(final Collection<Predicate<T>> predicates) {
		Predicate<T> result = acceptAll();
		for (Predicate<T> predicate : predicates) {
			result = result.and(predicate);
		}
		return result;
	}

	/**
	 * Returns a predicate with and operator between all elements of the input array.
	 *
	 * @param <T> the type of the input to the predicate
	 *
	 * @param predicates array of predicates
	 * @return a predicate with and operator between all elements of the input collection
	 */
	@SafeVarargs
	public static <T> Predicate<T> allOf(final Predicate<T>... predicates) {
		return allOf(List.of(predicates));
	}

	/**
	 * Returns a predicate with or operator between all elements of the input collection.
	 *
	 * @param <T> the type of the input to the predicate
	 *
	 * @param predicates collection of predicates
	 * @return a predicate with or operator between all elements of the input collection
	 */
	public static <T> Predicate<T> anyOf(final Collection<Predicate<T>> predicates) {
		Predicate<T> result = rejectAll();
		for (Predicate<T> predicate : predicates) {
			result = result.or(predicate);
		}
		return result;
	}

	/**
	 * Returns a predicate with or operator between all elements of the input array.
	 *
	 * @param <T> the type of the input to the predicate
	 *
	 * @param predicates array of predicates
	 * @return a predicate with or operator between all elements of the input collection
	 */
	@SafeVarargs
	public static <T> Predicate<T> anyOf(final Predicate<T>... predicates) {
		return anyOf(List.of(predicates));
	}

	/**
	 * Casts the object type of the input predicate to the object type of the output predicate.
	 *
	 * @param <T> input predicate type
	 * @param <U> output predicate type
	 *
	 * @param predicate input predicate
	 * @return output predicate
	 */
	public static <T, U> Predicate<U> cast(final Predicate<T> predicate) {
		return u -> predicate.test(JavaObjects.cast(u));
	}
}
