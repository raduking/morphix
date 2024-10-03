package org.morphix.extra;

import static org.morphix.reflection.predicates.ClassPredicates.canBeInstantiated;
import static org.morphix.reflection.predicates.ClassPredicates.isArrayListCompatible;
import static org.morphix.reflection.predicates.ClassPredicates.isIterable;
import static org.morphix.reflection.predicates.ClassPredicates.isMap;
import static org.morphix.reflection.predicates.ClassPredicates.isMapClass;
import static org.morphix.reflection.predicates.ClassPredicates.isQueue;
import static org.morphix.reflection.predicates.ClassPredicates.isSet;
import static org.morphix.reflection.predicates.Predicates.anyOf;
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

import org.morphix.function.InstanceFunction;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ConverterField;
import org.morphix.reflection.Primitives;

/**
 * Utility class for converter collections.
 *
 * @author Radu Sebastian LAZIN
 */
public class ConverterCollections {

	private static final Map<Predicate<Class<?>>, InstanceFunction<Collection<?>>> COLLECTION_INSTANCES_MAP = new HashMap<>();
	static {
		COLLECTION_INSTANCES_MAP.put(isArrayListCompatible(), ArrayList::new);
		COLLECTION_INSTANCES_MAP.put(isSet(), HashSet::new);
		COLLECTION_INSTANCES_MAP.put(isQueue(), LinkedList::new);
	}

	/**
	 * Private constructor.
	 */
	private ConverterCollections() {
		throw new UnsupportedOperationException("This class shouldn't be instantiated.");
	}

	/**
	 * Returns a predicate that verifies if the given class is a convertible {@link Iterable} class.
	 * Meaning that the generic converter can convert another {@link Iterable} to this class.
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
	 * Returns a predicate that verifies if the given class is a convertible {@link Map} class.
	 * Meaning that the generic converter can convert another {@link Map} to this class.
	 *
	 * @return a predicate that verifies if the given class is a convertible {@link Map} class
	 */
	public static Predicate<Class<?>> isConvertibleMapClass() {
		return isMapClass()
				.or(isMap().and(canBeInstantiated()));
	}

	/**
	 * Returns a predicate that verifies if the given class is a convertible {@link Iterable} class.
	 * Meaning that the generic converter can convert another {@link Iterable} to this class.
	 *
	 * @return a predicate that verifies if the given class is a convertible {@link Iterable} class
	 */
	public static Predicate<Type> isConvertibleIterableType() {
		return isAClassAnd(isConvertibleIterableClass())
				.or(isParameterizedType().and(rawType(isAClassAnd(isConvertibleIterableClass()))));
	}

	/**
	 * Returns a predicate that verifies if the given class is a convertible {@link Map} class.
	 * Meaning that the generic converter can convert another {@link Map} to this class.
	 *
	 * @return a predicate that verifies if the given class is a convertible {@link Map} class
	 */
	public static Predicate<Type> isConvertibleMapType() {
		return isAClassAnd(isConvertibleMapClass())
				.or(isParameterizedType().and(rawType(isAClassAnd(isConvertibleMapClass()))));
	}

	/**
	 * Creates a collection instance for the destination field.
	 *
	 * @param cls class of the collection (iterable)
	 * @return a new empty collection instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> Collection<T> newCollectionInstance(final Class<?> cls) {
		for (Map.Entry<Predicate<Class<?>>, InstanceFunction<Collection<?>>> entry : COLLECTION_INSTANCES_MAP.entrySet()) {
			if (entry.getKey().test(cls)) {
				return (Collection<T>) entry.getValue().instance();
			}
		}
		return (Collection<T>) Constructors.newInstance(cls);
	}

	/**
	 * Creates a collection instance for the destination field. It tries to
	 * create an object based on the getter return type, and if it doesn't
	 * succeed, it returns a collection based on the field type.
	 *
	 * @param fop the {@link ConverterField} describing the field
	 * @param <T> the generic type of the collection
	 * @return a new empty collection instance
	 */
	public static <T> Collection<T> newCollectionInstance(final ConverterField fop) {
		try {
			return newCollectionInstance(fop.toClass());
		} catch (Exception e) {
			return newCollectionInstance(fop.getField().getType());
		}
	}

	/**
	 * Returns a new {@link Map} instance.
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
	 * @param cls map class
	 * @return a new map instance
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> newMapInstance(final Class<?> cls) {
		if (isMapClass().test(cls)) {
			return new HashMap<>();
		}
		return (Map<K, V>) Constructors.newInstance(cls);
	}

	/**
	 * Creates a map instance for the destination field. It tries to create an
	 * object based on the getter return type, and if it doesn't succeed, it
	 * returns a map based on the field type.
	 *
	 * @param fop the {@link ConverterField} describing the field
	 * @param <K> the generic type of the map key
	 * @param <V> the generic type of the map value
	 * @return a new empty map instance
	 */
	public static <K, V> Map<K, V> newMapInstance(final ConverterField fop) {
		try {
			return newMapInstance(fop.toClass());
		} catch (Exception e) {
			return newMapInstance(fop.getField().getType());
		}
	}

	/**
	 * Returns a new array instance for the destination field.
	 *
	 * @param fop the {@link ConverterField} describing the field
	 * @return a new array instance
	 */
	public static Object newEmptyArrayInstance(final ConverterField fop) {
		return newEmptyArrayInstance(fop.toClass().getComponentType());
	}

	/**
	 * Returns a new array instance for the destination field.
	 *
	 * @param componentType component type
	 * @return a new array instance
	 */
	public static <T> T[] newEmptyArrayInstance(final Class<T> componentType) {
		return newArrayInstance(componentType, 0);
	}

	/**
	 * Returns a new array instance for the destination field.
	 *
	 * @param componentType component type
	 * @return a new array instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] newArrayInstance(final Class<T> componentType, final int size) {
		Class<?> computedComponentType = Primitives.getBoxedClass(componentType);
		if (null == computedComponentType) {
			computedComponentType = componentType;
		}
		return (T[]) Array.newInstance(computedComponentType, size);
	}
}
