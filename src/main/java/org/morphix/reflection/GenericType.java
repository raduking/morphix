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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import org.morphix.lang.JavaObjects;

/**
 * Implementation of {@link ParameterizedType}. This class can be used to create parameterized types programmatically.
 *
 * <p>
 * <b>Usage example:</b>
 * <p>
 * To create a parameterized type for <code>Map&lt;String, List&lt;Integer&gt;&gt;</code>:
 *
 * <pre>
 * GenericType mapType = GenericType.of(Map.class, GenericType.Arguments.of(String.class, GenericType.of(List.class, Integer.class)));
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
public class GenericType implements ParameterizedType {

	/**
	 * The raw type without generics.
	 */
	private final Class<?> rawType;

	/**
	 * Generic type arguments.
	 */
	private final Type[] arguments;

	/**
	 * Owner type.
	 */
	private final Type ownerType;

	/**
	 * Helper class to build type arguments arrays.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public interface Arguments {

		/**
		 * Builds a new array of {@link Type} from the given arguments.
		 *
		 * @param types types
		 * @return array of types
		 */
		static Type[] of(final Type... types) {
			return types;
		}
	}

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
	 * <p>
	 * For the actual type arguments use {@link Arguments#of(Type...)} to build the array.
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
	 * Builds a new {@link GenericType} object with the given argument.
	 *
	 * @param rawType raw type
	 * @param actualTypeArgument actual generic type argument
	 * @param ownerType owner type
	 * @return a new parameterized type object
	 */
	public static GenericType of(final Class<?> rawType, final Type actualTypeArgument, final Type ownerType) {
		return of(rawType, new Type[] { actualTypeArgument }, ownerType);
	}

	/**
	 * Builds a new {@link GenericType} object with the given argument. The owner type is set to null.
	 *
	 * @param rawType raw type
	 * @param actualTypeArgument actual generic type argument
	 * @return a new parameterized type object
	 */
	public static GenericType of(final Class<?> rawType, final Type actualTypeArgument) {
		return of(rawType, actualTypeArgument, null);
	}

	/**
	 * Builds a new {@link GenericType} object with the given arguments. The owner type is set to null.
	 * <p>
	 * For the actual type arguments use {@link Arguments#of(Type...)} to build the array.
	 *
	 * @param rawType raw type
	 * @param actualTypeArguments actual generic type arguments
	 * @return a new parameterized type object
	 */
	public static GenericType of(final Class<?> rawType, final Type[] actualTypeArguments) {
		return of(rawType, actualTypeArguments, null);
	}

	/**
	 * Builds a new {@link GenericType} object from a {@link ParameterizedType} object.
	 *
	 * @param parameterizedType parameterized type object
	 * @return a new parameterized type object
	 */
	public static GenericType of(final ParameterizedType parameterizedType) {
		return of(
				JavaObjects.cast(parameterizedType.getRawType()),
				Objects.requireNonNull(parameterizedType.getActualTypeArguments(), "actual type arguments"),
				parameterizedType.getOwnerType());
	}

	/**
	 * Builds a new {@link GenericType} object based on the generic argument of a generic class object. If the generic
	 * argument type is not a parameterized type then an {@link ReflectionException} is thrown.
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
		throw new ReflectionException("Cannot build GenericType from {} because it is not a {}", genericArgumentType, ParameterizedType.class);
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
		if (isNotGenericClass(cls)) {
			throw new ReflectionException("{} is not a generic class", cls.getCanonicalName());
		}
		ParameterizedType parameterizedType = (ParameterizedType) cls.getGenericSuperclass();
		return getGenericParameterType(parameterizedType, cls.getCanonicalName(), index);
	}

	/**
	 * Extract the generic parameter type for a given class.
	 *
	 * @param <T> generic parameter type
	 *
	 * @param parameterizedType parameterized type to extract from
	 * @param index type parameter index
	 * @return the generic parameter type for a given class
	 */
	public static <T extends Type> T getGenericParameterType(final ParameterizedType parameterizedType, final int index) {
		return getGenericParameterType(parameterizedType, parameterizedType.getRawType().getTypeName(), index);
	}

	/**
	 * Extract the generic parameter type for a given class.
	 *
	 * @param <T> generic parameter type
	 *
	 * @param parameterizedType parameterized type to extract from
	 * @param typeName type name for error messages
	 * @param index type parameter index
	 * @return the generic parameter type for a given class
	 */
	public static <T extends Type> T getGenericParameterType(final ParameterizedType parameterizedType, final String typeName, final int index) {
		if (index < 0) {
			throw new ReflectionException("Generic parameter type index cannot be negative, received: {}", index);
		}
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		if (index >= actualTypeArguments.length) {
			throw new ReflectionException("Cannot extract generic parameter type at index {} from {} because it has only {} generic parameter(s)",
					index, typeName, actualTypeArguments.length);
		}
		Type genericType = actualTypeArguments[index];
		return JavaObjects.cast(genericType);
	}

	/**
	 * Returns the generic argument class from a class with a generic type. It uses a trick where the JRE retains the
	 * generic type information for method return types. The method will try to find a getter for the field specified and
	 * get the return type that keeps the generic information.
	 * <p>
	 * Example: for <code>List&lt;String&gt;</code> the method with index 0 will return <code>Class&lt;String&gt;</code>
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
		String getterMethodName = MethodType.GETTER.getMethodName(field);
		Method getterMethod = Methods.getOneDeclaredInHierarchy(getterMethodName, cls);
		if (null == getterMethod) {
			return null;
		}
		return Methods.Safe.getGenericReturnType(getterMethod, index);
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
	 * Returns true if the given class is not generic, false otherwise.
	 *
	 * @param <T> type to check
	 *
	 * @param cls class to check
	 * @return true if the given class is not generic, false otherwise.
	 */
	public static <T> boolean isNotGenericClass(final Class<T> cls) {
		return !isGenericClass(cls);
	}

	/**
	 * @see ParameterizedType#getActualTypeArguments()
	 */
	@Override
	public Type[] getActualTypeArguments() {
		return arguments;
	}

	/**
	 * @see ParameterizedType#getRawType()
	 */
	@Override
	public Type getRawType() {
		return rawType;
	}

	/**
	 * @see ParameterizedType#getOwnerType()
	 */
	@Override
	public Type getOwnerType() {
		return ownerType;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof ParameterizedType that) {
			return Objects.equals(that.getRawType(), this.rawType)
					&& Arrays.equals(that.getActualTypeArguments(), this.arguments)
					&& Objects.equals(that.getOwnerType(), this.ownerType);
		}
		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(arguments) ^
				Objects.hashCode(ownerType) ^
				Objects.hashCode(rawType);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (ownerType != null) {
			sb.append(ownerType.getTypeName());
			sb.append("$");
			if (ownerType instanceof ParameterizedType ot) {
				// Find simple name of the nested type by removing the shared prefix with an owner.
				sb.append(rawType.getName().replace(ot.getRawType().getTypeName() + "$", ""));
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
