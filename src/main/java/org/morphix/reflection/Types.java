package org.morphix.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Methods for manipulating {@link Type}s.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Types {

	/**
	 * Returns an array class based on a class.
	 *
	 * @param type class
	 * @return an array class based on a class
	 */
	static <T> Class<T> getArrayClass(final Type type) {
		String className = getName(type);
		if (className.startsWith("[")) {
			className = "[" + className;
		} else {
			className = "[L" + className + ";";
		}
		return Reflection.getClass(className);
	}

	/**
	 * Returns the name of a type similar to {@link Class#getName()} to be used
	 * in {@link Class#forName(String)}.
	 *
	 * @param type type to get the name from
	 * @return the name of a type
	 */
	static String getName(final Type type) {
		String typeName = type.getTypeName();

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
	 * Returns an array class based on a parameterized type, null if the
	 * parameterized type raw type is not a class.
	 *
	 * @param parameterizedType parameterized type
	 * @return an array class based on a parameterized type
	 */
	static <T> Class<T> getArrayClass(final ParameterizedType parameterizedType) {
		return getArrayClass(parameterizedType.getRawType());
	}

}
