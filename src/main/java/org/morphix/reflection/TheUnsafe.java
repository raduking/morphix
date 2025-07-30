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

import sun.misc.Unsafe; // NOSONAR

/**
 * {@link Unsafe} wrapper class.
 *
 * @author Radu Sebastian LAZIN
 */
public class TheUnsafe {

	static final String THE_UNSAFE_FIELD_NAME = "theUnsafe";

	static final Unsafe UNSAFE = getUnsafe();

	/**
	 * Returns the Java {@link Unsafe}.
	 *
	 * @return the unsafe object
	 */
	public static Unsafe getUnsafe() {
		return getStaticFromUnsafe(THE_UNSAFE_FIELD_NAME);
	}

	/**
	 * Returns a static field from the {@link Unsafe} class.
	 *
	 * @param <T> return type
	 *
	 * @param fieldName field name
	 * @return a static field
	 */
	static <T> T getStaticFromUnsafe(final String fieldName) {
		Field theUnsafeField = Fields.getOneDeclaredInHierarchy(Unsafe.class, fieldName);
		if (null == theUnsafeField) {
			return null;
		}
		try (MemberAccessor<Field> ignored = new MemberAccessor<>(null, theUnsafeField)) {
			return Fields.get(null, theUnsafeField);
		}
	}

	/**
	 * Calls and returns the value of {@link Unsafe#staticFieldOffset(Field)}.
	 *
	 * @param field static field to get the offset for
	 * @return field offset
	 */
	@SuppressWarnings("deprecation")
	public static long staticFieldOffset(final Field field) {
		return UNSAFE.staticFieldOffset(field);
	}

	/**
	 * Calls and returns the value of {@link Unsafe#objectFieldOffset(Field)}.
	 *
	 * @param field field to get the offset for
	 * @return field offset
	 */
	@SuppressWarnings("deprecation")
	public static long objectFieldOffset(final Field field) {
		return UNSAFE.objectFieldOffset(field);
	}

	/**
	 * Calls and returns the value of {@link Unsafe#staticFieldBase(Field)}.
	 *
	 * @param field static field to get the base object for
	 * @return class object the static field is declared in
	 */
	@SuppressWarnings("deprecation")
	public static Object staticFieldBase(final Field field) {
		return UNSAFE.staticFieldBase(field);
	}

	/**
	 * Sets the field value for the given object. The field is determined by its offset.
	 *
	 * @param obj object containing the field
	 * @param offset field offset
	 * @param value value to set
	 */
	public static void putObject(final Object obj, final long offset, final Object value) {
		UNSAFE.putObject(obj, offset, value);
	}

	/**
	 * Hide constructor.
	 */
	private TheUnsafe() {
		throw Constructors.unsupportedOperationException();
	}
}