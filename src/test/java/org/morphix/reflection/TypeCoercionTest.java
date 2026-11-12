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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.morphix.utils.Tests;

/**
 * Test class for {@link TypeConversions}.
 *
 * @author Radu Sebastian LAZIN
 */
class TypeCoercionTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(TypeConversions.class);

		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldAcceptWideningConversions() {
		assertTrue(TypeConversions.isApplicable(short.class, (byte) 1));
		assertTrue(TypeConversions.isApplicable(int.class, (byte) 1));
		assertTrue(TypeConversions.isApplicable(int.class, (short) 1));
		assertTrue(TypeConversions.isApplicable(int.class, 'a'));
		assertTrue(TypeConversions.isApplicable(long.class, 1));
		assertTrue(TypeConversions.isApplicable(float.class, 1L));
		assertTrue(TypeConversions.isApplicable(double.class, 1f));
		assertTrue(TypeConversions.isApplicable(double.class, 'a'));

		assertTrue(TypeConversions.isApplicable(long.class, int.class));
		assertTrue(TypeConversions.isApplicable(long.class, Integer.class));
		assertTrue(TypeConversions.isApplicable(double.class, byte.class));
		assertTrue(TypeConversions.isApplicable(int.class, Character.class));
	}

	@Test
	void shouldRejectNarrowingConversions() {
		assertFalse(TypeConversions.isApplicable(int.class, 1L));
		assertFalse(TypeConversions.isApplicable(short.class, 'a'));
		assertFalse(TypeConversions.isApplicable(char.class, (byte) 1));
		assertFalse(TypeConversions.isApplicable(boolean.class, 1));
		assertFalse(TypeConversions.isApplicable(int.class, (Object) null));
		assertFalse(TypeConversions.isApplicable(int.class, "1"));

		assertFalse(TypeConversions.isApplicable(int.class, long.class));
		assertFalse(TypeConversions.isApplicable(int.class, Long.class));
		assertFalse(TypeConversions.isApplicable(char.class, byte.class));
		assertFalse(TypeConversions.isApplicable(int.class, (Class<?>) null));
		assertFalse(TypeConversions.isApplicable(null, int.class));
	}

	@Test
	void shouldAcceptReferenceValues() {
		assertTrue(TypeConversions.isApplicable(String.class, "value"));
		assertTrue(TypeConversions.isApplicable(CharSequence.class, "value"));
		assertTrue(TypeConversions.isApplicable(String.class, (Object) null));
		assertTrue(TypeConversions.isApplicable(String.class, (Class<?>) null));
		assertTrue(TypeConversions.isApplicable(Number.class, Integer.class));

		assertFalse(TypeConversions.isApplicable(Integer.class, "value"));
		assertFalse(TypeConversions.isApplicable(String.class, Number.class));
	}

	@Test
	void shouldUnboxAndWiden() {
		assertThat(TypeConversions.toBoolean(Boolean.TRUE), equalTo(true));
		assertThat(TypeConversions.toByte((byte) 4), equalTo((byte) 4));
		assertThat(TypeConversions.toChar('A'), equalTo('A'));
		assertThat(TypeConversions.toShort((byte) 4), equalTo((short) 4));
		assertThat(TypeConversions.toInt((byte) 4), equalTo(4));
		assertThat(TypeConversions.toInt('A'), equalTo((int) 'A'));
		assertThat(TypeConversions.toLong(4), equalTo(4L));
		assertThat(TypeConversions.toFloat(4L), equalTo(4f));
		assertThat(TypeConversions.toDouble(4f), equalTo(4d));
	}

	@Test
	void shouldThrowWhenTheValueCannotBeWidened() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> TypeConversions.toInt(1L));

		assertThat(e.getMessage(), equalTo("Can not set int field to java.lang.Long"));
	}

	@Test
	void shouldRejectNullTargetForAValue() {
		assertFalse(TypeConversions.isApplicable(null, "value"));
	}

	@Test
	void shouldAcceptExactPrimitiveAndWrapper() {
		assertTrue(TypeConversions.isApplicable(boolean.class, Boolean.TRUE));
		assertTrue(TypeConversions.isApplicable(byte.class, (byte) 1));
		assertTrue(TypeConversions.isApplicable(short.class, (short) 1));
		assertTrue(TypeConversions.isApplicable(double.class, 1d));

		assertTrue(TypeConversions.isApplicable(boolean.class, boolean.class));
		assertTrue(TypeConversions.isApplicable(byte.class, byte.class));
		assertTrue(TypeConversions.isApplicable(byte.class, Byte.class));
		assertTrue(TypeConversions.isApplicable(int.class, Integer.class));
		assertTrue(TypeConversions.isApplicable(void.class, void.class));
	}

	@Test
	void shouldAcceptEveryWideningTarget() {
		assertTrue(TypeConversions.isApplicable(short.class, byte.class));
		assertTrue(TypeConversions.isApplicable(int.class, byte.class));
		assertTrue(TypeConversions.isApplicable(long.class, byte.class));
		assertTrue(TypeConversions.isApplicable(float.class, byte.class));

		assertTrue(TypeConversions.isApplicable(int.class, short.class));
		assertTrue(TypeConversions.isApplicable(long.class, short.class));
		assertTrue(TypeConversions.isApplicable(float.class, short.class));
		assertTrue(TypeConversions.isApplicable(double.class, short.class));

		assertTrue(TypeConversions.isApplicable(long.class, char.class));
		assertTrue(TypeConversions.isApplicable(float.class, char.class));
		assertTrue(TypeConversions.isApplicable(double.class, char.class));

		assertTrue(TypeConversions.isApplicable(float.class, int.class));
		assertTrue(TypeConversions.isApplicable(double.class, int.class));

		assertTrue(TypeConversions.isApplicable(float.class, long.class));
		assertTrue(TypeConversions.isApplicable(double.class, long.class));

		assertTrue(TypeConversions.isApplicable(double.class, float.class));
	}

	@Test
	void shouldRejectPrimitiveSourcesThatDoNotWiden() {
		assertFalse(TypeConversions.isApplicable(byte.class, (short) 1));
		assertFalse(TypeConversions.isApplicable(char.class, byte.class));
		assertFalse(TypeConversions.isApplicable(char.class, short.class));
		assertFalse(TypeConversions.isApplicable(byte.class, int.class));
		assertFalse(TypeConversions.isApplicable(int.class, float.class));
		assertFalse(TypeConversions.isApplicable(double.class, boolean.class));
		assertFalse(TypeConversions.isApplicable(int.class, String.class));
		assertFalse(TypeConversions.isApplicable(void.class, 1));
		assertFalse(TypeConversions.isApplicable(double.class, "1"));
	}

	@Test
	void shouldUnboxEveryAcceptedSource() {
		assertThat(TypeConversions.toShort((short) 4), equalTo((short) 4));
		assertThat(TypeConversions.toInt((short) 4), equalTo(4));
		assertThat(TypeConversions.toInt(Integer.valueOf(4)), equalTo(4));

		assertThat(TypeConversions.toLong((byte) 4), equalTo(4L));
		assertThat(TypeConversions.toLong((short) 4), equalTo(4L));
		assertThat(TypeConversions.toLong(4L), equalTo(4L));
		assertThat(TypeConversions.toLong('A'), equalTo((long) 'A'));

		assertThat(TypeConversions.toFloat('A'), equalTo((float) 'A'));
		assertThat(TypeConversions.toFloat(4f), equalTo(4f));

		assertThat(TypeConversions.toDouble('A'), equalTo((double) 'A'));
		assertThat(TypeConversions.toDouble(4d), equalTo(4d));
	}

	@Test
	void shouldThrowWhenTheValueCannotBeUnboxed() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> TypeConversions.toBoolean(null));
		assertThat(e.getMessage(), equalTo("Can not set boolean field to null"));

		assertThat(thrownBy(() -> TypeConversions.toByte((short) 1)).getMessage(), equalTo("Can not set byte field to java.lang.Short"));
		assertThat(thrownBy(() -> TypeConversions.toChar((byte) 1)).getMessage(), equalTo("Can not set char field to java.lang.Byte"));
		assertThat(thrownBy(() -> TypeConversions.toShort(1)).getMessage(), equalTo("Can not set short field to java.lang.Integer"));
		assertThat(thrownBy(() -> TypeConversions.toLong(1f)).getMessage(), equalTo("Can not set long field to java.lang.Float"));
		assertThat(thrownBy(() -> TypeConversions.toFloat(1d)).getMessage(), equalTo("Can not set float field to java.lang.Double"));
		assertThat(thrownBy(() -> TypeConversions.toDouble("1")).getMessage(), equalTo("Can not set double field to java.lang.String"));
	}

	private static IllegalArgumentException thrownBy(final Executable executable) {
		return assertThrows(IllegalArgumentException.class, executable);
	}

}
