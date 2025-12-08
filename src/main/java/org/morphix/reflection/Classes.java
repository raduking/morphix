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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.morphix.lang.JavaObjects;

/**
 * Utility reflection methods for classes.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Classes {

	/**
	 * Returns a class based on a class name.
	 *
	 * @param <T> returned type
	 *
	 * @param className the {@linkplain ClassLoader##binary-name binary name}
	 * @return a class based on a class name
	 * @throws ReflectionException if the class cannot be loaded
	 */
	static <T> Class<T> getOne(final String className) {
		try {
			return JavaObjects.cast(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new ReflectionException("Could not load class: " + className, e);
		}
	}

	/**
	 * Returns a class based on a class name.
	 *
	 * @param <T> returned type
	 *
	 * @param className the {@linkplain ClassLoader##binary-name binary name}
	 * @param classLoader the class loader used to load the class
	 * @return a class based on a class name
	 * @throws ReflectionException if the class cannot be loaded
	 */
	static <T> Class<T> getOne(final String className, final ClassLoader classLoader) {
		try {
			return JavaObjects.cast(Class.forName(className, false, classLoader));
		} catch (ClassNotFoundException e) {
			throw new ReflectionException("Could not load class: " + className, e);
		}
	}

	/**
	 * Returns the subclass of the expected parent.
	 *
	 * @param expectedParent expected parent
	 * @param child some child class
	 * @return the subclass of the expected parent
	 * @throws ReflectionException if the expected parent is not found
	 */
	static Class<?> findSubclass(final Class<?> expectedParent, final Class<?> child) {
		Class<?> parent = child.getSuperclass();
		if (Object.class == parent) {
			throw new ReflectionException("The parent of " + child.getCanonicalName() + " is not a " + expectedParent.getCanonicalName());
		}
		if (expectedParent == parent) {
			return child;
		}
		return findSubclass(expectedParent, parent);
	}

	/**
	 * Creates a mutable set of classes.
	 *
	 * @param classes the classes to add in the set
	 * @return a mutable set of classes
	 */
	static Set<Class<?>> mutableSetOf(final Class<?>... classes) {
		Set<Class<?>> set = new HashSet<>();
		Collections.addAll(set, classes);
		return set;
	}

	/**
	 * Interface which groups all methods that return null and don't throw exceptions on expected errors. This functions as
	 * a name space so that the method names inside keep the same name pattern.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface Safe {

		/**
		 * Returns a class based on a class name, if the class is not found it returns {@code null}.
		 *
		 * @param <T> returned type
		 *
		 * @param className the {@linkplain ClassLoader##binary-name binary name}
		 * @return a class based on a class name, null if class is not found
		 */
		static <T> Class<T> getOne(final String className) {
			try {
				return Classes.getOne(className);
			} catch (ReflectionException e) {
				return null;
			}
		}

	}

}
