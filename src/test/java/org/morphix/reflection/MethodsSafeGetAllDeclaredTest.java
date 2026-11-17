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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Methods.Safe#getAllDeclared(Class)}</li>
 * <li>{@link Methods.Safe#getAllDeclared(Class, java.util.function.Predicate)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsSafeGetAllDeclaredTest {

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
	void shouldGetAllMethodsInHierarchy() {
		List<Method> methods = Methods.Safe.getAllDeclared(B.class);

		Method[] methodsB = B.class.getDeclaredMethods();
		int sizeB = B.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeB));

		for (int i = 0; i < sizeB; ++i) {
			assertThat(methods.get(i), equalTo(methodsB[i]));
		}
	}

	@Test
	void shouldReturnEmptyListForClassesWithNoMethods() {
		List<Method> methods = Methods.Safe.getAllDeclared(C.class);

		Method[] methodsC = C.class.getDeclaredMethods();
		int size = C.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(size));

		for (int i = 0; i < size; ++i) {
			assertThat(methods.get(i), equalTo(methodsC[i]));
		}
	}

	@Test
	void shouldReturnEnumClassMethodsListForEmptyEnumsToo() {
		List<Method> methods = Methods.Safe.getAllDeclared(E.class);

		Method[] methodsE = E.class.getDeclaredMethods();
		int sizeE = E.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeE));

		for (int i = 0; i < sizeE; ++i) {
			assertThat(methods.get(i), equalTo(methodsE[i]));
		}
	}

	@Test
	void shouldReturnEmptyListIfClassIsNull() {
		assertThat(Methods.Safe.getAllDeclared(null), empty());
	}

	@Test
	void shouldReturnEmptyListIfClassIsNullWithPredicate() {
		assertThat(Methods.Safe.getAllDeclared(null, method -> true), empty());
	}

	@Test
	void shouldReturnEmptyListIfPredicateIsNull() {
		assertThat(Methods.Safe.getAllDeclared(MethodsSafeGetAllDeclaredTest.class, null), empty());
	}

	@ParameterizedTest
	@MethodSource("provideClassesForParity")
	void shouldMatchMethodsGetAllDeclared(final Class<?> cls) {
		assertThat(Methods.Safe.getAllDeclared(cls), equalTo(Methods.getAllDeclared(cls)));
	}

	@ParameterizedTest
	@MethodSource("provideClassesForParity")
	void shouldMatchMethodsGetAllDeclaredWithPredicate(final Class<?> cls) {
		assertThat(Methods.Safe.getAllDeclared(cls, method -> true), equalTo(Methods.getAllDeclared(cls, method -> true)));
	}

	private static Stream<Arguments> provideClassesForParity() {
		return Stream.of(Arguments.of(B.class), Arguments.of(C.class), Arguments.of(E.class));
	}
}
