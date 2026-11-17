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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.morphix.lang.function.Predicates;
import org.morphix.reflection.predicates.MemberPredicates;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Methods.Unchecked#getAllDeclaredInHierarchy(Class)}</li>
 * <li>{@link Methods.Unchecked#getAllDeclaredInHierarchy(Class, java.util.function.Predicate)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsUncheckedGetAllDeclaredInHierarchyTest {

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

	public static class F extends B {
		void fooF() {
			// empty
		}
	}

	public interface WithDefaults {
		default String defaultMethod() {
			return "default";
		}

		static String staticMethod() {
			return "static";
		}
	}

	public record R(int x) {
		// record class
	}

	public static class D {
		@Deprecated
		void fooD() {
			// empty
		}

		void barD() {
			// empty
		}
	}

	@Test
	void shouldReturnAllMethodsInHierarchy() {
		List<Method> methods = Methods.Unchecked.getAllDeclaredInHierarchy(B.class);

		int sizeB = B.class.getDeclaredMethods().length;
		int sizeA = A.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeA + sizeB + sizeObject));
	}

	@Test
	void shouldReturnMethodsForClassesWithNoMethods() {
		List<Method> methods = Methods.Unchecked.getAllDeclaredInHierarchy(C.class);

		int size = C.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(size + sizeObject));
	}

	@Test
	void shouldReturnEnumClassMethodsListForEmptyEnumsToo() {
		List<Method> methods = Methods.Unchecked.getAllDeclaredInHierarchy(E.class);

		int sizeEnum = Enum.class.getDeclaredMethods().length;
		int sizeE = E.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeEnum + sizeE + sizeObject));
	}

	@Test
	void shouldReturnMethodsForRecords() {
		List<Method> methods = Methods.Unchecked.getAllDeclaredInHierarchy(R.class);

		int sizeRecord = Record.class.getDeclaredMethods().length;
		int sizeR = R.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeRecord + sizeR + sizeObject));
	}

	@Test
	void shouldThrowNullPointerExceptionForNullClass() {
		assertThrows(NullPointerException.class, () -> Methods.Unchecked.getAllDeclaredInHierarchy((Class<?>) null));
	}

	@Test
	void shouldReturnAllMethodsInExpectedHierarchyOrder() {
		List<Method> result = Methods.Unchecked.getAllDeclaredInHierarchy(F.class);

		List<Method> expected = new ArrayList<>();
		expected.addAll(Methods.Unchecked.getAllDeclared(F.class));
		expected.addAll(Methods.Unchecked.getAllDeclared(B.class));
		expected.addAll(Methods.Unchecked.getAllDeclared(A.class));
		expected.addAll(Methods.Unchecked.getAllDeclared(Object.class));

		assertThat(result, equalTo(expected));

		for (Iterator<Method> methodsIt = result.iterator(), expectedIt = expected.iterator(); methodsIt.hasNext();) {
			Method resultMethod = methodsIt.next();
			Method expectedMethod = expectedIt.next();
			assertThat(resultMethod, equalTo(expectedMethod));
		}
	}

	@Test
	void shouldReturnTheOwnMethodsOfAnInterface() {
		List<Method> methods = Methods.Unchecked.getAllDeclaredInHierarchy(WithDefaults.class);

		assertThat(namesOf(methods), containsInAnyOrder("defaultMethod", "staticMethod"));
	}

	@Test
	void shouldReturnMethodsDeclaredByObject() {
		assertThat(Methods.Unchecked.getAllDeclaredInHierarchy(Object.class),
				hasSize(Object.class.getDeclaredMethods().length));
	}

	@ParameterizedTest
	@MethodSource("provideClassesForCheckingNonNullClass")
	void shouldMatchMethodsGetAllDeclaredInHierarchyForNonNullClass(final Class<?> cls) {
		assertThat(Methods.Unchecked.getAllDeclaredInHierarchy(cls), equalTo(Methods.getAllDeclaredInHierarchy(cls)));
	}

	private static Stream<Arguments> provideClassesForCheckingNonNullClass() {
		return Stream.of(
				Arguments.of(B.class),
				Arguments.of(C.class),
				Arguments.of(E.class),
				Arguments.of(R.class),
				Arguments.of(F.class),
				Arguments.of(WithDefaults.class),
				Arguments.of(Object.class));
	}

	@Test
	void shouldReturnMethodsWithAnnotation() throws Exception {
		Method fooD = D.class.getDeclaredMethod("fooD");
		Predicate<Method> predicate = MemberPredicates.withAnnotation(Deprecated.class);

		List<Method> annotatedMethods = Methods.Unchecked.getAllDeclaredInHierarchy(D.class, predicate);

		assertThat(annotatedMethods, equalTo(Methods.getAllDeclaredInHierarchy(D.class, predicate)));
		assertThat(annotatedMethods, hasItem(fooD));
	}

	@Test
	void shouldReturnTheOwnMethodsOfAnInterfaceWithPredicate() {
		List<Method> methods = Methods.Unchecked.getAllDeclaredInHierarchy(WithDefaults.class, method -> true);

		assertThat(namesOf(methods), containsInAnyOrder("defaultMethod", "staticMethod"));
	}

	@Test
	void shouldThrowNullPointerExceptionForNullPredicate() {
		assertThrows(NullPointerException.class, () -> Methods.Unchecked.getAllDeclaredInHierarchy(D.class, null));
	}

	@Test
	void shouldThrowNullPointerExceptionForNullClassWithPredicate() {
		assertThrows(NullPointerException.class, () -> Methods.Unchecked.getAllDeclaredInHierarchy((Class<?>) null, method -> true));
	}

	@Test
	void shouldApplyPredicateWhenTheClassHasNoSuperclass() {
		List<Method> all = Methods.Unchecked.getAllDeclaredInHierarchy(WithDefaults.class);
		List<Method> none = Methods.Unchecked.getAllDeclaredInHierarchy(WithDefaults.class, method -> false);

		assertThat(none, hasSize(0));
		assertThat(all, hasSize(WithDefaults.class.getDeclaredMethods().length));
	}

	@Test
	void shouldMatchMethodsGetAllDeclaredInHierarchyWithPredicate() {
		Predicate<Method> predicate = MemberPredicates.withAnnotation(Deprecated.class);

		assertThat(Methods.Unchecked.getAllDeclaredInHierarchy(D.class, predicate),
				equalTo(Methods.getAllDeclaredInHierarchy(D.class, predicate)));
	}

	@ParameterizedTest
	@MethodSource("provideClassesForCheckingWithPredicate")
	void shouldMatchMethodsGetAllDeclaredInHierarchyWithPredicate(final Class<?> cls) {
		assertThat(Methods.Unchecked.getAllDeclaredInHierarchy(cls, Predicates.acceptAll()),
				equalTo(Methods.getAllDeclaredInHierarchy(cls, Predicates.acceptAll())));
	}

	private static Stream<Arguments> provideClassesForCheckingWithPredicate() {
		return Stream.of(Arguments.of(B.class), Arguments.of(WithDefaults.class), Arguments.of(Object.class));
	}

	private static List<String> namesOf(List<Method> methods) {
		return methods.stream()
				.filter(method -> !method.isSynthetic())
				.map(Method::getName)
				.toList();
	}
}
