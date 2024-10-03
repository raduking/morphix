package org.morphix.reflection.predicates;

import static java.util.function.Predicate.isEqual;
import static org.morphix.reflection.predicates.Predicates.not;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

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
		throw new UnsupportedOperationException("This class shouldn't be instantiated.");
	}

	/**
	 * Returns a predicate that verifies if a class is of type cls or any
	 * derived class of cls.
	 *
	 * @param cls class to verify
	 * @return a predicate that verifies if a class is of type cls or any
	 *         derived class of cls
	 */
	public static <T> Predicate<Class<?>> isA(final Class<T> cls) {
		return cls::isAssignableFrom;
	}

	/**
	 * Returns a predicate that verifies if a class is exactly of type cls.
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
	 * Returns a predicate that verifies if the converter can use a simple
	 * {@link ArrayList} for the destination collection. Meaning that the
	 * destination class is any {@link List} or superclass of {@link List}.
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
		return hasDefaultConstructor()
				.and(not(isAbstract()));
	}

}
