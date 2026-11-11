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

/**
 * Conversions accepted by {@link java.lang.reflect.Field#set(Object, Object)}.
 * <p>
 * A primitive target accepts its wrapper and the widening primitive conversions: {@code byte} to {@code short},
 * {@code int}, {@code long}, {@code float}, or {@code double}; {@code short} or {@code char} to {@code int},
 * {@code long}, {@code float}, or {@code double}; {@code int} to {@code long}, {@code float}, or {@code double};
 * {@code long} to {@code float} or {@code double}; {@code float} to {@code double}. {@code boolean} accepts only
 * {@code Boolean}. Narrowing, and {@code byte} or {@code short} to {@code char}, are rejected.
 * <p>
 * {@link Classes#isAssignableFrom(Class, Class)} treats a primitive and its wrapper as the same type and does not apply
 * these widening conversions.
 * <p>
 * The {@code toBoolean}, {@code toByte}, and other primitive methods unbox in place and do not allocate.
 *
 * @author Radu Sebastian LAZIN
 */
public class TypeCoercion {

	/**
	 * Returns true when {@code value} can be stored in a field of type {@code target}. A {@code null} value is accepted for
	 * a reference target and rejected for a primitive target. A {@code null} target returns false.
	 *
	 * @param target field type
	 * @param value value to store
	 * @return true when {@link java.lang.reflect.Field#set(Object, Object)} would accept the value
	 */
	public static boolean isApplicable(final Class<?> target, final Object value) {
		if (null == target) {
			return false;
		}
		if (!target.isPrimitive()) {
			return null == value || target.isInstance(value);
		}
		if (boolean.class == target) {
			return value instanceof Boolean;
		}
		if (byte.class == target) {
			return value instanceof Byte;
		}
		if (char.class == target) {
			return value instanceof Character;
		}
		if (short.class == target) {
			return value instanceof Byte || value instanceof Short;
		}
		if (int.class == target) {
			return isInt(value);
		}
		if (long.class == target) {
			return isLong(value);
		}
		if (float.class == target) {
			return isFloat(value);
		}
		if (double.class == target) {
			return isDouble(value);
		}
		return false;
	}

	/**
	 * Returns true when a value of type {@code source} can be stored in a field of type {@code target}. A {@code null}
	 * source represents a null reference: it is accepted for a reference target and rejected for a primitive target. A
	 * {@code null} target returns false.
	 *
	 * @param target field type
	 * @param source value type, or {@code null} for a null reference
	 * @return true when {@link java.lang.reflect.Field#set(Object, Object)} would accept a value of {@code source}
	 */
	public static boolean isApplicable(final Class<?> target, final Class<?> source) {
		if (null == target) {
			return false;
		}
		if (!target.isPrimitive()) {
			return null == source || target.isAssignableFrom(source);
		}
		Class<?> sourcePrimitive = primitiveOf(source);
		return null != sourcePrimitive && widens(target, sourcePrimitive);
	}

	/**
	 * Returns the boolean stored in {@code value}.
	 *
	 * @param value value to unbox
	 * @return boolean value
	 * @throws IllegalArgumentException if {@code value} is not a {@link Boolean}
	 */
	public static boolean toBoolean(final Object value) {
		if (value instanceof Boolean booleanValue) {
			return booleanValue;
		}
		throw incompatible(boolean.class, value);
	}

	/**
	 * Returns the byte stored in {@code value}.
	 *
	 * @param value value to unbox
	 * @return byte value
	 * @throws IllegalArgumentException if {@code value} is not a {@link Byte}
	 */
	public static byte toByte(final Object value) {
		if (value instanceof Byte byteValue) {
			return byteValue;
		}
		throw incompatible(byte.class, value);
	}

	/**
	 * Returns the char stored in {@code value}.
	 *
	 * @param value value to unbox
	 * @return char value
	 * @throws IllegalArgumentException if {@code value} is not a {@link Character}
	 */
	public static char toChar(final Object value) {
		if (value instanceof Character charValue) {
			return charValue;
		}
		throw incompatible(char.class, value);
	}

	/**
	 * Returns {@code value} widened to a short.
	 *
	 * @param value value to convert
	 * @return short value
	 * @throws IllegalArgumentException if {@code value} cannot be widened to {@code short}
	 */
	public static short toShort(final Object value) {
		if (value instanceof Byte || value instanceof Short) {
			return ((Number) value).shortValue();
		}
		throw incompatible(short.class, value);
	}

	/**
	 * Returns {@code value} widened to an int.
	 *
	 * @param value value to convert
	 * @return int value
	 * @throws IllegalArgumentException if {@code value} cannot be widened to {@code int}
	 */
	public static int toInt(final Object value) {
		if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
			return ((Number) value).intValue();
		}
		if (value instanceof Character character) {
			return character;
		}
		throw incompatible(int.class, value);
	}

	/**
	 * Returns {@code value} widened to a long.
	 *
	 * @param value value to convert
	 * @return long value
	 * @throws IllegalArgumentException if {@code value} cannot be widened to {@code long}
	 */
	public static long toLong(final Object value) {
		if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
			return ((Number) value).longValue();
		}
		if (value instanceof Character character) {
			return character;
		}
		throw incompatible(long.class, value);
	}

	/**
	 * Returns {@code value} widened to a float.
	 *
	 * @param value value to convert
	 * @return float value
	 * @throws IllegalArgumentException if {@code value} cannot be widened to {@code float}
	 */
	public static float toFloat(final Object value) {
		if (isLong(value)) {
			return value instanceof Character character ? character : ((Number) value).floatValue();
		}
		if (value instanceof Float floatValue) {
			return floatValue;
		}
		throw incompatible(float.class, value);
	}

	/**
	 * Returns {@code value} widened to a double.
	 *
	 * @param value value to convert
	 * @return double value
	 * @throws IllegalArgumentException if {@code value} cannot be widened to {@code double}
	 */
	public static double toDouble(final Object value) {
		if (isFloat(value)) {
			return value instanceof Character character ? character : ((Number) value).doubleValue();
		}
		throw incompatible(double.class, value);
	}

	/**
	 * Returns true when {@code value} can be widened to {@code int}.
	 *
	 * @param value value to test
	 * @return true when the value can be stored in an {@code int}
	 */
	private static boolean isInt(final Object value) {
		return value instanceof Byte || value instanceof Short || value instanceof Character || value instanceof Integer;
	}

	/**
	 * Returns true when {@code value} can be widened to {@code long}.
	 *
	 * @param value value to test
	 * @return true when the value can be stored in a {@code long}
	 */
	private static boolean isLong(final Object value) {
		return isInt(value) || value instanceof Long;
	}

	/**
	 * Returns true when {@code value} can be widened to {@code float}.
	 *
	 * @param value value to test
	 * @return true when the value can be stored in a {@code float}
	 */
	private static boolean isFloat(final Object value) {
		return isLong(value) || value instanceof Float;
	}

	/**
	 * Returns true when {@code value} can be widened to {@code double}.
	 *
	 * @param value value to test
	 * @return true when the value can be stored in a {@code double}
	 */
	private static boolean isDouble(final Object value) {
		return isFloat(value) || value instanceof Double;
	}

	/**
	 * Returns the primitive type represented by {@code type}, or {@code null} when {@code type} is null or not a primitive
	 * or wrapper.
	 *
	 * @param type primitive or wrapper type
	 * @return primitive type, or {@code null}
	 */
	private static Class<?> primitiveOf(final Class<?> type) {
		if (null == type) {
			return null;
		}
		if (type.isPrimitive()) {
			return type;
		}
		return Primitives.isUnboxable(type) ? Primitives.toPrimitive(type) : null;
	}

	/**
	 * Returns true when {@code source} widens to {@code target}. Both arguments are primitive types.
	 *
	 * @param target primitive target
	 * @param source primitive source
	 * @return true when the source widens to the target
	 */
	private static boolean widens(final Class<?> target, final Class<?> source) {
		if (target == source) {
			return true;
		}
		if (byte.class == source) {
			return short.class == target || int.class == target || long.class == target || float.class == target || double.class == target;
		}
		if (short.class == source || char.class == source) {
			return int.class == target || long.class == target || float.class == target || double.class == target;
		}
		if (int.class == source) {
			return long.class == target || float.class == target || double.class == target;
		}
		if (long.class == source) {
			return float.class == target || double.class == target;
		}
		return float.class == source && double.class == target;
	}

	/**
	 * Returns the exception thrown when {@code value} cannot be stored in {@code target}.
	 *
	 * @param target field type
	 * @param value rejected value
	 * @return type mismatch exception
	 */
	private static IllegalArgumentException incompatible(final Class<?> target, final Object value) {
		String actual = null == value ? "null" : value.getClass().getName();
		return new IllegalArgumentException("Can not set " + target.getName() + " field to " + actual);
	}

	/**
	 * Private constructor that throws exception if called.
	 */
	private TypeCoercion() {
		throw Constructors.unsupportedOperationException();
	}
}
