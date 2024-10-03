package org.morphix.reflection.predicates;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Predicates utility methods.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Predicates {

	/**
	 * Returns a negative predicate.
	 *
	 * @param predicate predicate to negate
	 * @return a negative predicate
	 */
	static <T> Predicate<T> not(final Predicate<T> predicate) {
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
	static <T> Predicate<T> allOf(final Collection<Predicate<T>> predicates) {
		Predicate<T> result = t -> true;
		for (Predicate<T> predicate : predicates) {
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
	static <T> Predicate<T> allOf(final Predicate<T>... predicates) {
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
	static <T> Predicate<T> anyOf(final Collection<Predicate<T>> predicates) {
		Predicate<T> result = t -> false;
		for (Predicate<T> predicate : predicates) {
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
	static <T> Predicate<T> anyOf(final Predicate<T>... predicates) {
		return anyOf(List.of(predicates));
	}

	/**
	 * Casts the type of the input predicate to the type of the output
	 * predicate.
	 *
	 * @param <T> input predicate type
	 * @param <U> output predicate type
	 * @param predicate input predicate
	 * @return output predicate
	 */
	@SuppressWarnings("unchecked")
	static <T, U> Predicate<U> cast(final Predicate<T> predicate) {
		return u -> predicate.test((T) u);
	}

}
