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

import java.util.Map;
import java.util.Set;

/**
 * Exact conversions between primitive types and their wrappers, and reference assignment compatibility:
 * <ul>
 * <li>numeric conversions preserve the represented value, including the sign of floating-point zero</li>
 * <li>NaN and infinities can be converted between floating-point types</li>
 * <li>NaN payloads are not guaranteed to be preserved</li>
 * <li>unsupported source types cause {@link IllegalArgumentException}</li>
 * <li>loss of information causes {@link ArithmeticException}</li>
 * </ul>
 * Only the standard primitive wrappers are supported as numeric sources.
 *
 * @author Radu Sebastian LAZIN
 */
public class PrimitiveValues {

	/**
	 * Maps each primitive source type to the primitive target types it converts to without loss of information. Used by
	 * {@link #isAlwaysConvertible(Class, Class)}. Boolean and void have no entries because they convert to no other type.
	 * <ul>
	 * <li>byte -> (short, int, long, float, double)</li>
	 * <li>short -> (int, long, float, double)</li>
	 * <li>char -> (int, long, float, double)</li>
	 * <li>int -> (long, float, double)</li>
	 * <li>long -> (float, double)</li>
	 * <li>float -> (double)</li>
	 * </ul>
	 */
	private static final Map<Class<?>, Set<Class<?>>> LOSSLESS_TARGETS = Map.of(
			byte.class, Set.of(
					short.class,
					int.class,
					long.class,
					float.class,
					double.class),
			short.class, Set.of(
					int.class,
					long.class,
					float.class,
					double.class),
			char.class, Set.of(
					int.class,
					long.class,
					float.class,
					double.class),
			int.class, Set.of(
					long.class,
					double.class),
			float.class, Set.of(
					double.class));

	/**
	 * Returns whether the value can be converted exactly to the target type. Primitive wrapper targets support the same
	 * conversions as their primitives. Null is accepted only for references. {@code void} is not a target type.
	 *
	 * @param target target type
	 * @param value source value
	 * @return whether the conversion is possible
	 */
	public static boolean isConvertible(final Class<?> target, final Object value) {
		if (null == target) {
			return false;
		}
		if (null == value) {
			return !target.isPrimitive();
		}
		if (value instanceof Class<?> cls) {
			return isAlwaysConvertible(target, cls);
		}
		if (target.isInstance(value)) {
			return true;
		}
		Class<?> primitive = Primitives.primitiveOf(target);
		try {
			switch (primitive) {
				case Class<?> c when c == boolean.class -> toBoolean(value);
				case Class<?> c when c == byte.class -> toByte(value);
				case Class<?> c when c == char.class -> toChar(value);
				case Class<?> c when c == short.class -> toShort(value);
				case Class<?> c when c == int.class -> toInt(value);
				case Class<?> c when c == long.class -> toLong(value);
				case Class<?> c when c == float.class -> toFloat(value);
				case Class<?> c when c == double.class -> toDouble(value);
				case null, default -> {
					return false;
				}
			}
			return true;
		} catch (IllegalArgumentException | ArithmeticException e) {
			return false;
		}
	}

	/**
	 * Returns whether every non-null value of the source type can be converted exactly to the target type. Primitive source
	 * types are treated as their boxed values for reference assignment.
	 *
	 * @param target target type
	 * @param source source type
	 * @return whether all source values are convertible
	 */
	public static boolean isAlwaysConvertible(final Class<?> target, final Class<?> source) {
		if (null == target || null == source) {
			return false;
		}
		if (void.class == target || void.class == source) {
			return false;
		}
		Class<?> boxedSource = source.isPrimitive() ? Primitives.boxedOf(source) : source;
		if (target.isAssignableFrom(boxedSource)) {
			return true;
		}
		Class<?> dst = Primitives.primitiveOf(target);
		Class<?> src = Primitives.primitiveOf(source);
		if (null == dst || null == src) {
			return false;
		}
		if (dst == src) {
			return true;
		}
		return LOSSLESS_TARGETS.getOrDefault(src, Set.of()).contains(dst);
	}

	/**
	 * Returns the boolean value, without numeric coercion.
	 *
	 * @param value source value
	 * @return the boolean value
	 * @throws IllegalArgumentException if {@code value} is not a {@link Boolean}
	 */
	public static boolean toBoolean(final Object value) {
		if (value instanceof Boolean booleanValue) {
			return booleanValue;
		}
		throw incompatible(boolean.class, value);
	}

	/**
	 * Returns the exact byte representation of the value.
	 *
	 * @param value source value
	 * @return the exact byte value
	 * @throws IllegalArgumentException if {@code value} is not a supported numeric wrapper
	 * @throws ArithmeticException if {@code value} cannot be represented exactly as a {@code byte}
	 */
	public static byte toByte(final Object value) {
		long number = toIntegral(value, byte.class);
		if (number < Byte.MIN_VALUE || number > Byte.MAX_VALUE) {
			throw inexact(byte.class, value);
		}
		return (byte) number;
	}

	/**
	 * Returns the exact unsigned 16-bit character representation of the value.
	 *
	 * @param value source value
	 * @return the exact character value
	 * @throws IllegalArgumentException if {@code value} is not a supported numeric wrapper
	 * @throws ArithmeticException if {@code value} cannot be represented exactly as a {@code char}
	 */
	public static char toChar(final Object value) {
		long number = toIntegral(value, char.class);
		if (number < Character.MIN_VALUE || number > Character.MAX_VALUE) {
			throw inexact(char.class, value);
		}
		return (char) number;
	}

	/**
	 * Returns the exact short representation of the value.
	 *
	 * @param value source value
	 * @return the exact short value
	 * @throws IllegalArgumentException if {@code value} is not a supported numeric wrapper
	 * @throws ArithmeticException if {@code value} cannot be represented exactly as a {@code short}
	 */
	public static short toShort(final Object value) {
		long number = toIntegral(value, short.class);
		if (number < Short.MIN_VALUE || number > Short.MAX_VALUE) {
			throw inexact(short.class, value);
		}
		return (short) number;
	}

	/**
	 * Returns the exact int representation of the value.
	 *
	 * @param value source value
	 * @return the exact int value
	 * @throws IllegalArgumentException if {@code value} is not a supported numeric wrapper
	 * @throws ArithmeticException if {@code value} cannot be represented exactly as an {@code int}
	 */
	public static int toInt(final Object value) {
		return Math.toIntExact(toIntegral(value, int.class));
	}

	/**
	 * Returns the exact long representation of the value.
	 *
	 * @param value source value
	 * @return the exact long value
	 * @throws IllegalArgumentException if {@code value} is not a supported numeric wrapper
	 * @throws ArithmeticException if {@code value} cannot be represented exactly as a {@code long}
	 */
	public static long toLong(final Object value) {
		return toIntegral(value, long.class);
	}

	/**
	 * Returns the exact float representation of the value.
	 *
	 * @param value source value
	 * @return the exact float value
	 * @throws IllegalArgumentException if {@code value} is not a supported numeric wrapper
	 * @throws ArithmeticException if {@code value} cannot be represented exactly as a {@code float}
	 */
	public static float toFloat(final Object value) {
		if (value instanceof Float floatValue) {
			return floatValue;
		}
		if (value instanceof Double doubleValue) {
			double number = doubleValue;
			float result = (float) number;
			if (Double.compare(number, result) != 0) {
				throw inexact(float.class, value);
			}
			return result;
		}
		long number = toIntegral(value, float.class);
		float result = number;
		if (result >= 0x1.0p63 || (long) result != number) {
			throw inexact(float.class, value);
		}
		return result;
	}

	/**
	 * Returns the exact double representation of the value.
	 *
	 * @param value source value
	 * @return the exact double value
	 * @throws IllegalArgumentException if {@code value} is not a supported numeric wrapper
	 * @throws ArithmeticException if {@code value} cannot be represented exactly as a {@code double}
	 */
	public static double toDouble(final Object value) {
		if (value instanceof Double doubleValue) {
			return doubleValue;
		}
		if (value instanceof Float floatValue) {
			return floatValue;
		}
		long number = toIntegral(value, double.class);
		double result = number;
		if (result >= 0x1.0p63 || (long) result != number) {
			throw inexact(double.class, value);
		}
		return result;
	}

	/**
	 * Converts a numeric wrapper to an exact integral value. The upper bound is exclusive because {@link Long#MAX_VALUE}
	 * rounds to 2^63 when represented as a double. Negative zero cannot be represented by an integral type.
	 *
	 * @param value source value
	 * @param target target primitive type, used only for the exception message
	 * @return the exact integral value
	 * @throws IllegalArgumentException if {@code value} is not a supported numeric wrapper
	 * @throws ArithmeticException if {@code value} cannot be represented exactly as a {@code long}
	 */
	private static long toIntegral(final Object value, final Class<?> target) {
		if (value instanceof Character character) {
			return character;
		}
		if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
			return ((Number) value).longValue();
		}
		if (value instanceof Float || value instanceof Double) {
			double number = ((Number) value).doubleValue();
			if (!Double.isFinite(number) || number < -0x1.0p63 || number >= 0x1.0p63
					|| Double.doubleToRawLongBits(number) == Long.MIN_VALUE) {
				throw inexact(target, value);
			}
			long result = (long) number;
			if (number != result) {
				throw inexact(target, value);
			}
			return result;
		}
		throw incompatible(target, value);
	}

	/**
	 * Builds the exception thrown when the value is not a supported source for the target type.
	 *
	 * @param target target type
	 * @param value source value
	 * @return the exception
	 */
	private static IllegalArgumentException incompatible(final Class<?> target, final Object value) {
		String actual = null == value ? "null" : value.getClass().getName();
		return new IllegalArgumentException("Cannot convert " + actual + " to " + target.getName());
	}

	/**
	 * Builds the exception thrown when the value cannot be represented exactly by the target type.
	 *
	 * @param target target type
	 * @param value source value
	 * @return the exception
	 */
	private static ArithmeticException inexact(final Class<?> target, final Object value) {
		return new ArithmeticException("Cannot convert " + value + " exactly to " + target.getName());
	}

	/**
	 * Hide constructor.
	 *
	 * @throws UnsupportedOperationException if called
	 */
	private PrimitiveValues() {
		throw Constructors.unsupportedOperationException();
	}
}
