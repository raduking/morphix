package org.morphix.function;

/**
 * Instance function based on a class.
 *
 * @param <T> instance type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface Instantiator<T> {

	T instance(Class<?> cls);

}
