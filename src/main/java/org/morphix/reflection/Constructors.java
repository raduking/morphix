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
package org.morphix.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Utility class for Java constructors.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Constructors {

	/**
	 * Exception message for when a class should not be instantiated.
	 */
	String MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED = "This class should not be instantiated!";

	/**
	 * Creates a new instance for the given type with its default constructor. When a new instance cannot be created It
	 * throws a {@link ReflectionException} an unchecked exception with a detailed message for why it failed.
	 *
	 * @param <T> instance type
	 *
	 * @param cls class for which to create an instance
	 * @return an object of type T
	 */
	static <T> T newInstance(final Class<T> cls) {
		try {
			return getDefaultConstructor(cls).newInstance();
		} catch (IllegalAccessException e) {
			throw new ReflectionException("Default constructor is not accessible for class: " + cls.getCanonicalName(), e);
		} catch (InstantiationException e) {
			throw new ReflectionException("Could not instantiate class, the class object represents an abstract class, an interface," +
					" an array class, a primitive type, or void: " + cls.getCanonicalName(), e);
		} catch (InvocationTargetException e) {
			throw new ReflectionException("Could not instantiate class, default constructor threw exception: " +
					e.getTargetException() + ", for class: " + cls.getCanonicalName(), e);
		}
	}

	/**
	 * Returns the default constructor for the given type.
	 *
	 * @param <T> instance type
	 *
	 * @param cls class for which to find the constructor
	 * @return constructor
	 */
	static <T> Constructor<T> getDefaultConstructor(final Class<T> cls) {
		try {
			return cls.getDeclaredConstructor();
		} catch (NoSuchMethodException e) {
			throw new ReflectionException("Default constructor is not defined for class: " + cls.getCanonicalName(), e);
		}
	}

	/**
	 * Returns a constructor for the given type with the constructor that matches the given parameter types.
	 *
	 * @param <T> instance type
	 *
	 * @param cls class for which to find the constructor
	 * @param paramTypes parameter types
	 * @return constructor
	 */
	static <T> Constructor<T> getDeclaredConstructor(final Class<T> cls, final Class<?>... paramTypes) {
		try {
			return cls.getDeclaredConstructor(paramTypes);
		} catch (NoSuchMethodException e) {
			throw new ReflectionException("No constructor found for class: " + cls.getCanonicalName()
					+ " with parameters: " + (null != paramTypes ? List.of(paramTypes) : "none"), e);
		}
	}

	/**
	 * Returns an {@link UnsupportedOperationException} with the message
	 * <code>"This class should not be instantiated!"</code> to be used in private constructors for utility classes.
	 *
	 * @return a new unsupported operation exception
	 */
	static UnsupportedOperationException unsupportedOperationException() {
		return new UnsupportedOperationException(MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED);
	}

	/**
	 * Interface which groups all methods that ignore constructor access modifiers.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface IgnoreAccess {

		/**
		 * Creates a new instance for the given type with its default constructor ignoring its visibility.
		 *
		 * @param <T> instance type
		 *
		 * @param cls class for which to create an instance
		 * @return an object of type T
		 */
		static <T> T newInstance(final Class<T> cls) {
			Constructor<T> constructor = getDefaultConstructor(cls);
			return newInstance(constructor);
		}

		/**
		 * Creates a new instance for the given type with a given constructor ignoring its visibility.
		 * <p>
		 * TODO: add more detailed information.
		 *
		 * @param <T> instance type
		 *
		 * @param constructor with which to create an instance
		 * @param args constructor arguments
		 * @return an object of type T
		 */
		static <T> T newInstance(final Constructor<T> constructor, final Object... args) {
			try (ConstructorAccessor<T> ignored = new ConstructorAccessor<>(constructor)) {
				return constructor.newInstance(args);
			} catch (NullPointerException | InvocationTargetException
					| IllegalAccessException | IllegalAccessError
					| InstantiationException | InstantiationError e) {
				throw new ReflectionException("Could not instantiate class: " + constructor.getDeclaringClass().getCanonicalName(), e);
			}
		}

		/**
		 * Creates a new instance. It uses instance creator if it can't create an instance normally.
		 *
		 * @param <T> instance type
		 *
		 * @param cls class to create an instance for
		 * @param instanceCreator instance creator function when {@link Constructors#newInstance(Class)} fails
		 * @return new instance
		 */
		static <T> T newInstance(final Class<T> cls, final InstanceCreator instanceCreator) {
			try {
				return IgnoreAccess.newInstance(cls);
			} catch (ReflectionException e) {
				return instanceCreator.newInstance(cls);
			}
		}

	}

}
