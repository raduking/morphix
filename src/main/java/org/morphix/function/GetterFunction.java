package org.morphix.function;

/**
 * Conversion functional interface for source getters.
 *
 * @param <T> type of the value to get
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface GetterFunction<T> {

	/**
	 * Get method.
	 */
	T get();

}
