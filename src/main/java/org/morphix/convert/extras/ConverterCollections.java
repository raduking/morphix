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
package org.morphix.convert.extras;

import static org.morphix.lang.function.Predicates.anyOf;
import static org.morphix.reflection.predicates.ClassPredicates.canBeInstantiated;
import static org.morphix.reflection.predicates.ClassPredicates.isArrayListCompatible;
import static org.morphix.reflection.predicates.ClassPredicates.isIterable;
import static org.morphix.reflection.predicates.ClassPredicates.isMap;
import static org.morphix.reflection.predicates.ClassPredicates.isMapClass;
import static org.morphix.reflection.predicates.ClassPredicates.isQueue;
import static org.morphix.reflection.predicates.ClassPredicates.isSet;
import static org.morphix.reflection.predicates.TypePredicates.isAClassAnd;
import static org.morphix.reflection.predicates.TypePredicates.isParameterizedType;
import static org.morphix.reflection.predicates.TypePredicates.rawType;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.InstanceFunction;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.Primitives;

/**
 * Utility class for converter collections.
 *
 * @author Radu Sebastian LAZIN
 */
public class ConverterCollections {

	/**
	 * Map that associates a predicate with a collection instance function.
	 */
	private static final Map<Predicate<Class<?>>, InstanceFunction<Collection<?>>> COLLECTION_INSTANCES_MAP = new HashMap<>();
	static {
		COLLECTION_INSTANCES_MAP.put(isArrayListCompatible(), ArrayList::new);
		COLLECTION_INSTANCES_MAP.put(isSet(), HashSet::new);
		COLLECTION_INSTANCES_MAP.put(isQueue(), LinkedList::new);
	}

	/**
	 * Returns a predicate that verifies if the given class is a convertible {@link Iterable} class, meaning that the
	 * converter can convert another {@link Iterable} to this class.
	 *
	 * @return a predicate that verifies if the given class is a convertible {@link Iterable} class
	 */
	public static Predicate<Class<?>> isConvertibleIterableClass() {
		return isIterable()
				.and(anyOf(
						isArrayListCompatible(),
						isSet(),
						isQueue(),
						canBeInstantiated()));
	}

	/**
	 * Returns a predicate that verifies if the given class is a convertible {@link Map} class, meaning that the converter
	 * can convert another {@link Map} to this class.
	 *
	 * @return a predicate that verifies if the given class is a convertible {@link Map} class
	 */
	public static Predicate<Class<?>> isConvertibleMapClass() {
		return isMapClass()
				.or(isMap().and(canBeInstantiated()));
	}

	/**
	 * Returns a predicate that verifies if the given class is a convertible {@link Iterable} class, meaning that the
	 * converter can convert another {@link Iterable} to this class.
	 *
	 * @return a predicate that verifies if the given class is a convertible {@link Iterable} class
	 */
	public static Predicate<Type> isConvertibleIterableType() {
		return isAClassAnd(isConvertibleIterableClass())
				.or(isParameterizedType().and(rawType(isAClassAnd(isConvertibleIterableClass()))));
	}

	/**
	 * Returns a predicate that verifies if the given class is a convertible {@link Map} class, meaning that the converter
	 * can convert another {@link Map} to this class.
	 *
	 * @return a predicate that verifies if the given class is a convertible {@link Map} class
	 */
	public static Predicate<Type> isConvertibleMapType() {
		return isAClassAnd(isConvertibleMapClass())
				.or(isParameterizedType().and(rawType(isAClassAnd(isConvertibleMapClass()))));
	}

	/**
	 * Creates a collection instance given the collection class.
	 *
	 * @param <T> collection element type
	 *
	 * @param cls class of the collection (iterable)
	 * @return a new empty collection instance
	 */
	public static <T> Collection<T> newCollectionInstance(final Class<?> cls) {
		for (Map.Entry<Predicate<Class<?>>, InstanceFunction<Collection<?>>> entry : COLLECTION_INSTANCES_MAP.entrySet()) {
			if (entry.getKey().test(cls)) {
				return JavaObjects.cast(entry.getValue().instance());
			}
		}
		return JavaObjects.cast(Constructors.newInstance(cls));
	}

	/**
	 * Creates a collection instance for the destination field. It tries to create an object based on the getter return
	 * type, and if it doesn't succeed, it returns a collection based on the field type.
	 *
	 * @param <T> the generic type of the collection
	 *
	 * @param fop the {@link ExtendedField} describing the field
	 * @return a new empty collection instance
	 */
	public static <T> Collection<T> newCollectionInstance(final ExtendedField fop) {
		try {
			return newCollectionInstance(fop.toClass());
		} catch (Exception e) {
			return newCollectionInstance(fop.getField().getType());
		}
	}

	/**
	 * Returns a new {@link Map} instance.
	 *
	 * @param <K> map key type
	 * @param <V> map value type
	 *
	 * @param type map type
	 * @return a new map instance
	 */
	public static <K, V> Map<K, V> newMapInstance(final Type type) {
		Class<?> cls = (Class<?>) ((ParameterizedType) type).getRawType();
		return newMapInstance(cls);
	}

	/**
	 * Returns a new {@link Map} instance.
	 *
	 * @param <K> map key type
	 * @param <V> map value type
	 *
	 * @param cls map class
	 * @return a new map instance
	 */
	public static <K, V> Map<K, V> newMapInstance(final Class<?> cls) {
		if (isMapClass().test(cls)) {
			return new HashMap<>();
		}
		return JavaObjects.cast(Constructors.newInstance(cls));
	}

	/**
	 * Creates a map instance given the extended field. It tries to create an object based on the getter return type, and if
	 * it doesn't succeed, it returns a map based on the field type.
	 *
	 * @param <K> the generic type of the map key
	 * @param <V> the generic type of the map value
	 *
	 * @param extendedField the {@link ExtendedField} describing the field
	 * @return a new empty map instance
	 */
	public static <K, V> Map<K, V> newMapInstance(final ExtendedField extendedField) {
		try {
			return newMapInstance(extendedField.toClass());
		} catch (Exception e) {
			return newMapInstance(extendedField.getField().getType());
		}
	}

	/**
	 * Returns a new array instance given the extended field.
	 *
	 * @param extendedField the {@link ExtendedField} describing the field
	 * @return a new array instance
	 */
	public static Object newEmptyArrayInstance(final ExtendedField extendedField) {
		return newEmptyArrayInstance(extendedField.toClass().getComponentType());
	}

	/**
	 * Returns a new array instance given the component type.
	 *
	 * @param <T> array element type
	 *
	 * @param componentType component type
	 * @return a new array instance
	 */
	public static <T> T[] newEmptyArrayInstance(final Class<T> componentType) {
		return newArrayInstance(componentType, 0);
	}

	/**
	 * Returns a new array instance given the component type and the array size.
	 *
	 * @param <T> array element type
	 *
	 * @param componentType component type
	 * @param size array size
	 * @return a new array instance
	 */
	public static <T> T[] newArrayInstance(final Class<T> componentType, final int size) {
		Class<?> computedComponentType = Primitives.getBoxedClass(componentType);
		if (null == computedComponentType) {
			computedComponentType = componentType;
		}
		return JavaObjects.cast(Array.newInstance(computedComponentType, size));
	}

	/**
	 * Private constructor.
	 */
	private ConverterCollections() {
		throw Constructors.unsupportedOperationException();
	}

}
