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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.morphix.lang.JavaObjects;

/**
 * Utility class to transform primitives to their respective boxed class back and forth.
 *
 * @author Radu Sebastian LAZIN
 */
public class Primitives {

	private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_CLASS_MAP = new HashMap<>();
	static {
		PRIMITIVE_TO_CLASS_MAP.put(int.class, Integer.class);
		PRIMITIVE_TO_CLASS_MAP.put(long.class, Long.class);
		PRIMITIVE_TO_CLASS_MAP.put(char.class, Character.class);
		PRIMITIVE_TO_CLASS_MAP.put(short.class, Short.class);
		PRIMITIVE_TO_CLASS_MAP.put(byte.class, Byte.class);
		PRIMITIVE_TO_CLASS_MAP.put(boolean.class, Boolean.class);
		PRIMITIVE_TO_CLASS_MAP.put(float.class, Float.class);
		PRIMITIVE_TO_CLASS_MAP.put(double.class, Double.class);
	}

	private static final Map<Class<?>, Class<?>> CLASS_TO_PRIMITIVE_MAP = PRIMITIVE_TO_CLASS_MAP.keySet().stream()
			.collect(Collectors.toMap(PRIMITIVE_TO_CLASS_MAP::get, Function.identity()));

	/**
	 * Returns the primitive type from the boxed class.
	 *
	 * @param <T> primitive type
	 *
	 * @param cls source class
	 * @return primitive type
	 */
	public static <T> Class<T> toPrimitive(final Class<?> cls) {
		if (!isUnboxable(cls)) {
			throw new ReflectionException("Class " + cls.getCanonicalName() + " cannot be transformed to primitive type");
		}
		return JavaObjects.cast(CLASS_TO_PRIMITIVE_MAP.get(cls));
	}

	/**
	 * Returns the boxed type from a primitive.
	 *
	 * @param <T> boxed type
	 *
	 * @param cls primitive class
	 * @return the boxed class
	 */
	public static <T> Class<T> fromPrimitive(final Class<?> cls) {
		if (!PRIMITIVE_TO_CLASS_MAP.containsKey(cls)) {
			throw new ReflectionException("Class " + cls.getCanonicalName() + " cannot be transformed to boxed type");
		}
		return JavaObjects.cast(PRIMITIVE_TO_CLASS_MAP.get(cls));
	}

	/**
	 * Returns the boxed class for a given primitive class.
	 *
	 * @param clsPrimitive primitive class
	 * @return the boxed class for a given primitive class
	 */
	public static Class<?> getBoxedClass(final Class<?> clsPrimitive) {
		return PRIMITIVE_TO_CLASS_MAP.get(clsPrimitive);
	}

	/**
	 * Returns true if the given class can be unboxed to its primitive counterpart.
	 *
	 * @param clsBoxed boxed class
	 * @return true if the given class can be unboxed to its primitive counterpart
	 */
	public static boolean isUnboxable(final Class<?> clsBoxed) {
		return CLASS_TO_PRIMITIVE_MAP.containsKey(clsBoxed);
	}

	/**
	 * Private constructor that throws exception if called.
	 */
	private Primitives() {
		throw Constructors.unsupportedOperationException();
	}

}
