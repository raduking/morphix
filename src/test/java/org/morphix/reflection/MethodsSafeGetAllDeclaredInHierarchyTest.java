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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
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
 * <li>{@link Methods.Safe#getAllDeclaredInHierarchy(Class)}</li>
 * <li>{@link Methods.Safe#getAllDeclaredInHierarchy(Class, java.util.function.Predicate)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsSafeGetAllDeclaredInHierarchyTest {

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

	public static interface WithDefaults {
		default String defaultMethod() {
			return "default";
		}

		static String staticMethod() {
			return "static";
		}
	}

	@Test
	void shouldGetAllMethodsInHierarchy() {
		List<Method> methods = Methods.Safe.getAllDeclaredInHierarchy(B.class);

		int sizeB = B.class.getDeclaredMethods().length;
		int sizeA = A.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeA + sizeB + sizeObject));
	}

	@Test
	void shouldReturnEmptyListForClassesWithNoMethods() {
		List<Method> methods = Methods.Safe.getAllDeclaredInHierarchy(C.class);

		int size = C.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(size + sizeObject));
	}

	@Test
	void shouldReturnEnumClassMethodsListForEmptyEnumsToo() {
		List<Method> methods = Methods.Safe.getAllDeclaredInHierarchy(E.class);

		int sizeEnum = Enum.class.getDeclaredMethods().length;
		int sizeE = E.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeEnum + sizeE + sizeObject));
	}

	@Test
	void shouldReturnEmptyListIfClassIsNull() {
		assertThat(Methods.Safe.getAllDeclaredInHierarchy(null), empty());
	}

	@Test
	void shouldReturnEmptyListIfClassIsNullWithPredicate() {
		assertThat(Methods.Safe.getAllDeclaredInHierarchy(null, method -> true), empty());
	}

	@Test
	void shouldReturnEmptyListIfPredicateIsNull() {
		assertThat(Methods.Safe.getAllDeclaredInHierarchy(MethodsSafeGetAllDeclaredInHierarchyTest.class, null), empty());
	}

	@Test
	void shouldReturnTheOwnMethodsOfAnInterface() {
		List<Method> methods = Methods.Safe.getAllDeclaredInHierarchy(WithDefaults.class);

		assertThat(namesOf(methods), containsInAnyOrder("defaultMethod", "staticMethod"));
	}

	@Test
	void shouldReturnTheOwnMethodsOfAnInterfaceWithPredicate() {
		List<Method> methods = Methods.Safe.getAllDeclaredInHierarchy(WithDefaults.class, method -> true);

		assertThat(namesOf(methods), containsInAnyOrder("defaultMethod", "staticMethod"));
	}

	@Test
	void shouldReturnMethodsDeclaredByObject() {
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(Methods.Safe.getAllDeclaredInHierarchy(Object.class), hasSize(sizeObject));
		assertThat(Methods.Safe.getAllDeclaredInHierarchy(Object.class, method -> true), hasSize(sizeObject));
	}

	@ParameterizedTest
	@MethodSource("provideClassesForParity")
	void shouldMatchMethodsGetAllDeclaredInHierarchy(final Class<?> cls) {
		assertThat(Methods.Safe.getAllDeclaredInHierarchy(cls), equalTo(Methods.getAllDeclaredInHierarchy(cls)));
	}

	@ParameterizedTest
	@MethodSource("provideClassesForParity")
	void shouldMatchMethodsGetAllDeclaredInHierarchyWithPredicate(final Class<?> cls) {
		assertThat(Methods.Safe.getAllDeclaredInHierarchy(cls, method -> true),
				equalTo(Methods.getAllDeclaredInHierarchy(cls, method -> true)));
	}

	private static Stream<Arguments> provideClassesForParity() {
		return Stream.of(
				Arguments.of(B.class),
				Arguments.of(C.class),
				Arguments.of(E.class),
				Arguments.of(WithDefaults.class),
				Arguments.of(Object.class));
	}

	private static List<String> namesOf(List<Method> methods) {
		return methods.stream()
				.filter(method -> !method.isSynthetic())
				.map(Method::getName)
				.toList();
	}
}
