/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.morphix.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import org.morphix.lang.JavaObjects;

/**
 * Implementation of {@link ParameterizedType}.
 *
 * @author Radu Sebastian LAZIN
 */
public class GenericType implements ParameterizedType {

	private final Class<?> rawType;

	private final Type[] arguments;

	private final Type ownerType;

	/**
	 * Private constructor with all arguments.
	 *
	 * @param rawType raw type
	 * @param arguments generic type arguments
	 * @param ownerType owner type
	 */
	private GenericType(final Class<?> rawType, final Type[] arguments, final Type ownerType) {
		this.rawType = rawType;
		this.arguments = arguments;
		this.ownerType = ownerType;
	}

	/**
	 * Builds a new {@link GenericType} object with the given arguments.
	 *
	 * @param rawType raw type
	 * @param actualTypeArguments actual generic type arguments
	 * @param ownerType owner type
	 * @return a new parameterized type object
	 */
	public static GenericType of(final Class<?> rawType, final Type[] actualTypeArguments, final Type ownerType) {
		return new GenericType(rawType, actualTypeArguments, ownerType);
	}

	/**
	 * Builds a new {@link GenericType} object from a {@link ParameterizedType} object.
	 *
	 * @param parameterizedType parameterized type object
	 * @return a new parameterized type object
	 */
	public static GenericType of(final ParameterizedType parameterizedType) {
		return of(
				(Class<?>) parameterizedType.getRawType(),
				Objects.requireNonNull(parameterizedType.getActualTypeArguments(), "actual type arguments"),
				parameterizedType.getOwnerType()
		);
	}

	/**
	 * Builds a new {@link GenericType} object based on the generic argument of a generic class object.
	 * If the generic argument type is not a parameterized type then an {@link ReflectionException}
	 * is thrown.
	 *
	 * @param <T> generic argument type
	 *
	 * @param genericClass generic class object
	 * @return a new parameterized type object
	 */
	public static <T> GenericType of(final GenericClass<T> genericClass) {
		Type genericArgumentType = genericClass.getType();
		if (genericArgumentType instanceof ParameterizedType parameterizedType) {
			return of(parameterizedType);
		}
		throw new ReflectionException("Cannot build GenericType from " + genericArgumentType +
				" because it is not a " + ParameterizedType.class);
	}

	/**
	 * Extract the generic parameter type for a given class.
	 *
	 * @param <T> generic parameter type
	 *
	 * @param cls class to extract from
	 * @param index type parameter index
	 * @return the generic parameter type for a given class
	 */
	public static <T extends Type> T getGenericParameterType(final Class<?> cls, final int index) {
		if (index < 0) {
			throw new ReflectionException("index cannot be negative.");
		}
		if (!isGenericClass(cls)) {
			throw new ReflectionException(cls.getCanonicalName() + " is not a generic class");
		}
		ParameterizedType parameterizedType = (ParameterizedType) cls.getGenericSuperclass();
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		if (index >= actualTypeArguments.length) {
			throw new ReflectionException(cls.getCanonicalName() + " class has only " + actualTypeArguments.length
					+ " generic arguments. Index: " + index + " is out of bounds.");
		}
		Type genericType = actualTypeArguments[index];
		return JavaObjects.cast(genericType);
	}

	/**
	 * Returns the generic argument class from a class with a generic type. It uses a trick
	 * where the JRE retains the generic type information for method return types. The method
	 * will try to find a getter for the field specified and get the return type that keeps
	 * the generic information.
	 * <p>
	 * Example: for <code>List&lt;String&gt;</code> the method with index 0 will return
	 * <code>Class&lt;String&gt;</code>
	 *
	 * @param <T> generic argument type
	 * @param <U> type to get the generic type from
	 *
	 * @param field field
	 * @param cls class
	 * @param index index of the generic argument
	 * @return class of the argument, null otherwise
	 */
	public static <T extends Type, U> T getGenericArgumentType(final Field field, final Class<U> cls, final int index) {
		Method getterMethod;
		try {
			String getterMethodName = MethodType.GETTER.getMethodName(field);
			getterMethod = Methods.getDeclaredMethodInHierarchy(getterMethodName, cls);
		} catch (NoSuchMethodException e) {
			return null;
		}
		return Methods.getSafeGenericReturnType(getterMethod, index);
	}

	/**
	 * Returns true if the given class is generic, false otherwise.
	 *
	 * @param <T> type to check
	 *
	 * @param cls class to check
	 * @return true if the given class is generic, false otherwise.
	 */
	public static <T> boolean isGenericClass(final Class<T> cls) {
		Type type = cls.getGenericSuperclass();
		return type instanceof ParameterizedType;
	}

	/**
	 * @see #getActualTypeArguments()
	 */
	@Override
	public Type[] getActualTypeArguments() {
		return arguments;
	}

	/**
	 * @see #getRawType()
	 */
	@Override
	public Type getRawType() {
		return rawType;
	}

	/**
	 * @see #getOwnerType()
	 */
	@Override
	public Type getOwnerType() {
		return ownerType;
	}

	/**
	 * @see #equals(Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof GenericType that) {
			return Objects.equals(that.rawType, this.rawType)
					&& Arrays.equals(that.arguments, this.arguments)
					&& Objects.equals(that.ownerType, this.ownerType);
		}
		return false;
	}

	/**
	 * @see #hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(arguments) ^
	            Objects.hashCode(ownerType) ^
	            Objects.hashCode(rawType);
	}

	/**
	 * @see #hashCode()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (ownerType != null) {
			sb.append(ownerType.getTypeName());
			sb.append("$");
			if (ownerType instanceof ParameterizedType ot) {
				// Find simple name of the nested type by removing the shared prefix with an owner.
				sb.append(rawType.getName().replace(((Class<?>) ot.getRawType()).getName() + "$", ""));
			} else {
				sb.append(rawType.getSimpleName());
			}
		} else {
			sb.append(rawType.getName());
		}

		StringJoiner sj = new StringJoiner(", ", "<", ">");
		sj.setEmptyValue("");
		for (Type t : arguments) {
			sj.add(t.getTypeName());
		}
		sb.append(sj);

		return sb.toString();
	}
}
