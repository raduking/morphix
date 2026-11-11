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
import org.morphix.utils.Tests;

/**
 * Test class for {@link TypeCoercion}.
 *
 * @author Radu Sebastian LAZIN
 */
class TypeCoercionTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(TypeCoercion.class);

		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldAcceptWideningConversions() {
		assertTrue(TypeCoercion.isApplicable(short.class, (byte) 1));
		assertTrue(TypeCoercion.isApplicable(int.class, (byte) 1));
		assertTrue(TypeCoercion.isApplicable(int.class, (short) 1));
		assertTrue(TypeCoercion.isApplicable(int.class, 'a'));
		assertTrue(TypeCoercion.isApplicable(long.class, 1));
		assertTrue(TypeCoercion.isApplicable(float.class, 1L));
		assertTrue(TypeCoercion.isApplicable(double.class, 1f));
		assertTrue(TypeCoercion.isApplicable(double.class, 'a'));

		assertTrue(TypeCoercion.isApplicable(long.class, int.class));
		assertTrue(TypeCoercion.isApplicable(long.class, Integer.class));
		assertTrue(TypeCoercion.isApplicable(double.class, byte.class));
		assertTrue(TypeCoercion.isApplicable(int.class, Character.class));
	}

	@Test
	void shouldRejectNarrowingConversions() {
		assertFalse(TypeCoercion.isApplicable(int.class, 1L));
		assertFalse(TypeCoercion.isApplicable(short.class, 'a'));
		assertFalse(TypeCoercion.isApplicable(char.class, (byte) 1));
		assertFalse(TypeCoercion.isApplicable(boolean.class, 1));
		assertFalse(TypeCoercion.isApplicable(int.class, (Object) null));
		assertFalse(TypeCoercion.isApplicable(int.class, "1"));

		assertFalse(TypeCoercion.isApplicable(int.class, long.class));
		assertFalse(TypeCoercion.isApplicable(int.class, Long.class));
		assertFalse(TypeCoercion.isApplicable(char.class, byte.class));
		assertFalse(TypeCoercion.isApplicable(int.class, (Class<?>) null));
		assertFalse(TypeCoercion.isApplicable(null, int.class));
	}

	@Test
	void shouldAcceptReferenceValues() {
		assertTrue(TypeCoercion.isApplicable(String.class, "value"));
		assertTrue(TypeCoercion.isApplicable(CharSequence.class, "value"));
		assertTrue(TypeCoercion.isApplicable(String.class, (Object) null));
		assertTrue(TypeCoercion.isApplicable(String.class, (Class<?>) null));
		assertTrue(TypeCoercion.isApplicable(Number.class, Integer.class));

		assertFalse(TypeCoercion.isApplicable(Integer.class, "value"));
		assertFalse(TypeCoercion.isApplicable(String.class, Number.class));
	}

	@Test
	void shouldUnboxAndWiden() {
		assertThat(TypeCoercion.toBoolean(Boolean.TRUE), equalTo(true));
		assertThat(TypeCoercion.toByte((byte) 4), equalTo((byte) 4));
		assertThat(TypeCoercion.toChar('A'), equalTo('A'));
		assertThat(TypeCoercion.toShort((byte) 4), equalTo((short) 4));
		assertThat(TypeCoercion.toInt((byte) 4), equalTo(4));
		assertThat(TypeCoercion.toInt('A'), equalTo((int) 'A'));
		assertThat(TypeCoercion.toLong(4), equalTo(4L));
		assertThat(TypeCoercion.toFloat(4L), equalTo(4f));
		assertThat(TypeCoercion.toDouble(4f), equalTo(4d));
	}

	@Test
	void shouldThrowWhenTheValueCannotBeWidened() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> TypeCoercion.toInt(1L));

		assertThat(e.getMessage(), equalTo("Can not set int field to java.lang.Long"));
	}

}
