package org.morphix.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class can be used to extract the generic type information for a type at
 * runtime.
 *
 * @param <T> actual class
 *
 * @author Radu Sebastian LAZIN
 */
@SuppressWarnings("unused")
public abstract class ParameterizedClass<T> {

	private final Type genericArgumentType;

	protected ParameterizedClass() {
		this.genericArgumentType = getGenericParameterType(getClass(), 0);
	}

	public Type getGenericArgumentType() {
		return genericArgumentType;
	}

	/**
	 * Extract the generic parameter type for a given class.
	 *
	 * @param cls class to extract from
	 * @param index type parameter index
	 * @return the generic parameter type for a given class
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Type> T getGenericParameterType(final Class<?> cls, final int index) {
		if (!isGenericClass(cls)) {
			throw new ReflectionException(cls.getCanonicalName() + " is not a generic class");
		}
		ParameterizedType parameterizedType = (ParameterizedType) cls.getGenericSuperclass();
		Type genericType = parameterizedType.getActualTypeArguments()[index];
		return (T) genericType;
	}

	/**
	 * Returns true if the given class is generic, false otherwise.
	 *
	 * @param cls class to check
	 * @return true if the given class is generic, false otherwise.
	 */
	public static boolean isGenericClass(final Class<?> cls) {
		Type type = cls.getGenericSuperclass();
		return type instanceof ParameterizedType;
	}

}
