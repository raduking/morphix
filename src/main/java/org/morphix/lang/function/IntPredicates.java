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

import java.util.Collection;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * Integer predicates utility methods.
 *
 * @author Radu Sebastian LAZIN
 */
public interface IntPredicates {

	/**
	 * Returns a negative predicate.
	 *
	 * @param predicate predicate to negate
	 * @return a negative predicate
	 */
	static IntPredicate not(final IntPredicate predicate) {
		return predicate.negate();
	}

	/**
	 * Returns a predicate with and operator between all elements of the input collection.
	 *
	 * @param predicates collection of predicates
	 * @return a predicate with and operator between all elements of the input collection
	 */
	static IntPredicate allOf(final Collection<IntPredicate> predicates) {
		IntPredicate result = t -> true;
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
	static IntPredicate allOf(final IntPredicate... predicates) {
		return allOf(List.of(predicates));
	}

	/**
	 * Returns a predicate with or operator between all elements of the input collection.
	 *
	 * @param predicates collection of predicates
	 * @return a predicate with or operator between all elements of the input collection
	 */
	static IntPredicate anyOf(final Collection<IntPredicate> predicates) {
		IntPredicate result = t -> false;
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
	static IntPredicate anyOf(final IntPredicate... predicates) {
		return anyOf(List.of(predicates));
	}

}
