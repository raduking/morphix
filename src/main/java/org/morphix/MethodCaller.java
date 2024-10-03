package org.morphix;

import java.util.List;

import org.morphix.function.FieldValueFunction;
import org.morphix.function.SetterFunction;

/**
 * Helper interface for method calling.
 *
 * @author Radu Sebastian LAZIN
 */
public interface MethodCaller {

	/**
	 * Calls the {@code setterFunction} with the value returned by the
	 * {@code fieldValueFunction}.
	 *
	 * @param setterFunction a function which sets the value
	 * @param fieldValueFunction function which provides the value to set
	 */
	static <T> void call(final SetterFunction<T> setterFunction, final FieldValueFunction<T> fieldValueFunction) throws Exception {
		setterFunction.set(fieldValueFunction.value());
	}

	/**
	 * Calls the {@code setterFunction} with the value returned by the
	 * {@code fieldValueFunction}. Swallows any exception thrown by the
	 * {@code fieldValueFunction} and will return it in the {@code exceptions}
	 * list given as parameter.
	 *
	 * @param setterFunction a function which sets the value
	 * @param fieldValueFunction function which provides the value to set
	 * @param exceptions list of exceptions thrown by the operations in this
	 *            call
	 */
	static <T> void call(final SetterFunction<T> setterFunction, final FieldValueFunction<T> fieldValueFunction,
			final List<? super Exception> exceptions) {
		try {
			call(setterFunction, fieldValueFunction);
		} catch (Exception e) {
			exceptions.add(e);
		}
	}

	/**
	 * Calls the setter function only if the value is not null.
	 *
	 * @param setterFunction setter function
	 * @param value value to set
	 */
	static <T> void nonNullCall(final SetterFunction<T> setterFunction, final T value) {
		if (null != value) {
			setterFunction.set(value);
		}
	}
}
