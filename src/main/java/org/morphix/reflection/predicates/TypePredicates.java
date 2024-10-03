package org.morphix.reflection.predicates;

import static org.morphix.reflection.predicates.Predicates.cast;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

/**
 * {@link Type} predicates.
 *
 * @author Radu Sebastian LAZIN
 */
public final class TypePredicates {

	/**
	 * Private constructor.
	 */
	private TypePredicates() {
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
	public static <T> Predicate<Type> isA(final Class<T> cls) {
		return cls::isInstance;
	}

	/**
	 * Returns a predicate that verifies if the class is an {@link Array}.
	 *
	 * @return a predicate that verifies if the class is an {@link Array}
	 */
	public static Predicate<Type> isAClass() {
		return isA(Class.class);
	}

	/**
	 * Returns a predicate that tests if the type is a class and another class
	 * predicate.
	 *
	 * @param classPredicate class predicate
	 * @return a predicate that tests if the type is a class and another class
	 *         predicate
	 */
	public static Predicate<Type> isAClassAnd(final Predicate<Class<?>> classPredicate) {
		return isAClass().and(cast(classPredicate));
	}

	/**
	 * Returns a predicate that verifies if a class is a {@link CharSequence}.
	 *
	 * @return a predicate that verifies if a class is a {@link CharSequence}
	 */
	public static Predicate<Type> isCharSequence() {
		return isAClassAnd(ClassPredicates.isCharSequence());
	}

	/**
	 * Returns a predicate that verifies if a class is an {@link Enum}.
	 *
	 * @return a predicate that verifies if a class is an {@link Enum}.
	 */
	public static Predicate<Type> isEnum() {
		return isAClassAnd(ClassPredicates.isEnum());
	}

	/**
	 * Returns a predicate that verifies if the class is an {@link Iterable}.
	 *
	 * @return a predicate that verifies if the class is an {@link Iterable}
	 */
	public static Predicate<Type> isIterable() {
		return isAClassAnd(ClassPredicates.isIterable())
				.or(isParameterizedType().and(rawType(isAClassAnd(ClassPredicates.isIterable()))));
	}

	/**
	 * Returns a predicate that verifies if the class is a {@link Set}.
	 *
	 * @return a predicate that verifies if the class is a {@link Set}
	 */
	public static Predicate<Type> isSet() {
		return isAClassAnd(ClassPredicates.isSet())
				.or(isParameterizedType().and(rawType(isAClassAnd(ClassPredicates.isSet()))));
	}

	/**
	 * Returns a predicate that verifies if the class is a {@link Queue}.
	 *
	 * @return a predicate that verifies if the class is a {@link Queue}
	 */
	public static Predicate<Type> isQueue() {
		return isAClassAnd(ClassPredicates.isQueue())
				.or(isParameterizedType().and(rawType(isAClassAnd(ClassPredicates.isQueue()))));
	}

	/**
	 * Returns a predicate that verifies if the class is a {@link Map}.
	 *
	 * @return a predicate that verifies if the class is a {@link Map}
	 */
	public static Predicate<Type> isMap() {
		return isAClassAnd(ClassPredicates.isMap())
				.or(isParameterizedType().and(rawType(isAClassAnd(ClassPredicates.isMap()))));
	}

	/**
	 * Returns a predicate that verifies if the class is an array type.
	 *
	 * @return a predicate that verifies if the class is an array type
	 */
	public static Predicate<Type> isArray() {
		return isAClassAnd(cast(ClassPredicates.isArray()))
				.or(isA(GenericArrayType.class));
	}

	/**
	 * Returns a predicate that verifies if the class is a {@link Queue}.
	 *
	 * @return a predicate that verifies if the class is a {@link Queue}
	 */
	public static Predicate<Type> isParameterizedType() {
		return isA(ParameterizedType.class);
	}

	/**
	 * Returns a rawType predicate based on another predicate.
	 *
	 * @param rawTypePredicate given predicate
	 * @return a rawType predicate based on another predicate
	 */
	public static Predicate<Type> rawType(final Predicate<Type> rawTypePredicate) {
		return type -> rawTypePredicate.test(((ParameterizedType) type).getRawType());
	}

}
