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

import java.lang.reflect.Field;

import sun.misc.Unsafe; // NOSONAR used for low-level operations when no other option is available

/**
 * {@link Unsafe} wrapper class.
 * <p>
 * Advanced API: depends on JDK internals and strong encapsulation may block access on some JDK/module configurations.
 * <p>
 * TODO: expand JavaDoc with module-system guidance and supported JDK expectations.
 *
 * @author Radu Sebastian LAZIN
 */
public class TheUnsafe {

	/**
	 * The name of the field containing the singleton instance of {@link Unsafe}.
	 */
	static final String THE_UNSAFE_FIELD_NAME = "theUnsafe";

	/**
	 * The singleton instance of {@link Unsafe}.
	 */
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
	 * Sets the boolean value at the given offset.
	 *
	 * @param obj object containing the field
	 * @param offset field offset
	 * @param value value to set
	 */
	public static void putBoolean(final Object obj, final long offset, final boolean value) {
		UNSAFE.putBoolean(obj, offset, value);
	}

	/**
	 * Sets the byte value at the given offset.
	 *
	 * @param obj object containing the field
	 * @param offset field offset
	 * @param value value to set
	 */
	public static void putByte(final Object obj, final long offset, final byte value) {
		UNSAFE.putByte(obj, offset, value);
	}

	/**
	 * Sets the char value at the given offset.
	 *
	 * @param obj object containing the field
	 * @param offset field offset
	 * @param value value to set
	 */
	public static void putChar(final Object obj, final long offset, final char value) {
		UNSAFE.putChar(obj, offset, value);
	}

	/**
	 * Sets the short value at the given offset.
	 *
	 * @param obj object containing the field
	 * @param offset field offset
	 * @param value value to set
	 */
	public static void putShort(final Object obj, final long offset, final short value) {
		UNSAFE.putShort(obj, offset, value);
	}

	/**
	 * Sets the int value at the given offset.
	 *
	 * @param obj object containing the field
	 * @param offset field offset
	 * @param value value to set
	 */
	public static void putInt(final Object obj, final long offset, final int value) {
		UNSAFE.putInt(obj, offset, value);
	}

	/**
	 * Sets the long value at the given offset.
	 *
	 * @param obj object containing the field
	 * @param offset field offset
	 * @param value value to set
	 */
	public static void putLong(final Object obj, final long offset, final long value) {
		UNSAFE.putLong(obj, offset, value);
	}

	/**
	 * Sets the float value at the given offset.
	 *
	 * @param obj object containing the field
	 * @param offset field offset
	 * @param value value to set
	 */
	public static void putFloat(final Object obj, final long offset, final float value) {
		UNSAFE.putFloat(obj, offset, value);
	}

	/**
	 * Sets the double value at the given offset.
	 *
	 * @param obj object containing the field
	 * @param offset field offset
	 * @param value value to set
	 */
	public static void putDouble(final Object obj, final long offset, final double value) {
		UNSAFE.putDouble(obj, offset, value);
	}

	/**
	 * Writes a primitive value at the given offset. Accepted source types match
	 * {@link java.lang.reflect.Field#set(Object, Object)}.
	 *
	 * @param obj object containing the field, or the static field base
	 * @param offset field offset
	 * @param type primitive field type
	 * @param value value to set
	 * @throws IllegalArgumentException if {@code value} cannot be stored in a field of {@code type}
	 */
	public static void putPrimitive(final Object obj, final long offset, final Class<?> type, final Object value) {
		if (boolean.class == type && value instanceof Boolean booleanValue) {
			putBoolean(obj, offset, booleanValue);
		} else if (byte.class == type && value instanceof Byte byteValue) {
			putByte(obj, offset, byteValue);
		} else if (char.class == type && value instanceof Character charValue) {
			putChar(obj, offset, charValue);
		} else if (short.class == type && (value instanceof Byte || value instanceof Short)) {
			putShort(obj, offset, ((Number) value).shortValue());
		} else if (int.class == type && isIntCompatible(value)) {
			putInt(obj, offset, toInt(value));
		} else if (long.class == type && isLongCompatible(value)) {
			putLong(obj, offset, toLong(value));
		} else if (float.class == type && isFloatCompatible(value)) {
			putFloat(obj, offset, toFloat(value));
		} else if (double.class == type && isDoubleCompatible(value)) {
			putDouble(obj, offset, toDouble(value));
		} else {
			throw incompatibleValue(type, value);
		}
	}

	/**
	 * Returns true when {@code value} can be widened to {@code int} the same way
	 * {@link java.lang.reflect.Field#set(Object, Object)} does.
	 *
	 * @param value value to test
	 * @return true when the value can be stored in an {@code int} field
	 */
	private static boolean isIntCompatible(final Object value) {
		return value instanceof Byte || value instanceof Short || value instanceof Character || value instanceof Integer;
	}

	/**
	 * Returns true when {@code value} can be widened to {@code long} the same way
	 * {@link java.lang.reflect.Field#set(Object, Object)} does.
	 *
	 * @param value value to test
	 * @return true when the value can be stored in a {@code long} field
	 */
	private static boolean isLongCompatible(final Object value) {
		return isIntCompatible(value) || value instanceof Long;
	}

	/**
	 * Returns true when {@code value} can be widened to {@code float} the same way
	 * {@link java.lang.reflect.Field#set(Object, Object)} does.
	 *
	 * @param value value to test
	 * @return true when the value can be stored in a {@code float} field
	 */
	private static boolean isFloatCompatible(final Object value) {
		return isLongCompatible(value) || value instanceof Float;
	}

	/**
	 * Returns true when {@code value} can be widened to {@code double} the same way
	 * {@link java.lang.reflect.Field#set(Object, Object)} does.
	 *
	 * @param value value to test
	 * @return true when the value can be stored in a {@code double} field
	 */
	private static boolean isDoubleCompatible(final Object value) {
		return isFloatCompatible(value) || value instanceof Double;
	}

	/**
	 * Converts a value accepted for an {@code int} field.
	 *
	 * @param value value to convert
	 * @return int value
	 */
	private static int toInt(final Object value) {
		return value instanceof Character character ? character : ((Number) value).intValue();
	}

	/**
	 * Converts a value accepted for a {@code long} field.
	 *
	 * @param value value to convert
	 * @return long value
	 */
	private static long toLong(final Object value) {
		return value instanceof Character character ? character : ((Number) value).longValue();
	}

	/**
	 * Converts a value accepted for a {@code float} field.
	 *
	 * @param value value to convert
	 * @return float value
	 */
	private static float toFloat(final Object value) {
		return value instanceof Character character ? character : ((Number) value).floatValue();
	}

	/**
	 * Converts a value accepted for a {@code double} field.
	 *
	 * @param value value to convert
	 * @return double value
	 */
	private static double toDouble(final Object value) {
		return value instanceof Character character ? character : ((Number) value).doubleValue();
	}

	/**
	 * Returns the exception thrown when a value cannot be stored in a field of the given type.
	 *
	 * @param type field type
	 * @param value rejected value
	 * @return type mismatch exception
	 */
	static IllegalArgumentException incompatibleValue(final Class<?> type, final Object value) {
		String actual = null == value ? "null" : value.getClass().getName();
		return new IllegalArgumentException("Can not set " + type.getName() + " field to " + actual);
	}

	/**
	 * Hide constructor.
	 */
	private TheUnsafe() {
		throw Constructors.unsupportedOperationException();
	}
}
