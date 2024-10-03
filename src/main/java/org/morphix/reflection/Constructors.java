package org.morphix.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Utility class for Java constructors
 *
 * @author Radu Sebastian LAZIN
 */
public interface Constructors {

	/**
	 * Creates a new instance for the given type with its default constructor.
	 *
	 * @param cls class for which to create an instance
	 * @return an object of type T
	 */
	static <T> T newInstance(final Class<T> cls) {
		try {
			return cls.getDeclaredConstructor().newInstance();
		} catch (NoSuchMethodException e) {
			throw new ReflectionException("Default constructor is not defined for class: " + cls.getCanonicalName(), e);
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
	 * Creates a new instance for the given type with its default constructor
	 * ignoring its visibility.
	 *
	 * @param cls class for which to create an instance
	 * @return an object of type T
	 */
	static <T> T newInstanceIgnoreAccess(final Class<T> cls) {
		Constructor<T> constructor;
		try {
			constructor = cls.getDeclaredConstructor();
		} catch (NoSuchMethodException e) {
			throw new ReflectionException("No default constructor found for class: " + cls.getCanonicalName(), e);
		}
		return newInstanceIgnoreAccess(constructor);
	}

	/**
	 * Returns a constructor for the given type with the constructor that matches the
	 * given parameter types.
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
					+ " with parameters: " + List.of(paramTypes), e);
		}
	}

	/**
	 * Creates a new instance for the given type with a given constructor
	 * ignoring its visibility.
	 *
	 * @param constructor with which to create an instance
	 * @param args constructor arguments
	 * @return an object of type T
	 */
	static <T> T newInstanceIgnoreAccess(final Constructor<T> constructor, final Object... args) {
		try (ConstructorAccessor<T> constructorAccessor = new ConstructorAccessor<>(constructor)) {
			return constructor.newInstance(args);
		} catch (NullPointerException | InvocationTargetException
				| IllegalAccessException | IllegalAccessError
				| InstantiationException | InstantiationError e) {
			throw new ReflectionException("Could not instantiate class: " + constructor.getDeclaringClass().getCanonicalName(), e);
		}
	}

	/**
	 * Creates a new instance. It uses instance creator if it can't create an
	 * instance normally.
	 *
	 * @param cls class to create an instance for
	 * @return new instance
	 */
	static <T> T newInstance(final Class<T> cls, final InstanceCreator instanceCreator) {
		try {
			return Constructors.newInstanceIgnoreAccess(cls);
		} catch (ReflectionException e) {
			return instanceCreator.newInstance(cls);
		}
	}

}
