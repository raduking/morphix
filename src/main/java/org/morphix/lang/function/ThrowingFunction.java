package org.morphix.lang.function;

import java.util.function.Function;

import org.morphix.lang.Unchecked;

/**
 * Functional interface to re-throw unchecked exceptions in functional calls. This class specifically accommodates any
 * {@link Function}.
 *
 * @param <T> argument type
 * @param <R> return type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 * @throws Throwable on any error
	 */
	R apply(T t) throws Throwable; // NOSONAR this declaration is only to accommodate all cases

	/**
	 * Removes the need to surround code with try/catch for checked exceptions effectively working like an unchecked
	 * exception.
	 *
	 * @param <T> argument type
	 * @param <R> return type
	 *
	 * @param f throwing function
	 * @return the function
	 */
	static <T, R> Function<T, R> unchecked(final ThrowingFunction<T, R> f) {
		return t -> {
			try {
				return f.apply(t);
			} catch (Throwable e) {
				return Unchecked.reThrow(e);
			}
		};
	}
}
