package org.morphix.lang.function;

import org.morphix.lang.Unchecked;

/**
 * Functional interface to re-throw unchecked exceptions in functional calls. This class specifically accommodates any
 * {@link Runnable}.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ThrowingRunnable {

	/**
	 * Runs this operation.
	 *
	 * @throws Throwable on any error
	 */
	void run() throws Throwable; // NOSONAR this declaration is only to accommodate all cases

	/**
	 * Removes the need to surround code with try/catch for checked exceptions effectively working like an unchecked
	 * exception.
	 *
	 * @param r throwing runnable
	 * @return the runnable
	 */
	static Runnable unchecked(final ThrowingRunnable r) {
		return () -> {
			try {
				r.run();
			} catch (Throwable e) {
				Unchecked.reThrow(e);
			}
		};
	}
}
