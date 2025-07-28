/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.lang.function;

import static org.morphix.reflection.predicates.MemberPredicates.isNotStatic;

import java.util.function.Supplier;

import org.morphix.reflection.Fields;

/**
 * Instance functional interface for destination instance creation.
 *
 * @param <T> destination type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface InstanceFunction<T> extends Supplier<T> {

	/**
	 * Returns an instance of type T.
	 *
	 * @return an instance of type T
	 */
	T instance();

	/**
	 * Returns an instance of type T.
	 *
	 * @return an instance of type T
	 */
	@Override
	default T get() {
		return instance();
	}

	/**
	 * Returns an instance function that returns the given instance.
	 *
	 * @param <T> destination instance type
	 *
	 * @param instance instance to return in the function
	 * @return an instance function
	 */
	static <T> InstanceFunction<T> instanceFunction(final T instance) {
		return () -> instance;
	}

	/**
	 * Returns an instance function that returns the instance given as parameter. Usable in conversions when an existing
	 * instance is needed.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * 		ClassA objectA = ...
	 * 		ClassB objectB = ...
	 * 		convertFrom(objectA, to(objectB));
	 * </pre>
	 * <p>
	 * will use <code>objectB</code> as instance to copy the fields to.
	 *
	 * @param <T> destination instance type
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
	 * @param <T> instance type
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
	 * @param <T> instance type
	 *
	 * @param instance object to reset
	 * @return instance function with the reset object
	 */
	static <T> InstanceFunction<T> toEmpty(final T instance) {
		return toEmpty(instanceFunction(instance));
	}

	/**
	 * Returns an instance function with the instance created by the instanceFunction with all fields reset. This method is
	 * useful when you want to ignore any initialization done in the default constructor or private fields.
	 *
	 * @param <T> instance type
	 *
	 * @param instanceFunction instance function
	 * @return an instance function which will create an instance with all fields set to null
	 */
	static <T> InstanceFunction<T> toEmpty(final InstanceFunction<T> instanceFunction) {
		T instance = instanceFunction.instance();
		Fields.getDeclaredFieldsInHierarchy(instance).stream()
				// only non static fields
				.filter(isNotStatic())
				.forEach(field -> Fields.reset(field, instance));
		return instanceFunction(instance);
	}

}
