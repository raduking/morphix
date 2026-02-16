/*
 * Copyright 2026 the original author or authors.
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
package org.morphix.reflection.predicates;

import static java.util.function.Predicate.isEqual;
import static org.morphix.lang.function.Predicates.allOf;
import static org.morphix.lang.function.Predicates.not;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import org.morphix.reflection.Constructors;

/**
 * Class predicates.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ClassPredicates {

	/**
	 * Private constructor.
	 */
	private ClassPredicates() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a predicate that verifies if a class is of type cls or any derived class of cls.
	 *
	 * @param <T> the class type of the input to the predicate
	 *
	 * @param cls class to verify
	 * @return a predicate that verifies if a class is of type cls or any derived class of cls
	 */
	public static <T> Predicate<Class<?>> isA(final Class<T> cls) {
		return cls::isAssignableFrom;
	}

	/**
	 * Returns a predicate that verifies if a class is exactly of type cls.
	 *
	 * @param <T> the class type of the input to the predicate
	 *
	 * @param cls class to verify
	 * @return a predicate that verifies if a class is exactly of type cls
	 */
	public static <T> Predicate<Class<?>> isClass(final Class<T> cls) {
		return isEqual(cls);
	}

	/**
	 * Returns a predicate that verifies if a class is a {@link CharSequence}.
	 *
	 * @return a predicate that verifies if a class is a {@link CharSequence}
	 */
	public static Predicate<Class<?>> isCharSequence() {
		return isA(CharSequence.class);
	}

	/**
	 * Returns a predicate that verifies if a class is an {@link Enum}.
	 *
	 * @return a predicate that verifies if a class is an {@link Enum}.
	 */
	public static Predicate<Class<?>> isEnum() {
		return isA(Enum.class);
	}

	/**
	 * Returns a predicate that verifies if the class is an {@link Iterable}.
	 *
	 * @return a predicate that verifies if the class is an {@link Iterable}
	 */
	public static Predicate<Class<?>> isIterable() {
		return isA(Iterable.class);
	}

	/**
	 * Returns a predicate that verifies if the class is a {@link Set}.
	 *
	 * @return a predicate that verifies if the class is a {@link Set}
	 */
	public static Predicate<Class<?>> isSet() {
		return isA(Set.class);
	}

	/**
	 * Returns a predicate that verifies if the class is a {@link Queue}.
	 *
	 * @return a predicate that verifies if the class is a {@link Queue}
	 */
	public static Predicate<Class<?>> isQueue() {
		return isA(Queue.class);
	}

	/**
	 * Returns a predicate that verifies if the class is a {@link Map}.
	 *
	 * @return a predicate that verifies if the class is a {@link Map}
	 */
	public static Predicate<Class<?>> isMap() {
		return isA(Map.class);
	}

	/**
	 * Returns a predicate that verifies if the class is an {@link Array}.
	 *
	 * @return a predicate that verifies if the class is an {@link Array}
	 */
	public static Predicate<Class<?>> isArray() {
		return Class::isArray;
	}

	/**
	 * Returns a predicate that verifies if the class is exactly a {@link Map}.
	 *
	 * @return a predicate that verifies if the class is exactly a {@link Map}
	 */
	public static Predicate<Class<?>> isMapClass() {
		return isClass(Map.class);
	}

	/**
	 * Returns a predicate that verifies if a class is abstract.
	 *
	 * @return a predicate that verifies if a class is abstract
	 */
	public static Predicate<Class<?>> isAbstract() {
		return cls -> Modifier.isAbstract(cls.getModifiers());
	}

	/**
	 * Returns a predicate that verifies if class is any {@link List} or superclass of {@link List}.
	 *
	 * @return a predicate that verifies if the class is array list compatible
	 */
	public static Predicate<Class<?>> isArrayListCompatible() {
		return cls -> cls.isAssignableFrom(List.class);
	}

	/**
	 * Returns a predicate that verifies if the class has a default constructor.
	 *
	 * @return a predicate that verifies if the class has a default constructor
	 */
	public static Predicate<Class<?>> hasDefaultConstructor() {
		return cls -> {
			try {
				cls.getDeclaredConstructor();
			} catch (NoSuchMethodException e) {
				return false;
			}
			return true;
		};
	}

	/**
	 * Returns a predicate that verifies if a class can be instantiated.
	 *
	 * @return a predicate that verifies if a class can be instantiated
	 */
	public static Predicate<Class<?>> canBeInstantiated() {
		return allOf(hasDefaultConstructor(), not(isAbstract()));
	}
}
