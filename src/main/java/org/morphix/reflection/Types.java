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
 * Methods for manipulating {@link Type}s.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Types {

	/**
	 * Returns an array class based on a class.
	 *
	 * @param <T> array type
	 *
	 * @param type the type to create an array from, cannot be null
	 * @return an array class based on a class
	 */
	static <T> Class<T> getArrayClass(final Type type) {
		String className = getName(type);
		if (className.startsWith("[")) {
			className = "[" + className;
		} else {
			className = "[L" + className + ";";
		}
		return Classes.Safe.getOne(className);
	}

	/**
	 * Returns the name of a type similar to {@link Class#getName()} to be used in {@link Class#forName(String)}.
	 *
	 * @param type type to get the name from, cannot be null
	 * @return the name of a type
	 */
	static String getName(final Type type) {
		String typeName = Objects.requireNonNull(type).getTypeName();

		String tmpTypeName = typeName.replace("]", "");
		int arrayCount = typeName.length() - tmpTypeName.length();
		typeName = tmpTypeName;
		while (typeName.endsWith("[")) {
			typeName = typeName.substring(0, typeName.length() - 1);
		}
		StringBuilder sb = new StringBuilder();
		if (arrayCount > 0) {
			sb.append("[".repeat(arrayCount));
			sb.append('L').append(typeName).append(';');
		} else {
			sb.append(typeName);
		}
		return sb.toString();
	}

	/**
	 * Returns an array class based on a parameterized type, null if the parameterized type raw type is not a class.
	 *
	 * @param <T> array type
	 *
	 * @param parameterizedType parameterized type
	 * @return an array class based on a parameterized type
	 */
	static <T> Class<T> getArrayClass(final ParameterizedType parameterizedType) {
		return getArrayClass(parameterizedType.getRawType());
	}

}
