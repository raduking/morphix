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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.morphix.utils.Tests;

/**
 * Test class for {@link PrimitiveValues}.
 *
 * @author Radu Sebastian LAZIN
 */
class PrimitiveValuesTest {

	private static final int NOT_EXACT_IN_FLOAT = (1 << 24) + 1;

	private static final long NOT_EXACT_IN_DOUBLE = (1L << 53) + 1;

	@Nested
	class ConstructorTests {

		@Test
		void shouldThrowExceptionOnCallingConstructor() {
			UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(PrimitiveValues.class);

			assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
		}
	}

	@Nested
	class IsConvertibleTests {

		@Test
		void shouldRejectNullOrVoidTarget() {
			assertFalse(PrimitiveValues.isConvertible(null, "value"));
			assertFalse(PrimitiveValues.isConvertible(null, int.class));
			assertFalse(PrimitiveValues.isConvertible(void.class, 1));
			assertFalse(PrimitiveValues.isConvertible(void.class, null));
			assertFalse(PrimitiveValues.isConvertible(void.class, void.class));
		}

		@Test
		void shouldAcceptNullOnlyForReferences() {
			assertTrue(PrimitiveValues.isConvertible(String.class, (Object) null));
			assertTrue(PrimitiveValues.isConvertible(Integer.class, (Object) null));
			assertTrue(PrimitiveValues.isConvertible(String.class, (Class<?>) null));

			assertFalse(PrimitiveValues.isConvertible(int.class, (Object) null));
			assertFalse(PrimitiveValues.isConvertible(int.class, (Class<?>) null));
		}

		@Test
		void shouldDelegateClassValuesToAlwaysConvertible() {
			assertTrue(PrimitiveValues.isConvertible(long.class, int.class));
			assertTrue(PrimitiveValues.isConvertible(long.class, Integer.class));
			assertTrue(PrimitiveValues.isConvertible(Number.class, Integer.class));
			assertFalse(PrimitiveValues.isConvertible(int.class, void.class));

			assertFalse(PrimitiveValues.isConvertible(int.class, long.class));
			assertFalse(PrimitiveValues.isConvertible(String.class, Number.class));
		}

		@Test
		void shouldAcceptValuesAlreadyAssignableToTheTarget() {
			assertTrue(PrimitiveValues.isConvertible(String.class, "value"));
			assertTrue(PrimitiveValues.isConvertible(CharSequence.class, "value"));
			assertTrue(PrimitiveValues.isConvertible(Integer.class, Integer.valueOf(1)));
			assertTrue(PrimitiveValues.isConvertible(Object.class, new Object()));
		}

		@Test
		void shouldAcceptExactPrimitiveConversions() {
			assertTrue(PrimitiveValues.isConvertible(boolean.class, Boolean.TRUE));
			assertTrue(PrimitiveValues.isConvertible(byte.class, (byte) 1));
			assertTrue(PrimitiveValues.isConvertible(char.class, 'A'));
			assertTrue(PrimitiveValues.isConvertible(short.class, (short) 1));
			assertTrue(PrimitiveValues.isConvertible(int.class, 1));
			assertTrue(PrimitiveValues.isConvertible(long.class, 1L));
			assertTrue(PrimitiveValues.isConvertible(float.class, 1f));
			assertTrue(PrimitiveValues.isConvertible(double.class, 1d));

			assertTrue(PrimitiveValues.isConvertible(Integer.class, (byte) 1));
			assertTrue(PrimitiveValues.isConvertible(byte.class, (short) 1));
			assertTrue(PrimitiveValues.isConvertible(short.class, 'a'));
			assertTrue(PrimitiveValues.isConvertible(char.class, (byte) 1));
		}

		@Test
		void shouldRejectValuesThatCannotBeConvertedExactly() {
			assertFalse(PrimitiveValues.isConvertible(boolean.class, 1));
			assertFalse(PrimitiveValues.isConvertible(byte.class, (short) 200));
			assertFalse(PrimitiveValues.isConvertible(char.class, (byte) -1));
			assertFalse(PrimitiveValues.isConvertible(int.class, "1"));
			assertFalse(PrimitiveValues.isConvertible(String.class, 1));
			assertFalse(PrimitiveValues.isConvertible(Number.class, "value"));
			assertFalse(PrimitiveValues.isConvertible(double.class, "1"));
		}
	}

	@Nested
	class IsAlwaysConvertibleTests {

		@Test
		void shouldRejectNullOrVoidTypes() {
			assertFalse(PrimitiveValues.isAlwaysConvertible(null, int.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(int.class, null));
			assertFalse(PrimitiveValues.isAlwaysConvertible(null, null));
			assertFalse(PrimitiveValues.isAlwaysConvertible(void.class, int.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(int.class, void.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(void.class, void.class));
		}

		@Test
		void shouldAcceptReferenceAssignment() {
			assertTrue(PrimitiveValues.isAlwaysConvertible(String.class, String.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(CharSequence.class, String.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(Number.class, Integer.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(Number.class, byte.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(Integer.class, int.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(Object.class, String.class));
		}

		@Test
		void shouldAcceptTheSamePrimitiveAndItsWrapper() {
			assertTrue(PrimitiveValues.isAlwaysConvertible(int.class, int.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(int.class, Integer.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(boolean.class, boolean.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(boolean.class, Boolean.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(byte.class, byte.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(byte.class, Byte.class));
		}

		@Test
		void shouldRejectBooleanMixedWithOtherPrimitives() {
			assertFalse(PrimitiveValues.isAlwaysConvertible(boolean.class, int.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(int.class, boolean.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(boolean.class, byte.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(byte.class, boolean.class));
		}

		@Test
		void shouldAcceptEveryExactByteTarget() {
			assertTrue(PrimitiveValues.isAlwaysConvertible(short.class, byte.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(int.class, byte.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(long.class, byte.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(float.class, byte.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(double.class, byte.class));

			assertFalse(PrimitiveValues.isAlwaysConvertible(char.class, byte.class));
		}

		@Test
		void shouldAcceptEveryExactShortOrCharTarget() {
			assertTrue(PrimitiveValues.isAlwaysConvertible(int.class, short.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(long.class, short.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(float.class, short.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(double.class, short.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(byte.class, short.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(char.class, short.class));

			assertTrue(PrimitiveValues.isAlwaysConvertible(int.class, char.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(long.class, char.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(float.class, char.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(double.class, char.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(short.class, char.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(int.class, Character.class));
		}

		@Test
		void shouldAcceptOnlyLongAndDoubleForInt() {
			assertTrue(PrimitiveValues.isAlwaysConvertible(long.class, int.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(double.class, int.class));
			assertTrue(PrimitiveValues.isAlwaysConvertible(long.class, Integer.class));

			assertFalse(PrimitiveValues.isAlwaysConvertible(float.class, int.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(short.class, int.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(byte.class, int.class));
		}

		@Test
		void shouldAcceptOnlyDoubleForFloat() {
			assertTrue(PrimitiveValues.isAlwaysConvertible(double.class, float.class));

			assertFalse(PrimitiveValues.isAlwaysConvertible(long.class, float.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(double.class, long.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(float.class, long.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(float.class, double.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(int.class, double.class));
		}

		@Test
		void shouldRejectUnrelatedReferenceTypes() {
			assertFalse(PrimitiveValues.isAlwaysConvertible(String.class, Integer.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(String.class, int.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(int.class, String.class));
			assertFalse(PrimitiveValues.isAlwaysConvertible(String.class, Number.class));
		}
	}

	@Nested
	class ToBooleanTests {

		@Test
		void shouldReturnTheBooleanValue() {
			assertThat(PrimitiveValues.toBoolean(Boolean.TRUE), equalTo(true));
			assertThat(PrimitiveValues.toBoolean(Boolean.FALSE), equalTo(false));
		}

		@Test
		void shouldRejectNonBooleanValues() {
			assertIncompatible(boolean.class, null, () -> PrimitiveValues.toBoolean(null));
			assertIncompatible(boolean.class, 1, () -> PrimitiveValues.toBoolean(1));
			assertIncompatible(boolean.class, "true", () -> PrimitiveValues.toBoolean("true"));
		}
	}

	@Nested
	class ToByteTests {

		@Test
		void shouldReturnExactByteValues() {
			assertThat(PrimitiveValues.toByte((byte) 4), equalTo((byte) 4));
			assertThat(PrimitiveValues.toByte(Byte.MIN_VALUE), equalTo(Byte.MIN_VALUE));
			assertThat(PrimitiveValues.toByte(Byte.MAX_VALUE), equalTo(Byte.MAX_VALUE));
			assertThat(PrimitiveValues.toByte((short) 1), equalTo((byte) 1));
			assertThat(PrimitiveValues.toByte('A'), equalTo((byte) 'A'));
		}

		@Test
		void shouldRejectValuesOutsideTheByteRange() {
			assertInexact(byte.class, Byte.MIN_VALUE - 1, () -> PrimitiveValues.toByte(Byte.MIN_VALUE - 1));
			assertInexact(byte.class, Byte.MAX_VALUE + 1, () -> PrimitiveValues.toByte(Byte.MAX_VALUE + 1));
			assertInexact(byte.class, (char) 200, () -> PrimitiveValues.toByte((char) 200));
		}

		@Test
		void shouldRejectUnsupportedValues() {
			assertIncompatible(byte.class, "1", () -> PrimitiveValues.toByte("1"));
			assertIncompatible(byte.class, null, () -> PrimitiveValues.toByte(null));
		}
	}

	@Nested
	class ToCharTests {

		@Test
		void shouldReturnExactCharacterValues() {
			assertThat(PrimitiveValues.toChar('A'), equalTo('A'));
			assertThat(PrimitiveValues.toChar((char) 0), equalTo((char) 0));
			assertThat(PrimitiveValues.toChar(Character.MAX_VALUE), equalTo(Character.MAX_VALUE));
			assertThat(PrimitiveValues.toChar((byte) 1), equalTo((char) 1));
		}

		@Test
		void shouldRejectValuesOutsideTheCharacterRange() {
			assertInexact(char.class, -1, () -> PrimitiveValues.toChar(-1));
			assertInexact(char.class, Character.MAX_VALUE + 1, () -> PrimitiveValues.toChar(Character.MAX_VALUE + 1));
			assertInexact(char.class, (byte) -1, () -> PrimitiveValues.toChar((byte) -1));
		}

		@Test
		void shouldRejectUnsupportedValues() {
			assertIncompatible(char.class, "A", () -> PrimitiveValues.toChar("A"));
			assertIncompatible(char.class, null, () -> PrimitiveValues.toChar(null));
		}
	}

	@Nested
	class ToShortTests {

		@Test
		void shouldReturnExactShortValues() {
			assertThat(PrimitiveValues.toShort((short) 4), equalTo((short) 4));
			assertThat(PrimitiveValues.toShort((byte) 4), equalTo((short) 4));
			assertThat(PrimitiveValues.toShort('A'), equalTo((short) 'A'));
			assertThat(PrimitiveValues.toShort(Short.MIN_VALUE), equalTo(Short.MIN_VALUE));
			assertThat(PrimitiveValues.toShort(Short.MAX_VALUE), equalTo(Short.MAX_VALUE));
		}

		@Test
		void shouldRejectValuesOutsideTheShortRange() {
			assertInexact(short.class, Short.MIN_VALUE - 1, () -> PrimitiveValues.toShort(Short.MIN_VALUE - 1));
			assertInexact(short.class, Short.MAX_VALUE + 1, () -> PrimitiveValues.toShort(Short.MAX_VALUE + 1));
			assertInexact(short.class, 40_000, () -> PrimitiveValues.toShort(40_000));
		}

		@Test
		void shouldRejectUnsupportedValues() {
			assertIncompatible(short.class, "1", () -> PrimitiveValues.toShort("1"));
			assertIncompatible(short.class, null, () -> PrimitiveValues.toShort(null));
		}
	}

	@Nested
	class ToIntTests {

		@Test
		void shouldReturnExactIntValues() {
			assertThat(PrimitiveValues.toInt(4), equalTo(4));
			assertThat(PrimitiveValues.toInt((byte) 4), equalTo(4));
			assertThat(PrimitiveValues.toInt((short) 4), equalTo(4));
			assertThat(PrimitiveValues.toInt('A'), equalTo((int) 'A'));
			assertThat(PrimitiveValues.toInt(4L), equalTo(4));
			assertThat(PrimitiveValues.toInt(Integer.valueOf(4)), equalTo(4));
			assertThat(PrimitiveValues.toInt(Integer.MIN_VALUE), equalTo(Integer.MIN_VALUE));
			assertThat(PrimitiveValues.toInt(Integer.MAX_VALUE), equalTo(Integer.MAX_VALUE));
			assertThat(PrimitiveValues.toInt(4.0d), equalTo(4));
		}

		@Test
		void shouldRejectValuesThatDoNotFitInAnInt() {
			ArithmeticException above = assertThrows(ArithmeticException.class, () -> PrimitiveValues.toInt((long) Integer.MAX_VALUE + 1));
			ArithmeticException below = assertThrows(ArithmeticException.class, () -> PrimitiveValues.toInt((long) Integer.MIN_VALUE - 1));

			assertThat(above.getMessage(), equalTo("integer overflow"));
			assertThat(below.getMessage(), equalTo("integer overflow"));
			assertInexact(int.class, 1.5d, () -> PrimitiveValues.toInt(1.5d));
		}

		@Test
		void shouldRejectUnsupportedValues() {
			assertIncompatible(int.class, "1", () -> PrimitiveValues.toInt("1"));
			assertIncompatible(int.class, null, () -> PrimitiveValues.toInt(null));
		}
	}

	@Nested
	class ToLongTests {

		@Test
		void shouldReturnExactIntegralValues() {
			assertThat(PrimitiveValues.toLong('A'), equalTo((long) 'A'));
			assertThat(PrimitiveValues.toLong((byte) 4), equalTo(4L));
			assertThat(PrimitiveValues.toLong((short) 4), equalTo(4L));
			assertThat(PrimitiveValues.toLong(4), equalTo(4L));
			assertThat(PrimitiveValues.toLong(4L), equalTo(4L));
			assertThat(PrimitiveValues.toLong(Long.MIN_VALUE), equalTo(Long.MIN_VALUE));
			assertThat(PrimitiveValues.toLong(Long.MAX_VALUE), equalTo(Long.MAX_VALUE));
		}

		@Test
		void shouldReturnExactFloatingPointValues() {
			assertThat(PrimitiveValues.toLong(4f), equalTo(4L));
			assertThat(PrimitiveValues.toLong(4d), equalTo(4L));
			assertThat(PrimitiveValues.toLong(0.0d), equalTo(0L));
			assertThat(PrimitiveValues.toLong(-0x1.0p63), equalTo(Long.MIN_VALUE));
		}

		@Test
		void shouldRejectNonFiniteOrOutOfRangeFloatingPointValues() {
			assertInexact(long.class, Float.NaN, () -> PrimitiveValues.toLong(Float.NaN));
			assertInexact(long.class, Double.NaN, () -> PrimitiveValues.toLong(Double.NaN));
			assertInexact(long.class, Float.POSITIVE_INFINITY, () -> PrimitiveValues.toLong(Float.POSITIVE_INFINITY));
			assertInexact(long.class, Double.NEGATIVE_INFINITY, () -> PrimitiveValues.toLong(Double.NEGATIVE_INFINITY));
			assertInexact(long.class, -1.0e20d, () -> PrimitiveValues.toLong(-1.0e20d));
			assertInexact(long.class, 0x1.0p63, () -> PrimitiveValues.toLong(0x1.0p63));
			assertInexact(long.class, -0.0f, () -> PrimitiveValues.toLong(-0.0f));
			assertInexact(long.class, -0.0d, () -> PrimitiveValues.toLong(-0.0d));
		}

		@Test
		void shouldRejectFractionalFloatingPointValues() {
			assertInexact(long.class, 1.5f, () -> PrimitiveValues.toLong(1.5f));
			assertInexact(long.class, 1.5d, () -> PrimitiveValues.toLong(1.5d));
		}

		@Test
		void shouldRejectUnsupportedValues() {
			assertIncompatible(long.class, "1", () -> PrimitiveValues.toLong("1"));
			assertIncompatible(long.class, Boolean.TRUE, () -> PrimitiveValues.toLong(Boolean.TRUE));
			assertIncompatible(long.class, null, () -> PrimitiveValues.toLong(null));
		}
	}

	@Nested
	class ToFloatTests {

		@Test
		void shouldReturnFloatValuesUnchanged() {
			assertThat(PrimitiveValues.toFloat(4f), equalTo(4f));
			assertThat(PrimitiveValues.toFloat(Float.POSITIVE_INFINITY), equalTo(Float.POSITIVE_INFINITY));
			assertTrue(Float.isNaN(PrimitiveValues.toFloat(Float.NaN)));
			assertThat(Float.floatToRawIntBits(PrimitiveValues.toFloat(-0.0f)), equalTo(Float.floatToRawIntBits(-0.0f)));
		}

		@Test
		void shouldReturnDoublesThatAreExactFloats() {
			assertThat(PrimitiveValues.toFloat(1.0d), equalTo(1f));
			assertThat(PrimitiveValues.toFloat(0.5d), equalTo(0.5f));
			assertThat(PrimitiveValues.toFloat(Double.POSITIVE_INFINITY), equalTo(Float.POSITIVE_INFINITY));
			assertTrue(Float.isNaN(PrimitiveValues.toFloat(Double.NaN)));
			assertThat(Float.floatToRawIntBits(PrimitiveValues.toFloat(-0.0d)), equalTo(Float.floatToRawIntBits(-0.0f)));
		}

		@Test
		void shouldRejectDoublesThatAreNotExactFloats() {
			assertInexact(float.class, (double) NOT_EXACT_IN_FLOAT, () -> PrimitiveValues.toFloat((double) NOT_EXACT_IN_FLOAT));
		}

		@Test
		void shouldReturnIntegralsThatAreExactFloats() {
			assertThat(PrimitiveValues.toFloat('A'), equalTo((float) 'A'));
			assertThat(PrimitiveValues.toFloat((byte) 4), equalTo(4f));
			assertThat(PrimitiveValues.toFloat((short) 4), equalTo(4f));
			assertThat(PrimitiveValues.toFloat(4), equalTo(4f));
			assertThat(PrimitiveValues.toFloat(-4), equalTo(-4f));
			assertThat(PrimitiveValues.toFloat(4L), equalTo(4f));
		}

		@Test
		void shouldRejectIntegralsThatAreNotExactFloats() {
			assertInexact(float.class, NOT_EXACT_IN_FLOAT, () -> PrimitiveValues.toFloat(NOT_EXACT_IN_FLOAT));
			assertInexact(float.class, Long.MAX_VALUE, () -> PrimitiveValues.toFloat(Long.MAX_VALUE));
		}

		@Test
		void shouldRejectUnsupportedValues() {
			assertIncompatible(float.class, "1", () -> PrimitiveValues.toFloat("1"));
			assertIncompatible(float.class, null, () -> PrimitiveValues.toFloat(null));
		}
	}

	@Nested
	class ToDoubleTests {

		@Test
		void shouldReturnDoubleValuesUnchanged() {
			assertThat(PrimitiveValues.toDouble(4d), equalTo(4d));
			assertThat(PrimitiveValues.toDouble(Double.NEGATIVE_INFINITY), equalTo(Double.NEGATIVE_INFINITY));
			assertTrue(Double.isNaN(PrimitiveValues.toDouble(Double.NaN)));
			assertThat(Double.doubleToRawLongBits(PrimitiveValues.toDouble(-0.0d)), equalTo(Double.doubleToRawLongBits(-0.0d)));
		}

		@Test
		void shouldWidenFloatValues() {
			assertThat(PrimitiveValues.toDouble(4f), equalTo(4d));
			assertThat(PrimitiveValues.toDouble(1.5f), equalTo((double) 1.5f));
			assertThat(PrimitiveValues.toDouble(Float.NEGATIVE_INFINITY), equalTo(Double.NEGATIVE_INFINITY));
			assertTrue(Double.isNaN(PrimitiveValues.toDouble(Float.NaN)));
			assertThat(Double.doubleToRawLongBits(PrimitiveValues.toDouble(-0.0f)), equalTo(Double.doubleToRawLongBits(-0.0d)));
		}

		@Test
		void shouldReturnIntegralsThatAreExactDoubles() {
			assertThat(PrimitiveValues.toDouble('A'), equalTo((double) 'A'));
			assertThat(PrimitiveValues.toDouble((byte) 4), equalTo(4d));
			assertThat(PrimitiveValues.toDouble((short) 4), equalTo(4d));
			assertThat(PrimitiveValues.toDouble(4), equalTo(4d));
			assertThat(PrimitiveValues.toDouble(4L), equalTo(4d));
			assertThat(PrimitiveValues.toDouble(Long.MIN_VALUE), equalTo((double) Long.MIN_VALUE));
		}

		@Test
		void shouldRejectIntegralsThatAreNotExactDoubles() {
			assertInexact(double.class, NOT_EXACT_IN_DOUBLE, () -> PrimitiveValues.toDouble(NOT_EXACT_IN_DOUBLE));
			assertInexact(double.class, Long.MAX_VALUE, () -> PrimitiveValues.toDouble(Long.MAX_VALUE));
		}

		@Test
		void shouldRejectUnsupportedValues() {
			assertIncompatible(double.class, "1", () -> PrimitiveValues.toDouble("1"));
			assertIncompatible(double.class, null, () -> PrimitiveValues.toDouble(null));
		}
	}

	private static void assertIncompatible(final Class<?> target, final Object value, final Executable executable) {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
		String actual = null == value ? "null" : value.getClass().getName();

		assertThat(e.getMessage(), equalTo("Cannot convert " + actual + " to " + target.getName()));
	}

	private static void assertInexact(final Class<?> target, final Object value, final Executable executable) {
		ArithmeticException e = assertThrows(ArithmeticException.class, executable);

		assertThat(e.getMessage(), equalTo("Cannot convert " + value + " exactly to " + target.getName()));
	}
}
