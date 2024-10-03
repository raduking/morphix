package org.morphix.reflection.predicates;

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
	 * Returns a predicate with and operator between all elements of the input
	 * collection.
	 *
	 * @param predicates collection of predicates
	 * @return a predicate with and operator between all elements of the input
	 *         collection
	 */
	static IntPredicate allOf(final Collection<IntPredicate> predicates) {
		IntPredicate result = t -> true;
		for (IntPredicate predicate : predicates) {
			result = result.and(predicate);
		}
		return result;
	}

	/**
	 * Returns a predicate with and operator between all elements of the input
	 * array.
	 *
	 * @param predicates array of predicates
	 * @return a predicate with and operator between all elements of the input
	 *         collection
	 */
	@SafeVarargs
	static IntPredicate allOf(final IntPredicate... predicates) {
		return allOf(List.of(predicates));
	}

	/**
	 * Returns a predicate with or operator between all elements of the input
	 * collection.
	 *
	 * @param predicates collection of predicates
	 * @return a predicate with or operator between all elements of the input
	 *         collection
	 */
	static IntPredicate anyOf(final Collection<IntPredicate> predicates) {
		IntPredicate result = t -> false;
		for (IntPredicate predicate : predicates) {
			result = result.or(predicate);
		}
		return result;
	}

	/**
	 * Returns a predicate with or operator between all elements of the input
	 * array.
	 *
	 * @param predicates array of predicates
	 * @return a predicate with or operator between all elements of the input
	 *         collection
	 */
	@SafeVarargs
	static IntPredicate anyOf(final IntPredicate... predicates) {
		return anyOf(List.of(predicates));
	}

}
