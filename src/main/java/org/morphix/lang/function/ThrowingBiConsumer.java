package org.morphix.lang.function;

import java.util.function.BiConsumer;

import org.morphix.lang.Unchecked;

/**
 * Functional interface to re-throw unchecked exceptions in functional calls. This class specifically accommodates any
 * {@link BiConsumer}.
 *
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ThrowingBiConsumer<T, U> {

	/**
	 * Performs this operation on the given arguments.
	 *
	 * @param t the first input argument
	 * @param u the second input argument
	 * @throws Throwable on any error
	 */
	void accept(T t, U u) throws Throwable; // NOSONAR this declaration is only to accommodate all cases

	/**
	 * Removes the need to surround code with try/catch for checked exceptions effectively working like an unchecked
	 * exception.
	 *
	 * @param <T> first argument type
	 * @param <U> second argument type
	 *
	 * @param c throwing bi-consumer
	 * @return the bi-consumer
	 */
	static <T, U> BiConsumer<T, U> unchecked(final ThrowingBiConsumer<T, U> c) {
		return (t, u) -> {
			try {
				c.accept(t, u);
			} catch (Throwable e) {
				Unchecked.reThrow(e);
			}
		};
	}
}
