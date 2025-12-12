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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * The purpose of this class is to enable capturing and passing a generic type. This is achieved by creating an inline
 * subclass of this class. The Java runtime retains in this case the actual generic type.
 *
 * <pre>
 * GenericClass&lt;Map&lt;String, String&gt;&gt; typeRef = new GenericClass&lt;&gt;() {
 * };
 * </pre>
 *
 * and the captured generic type will be: <code>Map&lt;String,String&gt;</code>
 *
 * @param <T> generic type
 *
 * @author Radu Sebastian LAZIN
 */
@SuppressWarnings("unused")
public abstract class GenericClass<T> {

	/**
	 * Captured generic type.
	 */
	private Type type;

	/**
	 * Default protected constructor.
	 */
	protected GenericClass() {
		Class<?> genericClassSubclass = Classes.findSubclass(GenericClass.class, getClass());
		setType(GenericType.getGenericParameterType(genericClassSubclass, 0));
	}

	/**
	 * Private constructor with type.
	 *
	 * @param type captured type
	 */
	private GenericClass(final Type type) {
		setType(type);
	}

	/**
	 * Build a {@code GenericClass} wrapping the given type.
	 *
	 * @param <T> generic type
	 *
	 * @param type a generic type
	 * @return generic type reference object representing the type
	 */
	public static <T> GenericClass<T> of(final Type type) {
		return new GenericClass<>(type) {
			// empty
		};
	}

	/**
	 * Build a {@code GenericClass} without a type.
	 *
	 * @param <T> generic type
	 * @return generic type reference object without a type
	 */
	public static <T> GenericClass<T> of() {
		return of(null);
	}

	/**
	 * Sets the type.
	 *
	 * @param type type to set
	 */
	public void setType(final Type type) {
		if (null != type && !(type instanceof ParameterizedType)) {
			throw new ReflectionException("Generic argument type must be a generic class (ParameterizedType), but got: " + type);
		}
		this.type = type;
	}

	/**
	 * Alias for {@link #setType(Type)}.
	 *
	 * @param type type to set
	 */
	public void setGenericArgumentType(final Type type) {
		setType(type);
	}

	/**
	 * Returns the captured type.
	 *
	 * @return the captured type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Alias for {@link #getType()}.
	 *
	 * @return the captured generic argument type
	 */
	public Type getGenericArgumentType() {
		return getType();
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof GenericClass<?> that && Objects.equals(type, that.type);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(type);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		String typeString = null == type ? "T" : type.toString();
		return "GenericClass<" + typeString + ">";
	}

}
