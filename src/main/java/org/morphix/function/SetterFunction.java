package org.morphix.function;

/**
 * Conversion functional interface for destination setters.
 *
 * @param <T> type of the value to set
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface SetterFunction<T> {

	/**
	 * Set method.
	 *
	 * @param t value to set
	 */
	void set(T t);

	/**
	 * Returns a new setter function based on the given setter that only sets
	 * the value if value is not null.
	 *
	 * @param setter setter
	 * @return a setter that only sets the value if value is not null
	 */
	static <T> SetterFunction<T> nonNullSetter(final SetterFunction<T> setter) {
		return t -> {
			if (null != t) {
				setter.set(t);
			}
		};
	}
}
