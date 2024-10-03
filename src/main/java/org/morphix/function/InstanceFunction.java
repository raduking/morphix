package org.morphix.function;

import java.lang.reflect.Modifier;

import org.morphix.reflection.Fields;

/**
 * Instance functional interface for destination instance creation.
 *
 * @param <T> destination type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface InstanceFunction<T> {

	/**
	 * Returns an instance of type T.
	 *
	 * @return an instance of type T
	 */
	T instance();

	/**
	 * Returns an instance function that returns the given instance.
	 *
	 * @param instance instance to return in the function
	 * @return an instance function
	 */
	static <T> InstanceFunction<T> instanceFunction(final T instance) {
		return () -> instance;
	}

	/**
	 * Returns an instance function that returns the instance given as
	 * parameter. Usable in conversions when an existing instance is needed.
	 * <p>
	 * Example:
	 * <p>
	 *
	 * <pre>
	 * 		ClassA objectA = ...
	 * 		ClassB objectB = ...
	 * 		convertFrom(objectA, to(objectB));
	 * </pre>
	 * <p>
	 * will use <code>objectB</code> as instance to copy the fields to.
	 *
	 * @param instance instance to be returned in the instance function
	 * @return an instance function
	 */
	static <T> InstanceFunction<T> to(final T instance) {
		return instanceFunction(instance);
	}

	/**
	 * Resets all fields on the given object (instance).
	 *
	 * @param instance object to reset
	 * @return instance function with the reset object
	 */
	static <T> InstanceFunction<T> reset(final T instance) {
		return toEmpty(instanceFunction(instance));
	}

	/**
	 * Resets all fields on the given object (instance).
	 *
	 * @param instance object to reset
	 * @return instance function with the reset object
	 */
	static <T> InstanceFunction<T> toEmpty(final T instance) {
		return toEmpty(instanceFunction(instance));
	}

	/**
	 * Returns an instance function with the instance created by the
	 * instanceFunction with all fields reset. This method is useful when you
	 * want to ignore any initialization done in the default constructor or
	 * private fields.
	 *
	 * @param instanceFunction instance function
	 * @return an instance function which will create an instance with all
	 *         fields set to null
	 */
	static <T> InstanceFunction<T> toEmpty(final InstanceFunction<T> instanceFunction) {
		T instance = instanceFunction.instance();
		Fields.getDeclaredFieldsInHierarchy(instance).stream()
				// only non static fields
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.forEach(field -> Fields.resetField(instance, field));
		return instanceFunction(instance);
	}

}
