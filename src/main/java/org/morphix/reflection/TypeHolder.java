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
package org.morphix.reflection;

import java.lang.reflect.Type;

import org.morphix.lang.JavaObjects;

/**
 * Holds a reflected type which is either a {@link Class} or a captured generic type represented by a
 * {@link GenericClass}.
 * <p>
 * Instances are obtained through the {@link #of(Class)} and {@link #of(GenericClass)} factory methods and expose the
 * underlying {@link Type}, its name, and whether the holder has a class type, a generic type, or neither.
 * <p>
 * Instances built from a {@link Class} are cached, one instance per class (see {@link ClassValue}), while instances
 * built from a {@link GenericClass} are created on every call. When no type at all is held, {@link #getTypeName()}
 * returns {@link #UNKNOWN_TYPE}.
 * <p>
 * The shared {@link #NONE} instance has no type set and can be used instead of {@code null}.
 *
 * @author Radu Sebastian LAZIN
 */
public final class TypeHolder {

	/**
	 * Type name returned when the holder has no underlying type.
	 */
	public static final String UNKNOWN_TYPE = "<unknown-type>";

	/**
	 * Type holder that has no underlying type, returned by {@link #of(Class)} and {@link #safe(TypeHolder)} when the type
	 * given as parameter is {@code null}.
	 */
	public static final TypeHolder NONE = new TypeHolder(null, null);

	/**
	 * Cache of holders built from classes. Since a holder is immutable and carries only the described class, a single
	 * instance per class is enough. A {@link ClassValue} is used so that the cache does not prevent class unloading.
	 */
	private static final ClassValue<TypeHolder> CLASS_TYPES = new ClassValue<>() {

		/**
		 * @see ClassValue#computeValue(Class)
		 */
		@Override
		protected TypeHolder computeValue(final Class<?> type) {
			return new TypeHolder(type, null);
		}
	};

	/**
	 * The described class type, {@code null} if this holder was built from a {@link GenericClass}.
	 */
	private final Class<?> classType;

	/**
	 * The described generic type, {@code null} if this holder was built from a {@link Class}.
	 */
	private final GenericClass<?> genericType;

	/**
	 * Private constructor.
	 *
	 * @param classType the described class type, may be {@code null}
	 * @param genericType the described generic type, may be {@code null}
	 */
	private TypeHolder(final Class<?> classType, final GenericClass<?> genericType) {
		this.classType = classType;
		this.genericType = genericType;
	}

	/**
	 * Returns the holder for the given class. Holders built from classes are cached, so calling this method repeatedly with
	 * the same class returns the same instance.
	 *
	 * @param type the class to describe, {@link #NONE} is returned if {@code null}
	 * @return the holder for the given class, or {@link #NONE} if the class is {@code null}
	 */
	public static TypeHolder of(final Class<?> type) {
		return null != type ? CLASS_TYPES.get(type) : NONE;
	}

	/**
	 * Returns a holder for the given captured generic type. Unlike {@link #of(Class)}, the result is not cached and a new
	 * instance is returned on every call.
	 *
	 * @param type the generic type to describe, may be {@code null}
	 * @return a holder for the given generic type, which has no type set if the parameter is {@code null}
	 */
	public static TypeHolder of(final GenericClass<?> type) {
		return new TypeHolder(null, type);
	}

	/**
	 * Returns the type holder given as parameter or {@link #NONE} if the parameter is {@code null}.
	 *
	 * @param typeHolder a type holder
	 * @return the type holder given as parameter or {@link #NONE} if the parameter is {@code null}
	 */
	public static TypeHolder safe(final TypeHolder typeHolder) {
		return null != typeHolder ? typeHolder : NONE;
	}

	/**
	 * Returns the described class type.
	 *
	 * @param <T> the class type
	 *
	 * @return the described class type, or {@code null} if this holder does not hold a class type
	 */
	public <T> Class<T> getClassType() {
		return JavaObjects.cast(classType);
	}

	/**
	 * Returns the described generic type.
	 *
	 * @param <T> the generic type
	 *
	 * @return the described generic type, or {@code null} if this holder does not hold a generic type
	 */
	public <T> GenericClass<T> getGenericType() {
		return JavaObjects.cast(genericType);
	}

	/**
	 * Returns true if this holder holds a class type.
	 *
	 * @return true if this holder holds a class type
	 */
	public boolean hasClassType() {
		return null != classType;
	}

	/**
	 * Returns true if this holder holds a generic type.
	 *
	 * @return true if this holder holds a generic type
	 */
	public boolean hasGenericType() {
		return null != genericType;
	}

	/**
	 * Returns true if this holder holds a class type or a generic type.
	 *
	 * @return true if this holder holds a type
	 */
	public boolean hasType() {
		return hasClassType() || hasGenericType();
	}

	/**
	 * Returns the described type, preferring the generic type when present.
	 *
	 * @return the described type, or {@code null} if this holder holds no type
	 */
	public Type getType() {
		return hasGenericType() ? genericType.getType() : classType;
	}

	/**
	 * Returns the name of the described type.
	 *
	 * @return the name of the described type, or {@link #UNKNOWN_TYPE} if this holder holds no type
	 */
	public String getTypeName() {
		Type type = getType();
		return null != type ? type.getTypeName() : UNKNOWN_TYPE;
	}
}
