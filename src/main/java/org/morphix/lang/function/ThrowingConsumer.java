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
