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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.morphix.utils.Tests;

/**
 * Test class for {@link Primitives}.
 *
 * @author Radu Sebastian LAZIN
 */
class PrimitivesTest {

	@ParameterizedTest
	@MethodSource("providePrimitivesAndBoxedClasses")
	void shouldTransformToAllPrimitives(final Class<?> primitiveClass, final Class<?> boxedClass) {
		assertThat(Primitives.toPrimitive(boxedClass), equalTo(primitiveClass));
	}

	@Test
	void shouldThrowExceptionIfClassCannotBeTransformedToPrimitive() {
		assertThrows(ReflectionException.class, () -> Primitives.toPrimitive(String.class));
	}

	@ParameterizedTest
	@MethodSource("providePrimitivesAndBoxedClasses")
	void shouldTransformFromAllPrimitives(final Class<?> primitiveClass, final Class<?> boxedClass) {
		assertThat(Primitives.fromPrimitive(primitiveClass), equalTo(boxedClass));
	}

	@Test
	void shouldThrowExceptionIfParameterIsNotPrimitive() {
		assertThrows(ReflectionException.class, () -> Primitives.fromPrimitive(String.class));
	}

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(Primitives.class);

		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@ParameterizedTest
	@MethodSource("providePrimitivesAndBoxedClasses")
	void shouldReturnTrueIfTheClassIsUnboxable(@SuppressWarnings("unused") final Class<?> primitiveClass, final Class<?> boxedClass) {
		assertTrue(Primitives.isUnboxable(boxedClass));
	}

	@Test
	void shouldReportWhetherAClassIsUnboxable() {
		assertFalse(Primitives.isUnboxable(String.class));
		assertFalse(Primitives.isUnboxable(int.class));
		assertFalse(Primitives.isNotUnboxable(Integer.class));
		assertTrue(Primitives.isNotUnboxable(String.class));
	}

	@Test
	void shouldReportWhetherAClassIsBoxable() {
		assertTrue(Primitives.isBoxable(int.class));
		assertFalse(Primitives.isBoxable(Integer.class));
		assertFalse(Primitives.isBoxable(void.class));
		assertFalse(Primitives.isNotBoxable(int.class));
		assertTrue(Primitives.isNotBoxable(Integer.class));
		assertTrue(Primitives.isNotBoxable(void.class));
	}

	@Test
	void shouldReturnThePrimitiveRepresentation() {
		assertThat(Primitives.primitiveOf(null), nullValue());
		assertThat(Primitives.primitiveOf(int.class), equalTo(int.class));
		assertThat(Primitives.primitiveOf(void.class), equalTo(void.class));
		assertThat(Primitives.primitiveOf(Integer.class), equalTo(int.class));
		assertThat(Primitives.primitiveOf(String.class), nullValue());
	}

	@Test
	void shouldReturnTheBoxedRepresentation() {
		assertThat(Primitives.boxedOf(null), nullValue());
		assertThat(Primitives.boxedOf(int.class), equalTo(Integer.class));
		assertThat(Primitives.boxedOf(void.class), nullValue());
		assertThat(Primitives.boxedOf(Integer.class), equalTo(Integer.class));
		assertThat(Primitives.boxedOf(String.class), nullValue());
	}

	@Test
	void shouldReturnNullWhenTheClassHasNoCounterpart() {
		assertThat(Primitives.getUnboxedClass(String.class), nullValue());
		assertThat(Primitives.getUnboxedClass(void.class), nullValue());
		assertThat(Primitives.getBoxedClass(String.class), nullValue());
		assertThat(Primitives.getBoxedClass(void.class), nullValue());
	}

	private static Stream<Arguments> providePrimitivesAndBoxedClasses() {
		return Stream.of(
				Arguments.of(int.class, Integer.class),
				Arguments.of(long.class, Long.class),
				Arguments.of(char.class, Character.class),
				Arguments.of(short.class, Short.class),
				Arguments.of(byte.class, Byte.class),
				Arguments.of(boolean.class, Boolean.class),
				Arguments.of(float.class, Float.class),
				Arguments.of(double.class, Double.class));
	}
}
