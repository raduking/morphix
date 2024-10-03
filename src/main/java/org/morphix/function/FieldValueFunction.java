package org.morphix.function;

/**
 * Conversion functional interface for destination field values.
 *
 * @param <T> return type for the value function
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface FieldValueFunction<T> {

	/**
	 * Applies this function and returns a value to be used.
	 *
	 * @return a value
	 * @throws Exception any exception thrown from the function
	 */
	T value() throws Exception;

	/**
	 * Returns a {@link FieldValueFunction} from the given value.
	 *
	 * @param value value to be returned
	 * @return a field value function
	 */
	static <T> FieldValueFunction<T> from(final T value) {
		return () -> value;
	}

}
