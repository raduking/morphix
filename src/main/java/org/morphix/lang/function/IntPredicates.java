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
import java.util.function.IntPredicate;

import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;

/**
 * Integer predicates utility methods.
 *
 * @author Radu Sebastian LAZIN
 */
public final class IntPredicates {

	/**
	 * A predicate that is always true.
	 */
	private static final IntPredicate ALWAYS_TRUE = t -> true;

	/**
	 * A predicate that is always false.
	 */
	private static final IntPredicate ALWAYS_FALSE = t -> false;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private IntPredicates() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a negative predicate.
	 *
	 * @param predicate predicate to negate
	 * @return a negative predicate
	 */
	public static IntPredicate not(final IntPredicate predicate) {
		return predicate.negate();
	}

	/**
	 * Returns a predicate that is always true.
	 *
	 * @return a predicate that is always true
	 */
	public static IntPredicate alwaysTrue() {
		return acceptAll();
	}

	/**
	 * Returns a predicate that accepts all inputs.
	 *
	 * @return a predicate that accepts all inputs
	 */
	public static IntPredicate acceptAll() {
		return JavaObjects.cast(ALWAYS_TRUE);
	}

	/**
	 * Returns a predicate that is always false.
	 *
	 * @return a predicate that is always false
	 */
	public static IntPredicate alwaysFalse() {
		return rejectAll();
	}

	/**
	 * Returns a predicate that rejects all inputs.
	 *
	 * @return a predicate that rejects all inputs
	 */
	public static IntPredicate rejectAll() {
		return JavaObjects.cast(ALWAYS_FALSE);
	}

	/**
	 * Returns a predicate with and operator between all elements of the input collection.
	 *
	 * @param predicates collection of predicates
	 * @return a predicate with and operator between all elements of the input collection
	 */
	public static IntPredicate allOf(final Collection<IntPredicate> predicates) {
		IntPredicate result = alwaysTrue();
		for (IntPredicate predicate : predicates) {
			result = result.and(predicate);
		}
		return result;
	}

	/**
	 * Returns a predicate with and operator between all elements of the input array.
	 *
	 * @param predicates array of predicates
	 * @return a predicate with and operator between all elements of the input collection
	 */
	@SafeVarargs
	public static IntPredicate allOf(final IntPredicate... predicates) {
		return allOf(List.of(predicates));
	}

	/**
	 * Returns a predicate with or operator between all elements of the input collection.
	 *
	 * @param predicates collection of predicates
	 * @return a predicate with or operator between all elements of the input collection
	 */
	public static IntPredicate anyOf(final Collection<IntPredicate> predicates) {
		IntPredicate result = alwaysFalse();
		for (IntPredicate predicate : predicates) {
			result = result.or(predicate);
		}
		return result;
	}

	/**
	 * Returns a predicate with or operator between all elements of the input array.
	 *
	 * @param predicates array of predicates
	 * @return a predicate with or operator between all elements of the input collection
	 */
	@SafeVarargs
	public static IntPredicate anyOf(final IntPredicate... predicates) {
		return anyOf(List.of(predicates));
	}
}
