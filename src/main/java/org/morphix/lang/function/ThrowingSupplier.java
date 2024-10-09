package org.morphix.lang.function;

import java.util.function.Supplier;

import org.morphix.lang.Unchecked;

/**
 * Functional interface to re-throw unchecked exceptions in functional calls. This class specifically accommodates any
 * {@link Supplier}.
 *
 * @param <T> return type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {

	/**
	 * Gets a result.
	 *
	 * @return a result
	 * @throws Throwable on any error
	 */
	T get() throws Throwable; // NOSONAR this declaration is only to accommodate all cases

	/**
	 * Removes the need to surround code with try/catch for checked exceptions effectively working like an unchecked
	 * exception.
	 *
	 * @param <T> return type
	 *
	 * @param s throwing function
	 * @return the supplier
	 */
	static <T> Supplier<T> unchecked(final ThrowingSupplier<T> s) {
		return () -> {
			try {
				return s.get();
			} catch (Throwable e) {
				return Unchecked.reThrow(e);
			}
		};
	}
}
