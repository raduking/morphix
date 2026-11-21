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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.morphix.lang.function.Predicates;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Methods.Unchecked#getAllDeclared(Class)}</li>
 * <li>{@link Methods.Unchecked#getAllDeclared(Class, java.util.function.Predicate)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsUncheckedGetAllDeclaredTest {

	public enum E {
		// empty enum
	}

	public static class A {
		void fooA() {
			// empty
		}
	}

	public static class B extends A {
		void fooB() {
			// empty
		}
	}

	public static class C {
		// empty class
	}

	@Test
	void shouldReturnDeclaredMethodsInOrder() {
		List<Method> methods = Methods.Unchecked.getAllDeclared(B.class);

		Method[] methodsB = B.class.getDeclaredMethods();
		int sizeB = methodsB.length;

		assertThat(methods, hasSize(sizeB));

		for (int i = 0; i < sizeB; ++i) {
			assertThat(methods.get(i), equalTo(methodsB[i]));
		}
	}

	@Test
	void shouldThrowNullPointerExceptionForNullClass() {
		assertThrows(NullPointerException.class, () -> Methods.Unchecked.getAllDeclared((Class<?>) null));
	}

	@Test
	void shouldThrowNullPointerExceptionForNullClassWithPredicate() {
		assertThrows(NullPointerException.class, () -> Methods.Unchecked.getAllDeclared((Class<?>) null, method -> true));
	}

	@Test
	void shouldThrowNullPointerExceptionForNullPredicate() {
		assertThrows(NullPointerException.class, () -> Methods.Unchecked.getAllDeclared(A.class, null));
	}

	@Test
	void shouldThrowNullPointerExceptionForNullPredicateAndNoMethods() {
		assertThrows(NullPointerException.class, () -> Methods.Unchecked.getAllDeclared(C.class, null));
	}

	@ParameterizedTest
	@MethodSource("provideClassesForParity")
	void shouldMatchMethodsGetAllDeclared(final Class<?> cls) {
		assertThat(Methods.Unchecked.getAllDeclared(cls), equalTo(Methods.getAllDeclared(cls)));
	}

	@ParameterizedTest
	@MethodSource("provideClassesForParity")
	void shouldMatchMethodsGetAllDeclaredWithPredicate(final Class<?> cls) {
		assertThat(Methods.Unchecked.getAllDeclared(cls, Predicates.acceptAll()),
				equalTo(Methods.getAllDeclared(cls, Predicates.acceptAll())));
	}

	private static Stream<Arguments> provideClassesForParity() {
		return Stream.of(Arguments.of(B.class), Arguments.of(C.class), Arguments.of(E.class));
	}
}
