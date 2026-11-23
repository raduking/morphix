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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.constant.Constable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Methods.Complete#getAllDeclaredInHierarchy(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsCompleteGetAllDeclaredInHierarchyTest {

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

	private interface Top {
		void top();
	}

	private interface Left extends Top {
		void left();
	}

	private interface Right extends Top {
		void right();
	}

	private interface Diamond extends Left, Right {
		void diamond();
	}

	private static final class WithInterface extends A implements Top {

		@Override
		public void top() {
			// empty
		}
	}

	@Test
	void shouldGetAllMethodsInHierarchy() {
		List<Method> methods = Methods.Complete.getAllDeclaredInHierarchy(B.class);

		int sizeB = B.class.getDeclaredMethods().length;
		int sizeA = A.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeA + sizeB + sizeObject));
	}

	@Test
	void shouldKeepSuperclassBeforeInterfacesInTraversalOrder() {
		var result = Methods.Complete.getAllDeclaredInHierarchy(WithInterface.class);

		assertThat(result.stream().map(Method::getDeclaringClass).distinct().toList(),
				equalTo(List.of(WithInterface.class, A.class, Object.class, Top.class)));
	}

	@Test
	void shouldVisitSharedInterfaceOnlyOnce() {
		Set<Class<?>> excluded = Classes.mutableSetOf();

		var result = Methods.Complete.getAllDeclaredInHierarchy(Diamond.class, excluded);

		assertThat(result.stream().map(Method::getDeclaringClass).toList(),
				equalTo(List.of(Diamond.class, Left.class, Top.class, Right.class)));
		assertThat(excluded, equalTo(Set.of(Diamond.class, Left.class, Top.class, Right.class)));
	}

	@Test
	void shouldReturnEmptyListForClassesWithNoMethods() {
		List<Method> methods = Methods.Complete.getAllDeclaredInHierarchy(C.class);

		int size = C.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(size + sizeObject));
	}

	@Test
	void shouldReturnEnumClassMethodsListForEmptyEnumsToo() {
		List<Method> methods = Methods.Complete.getAllDeclaredInHierarchy(E.class);

		int sizeEnum = Enum.class.getDeclaredMethods().length;
		int sizeE = E.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;
		int sizeConstable = Constable.class.getDeclaredMethods().length;
		int sizeComparable = Comparable.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeEnum + sizeE + sizeObject + sizeConstable + sizeComparable));
	}

	@Test
	void shouldReturnEmptyListForExcludedClass() {
		List<Method> methods = Methods.Complete.getAllDeclaredInHierarchy(Object.class, Set.of(Object.class));

		assertThat(methods, hasSize(0));
	}

	@Test
	void shouldThrowExceptionForNullExcludedClasses() {
		ReflectionException exception = assertThrows(ReflectionException.class, () -> Methods.Complete.getAllDeclaredInHierarchy(Object.class, null));

		assertThat(exception.getMessage(), equalTo("Cannot traverse method hierarchy for class " + Object.class + ": excluded set is null"));
	}

	@Test
	void shouldThrowExceptionForNonModifiableExcludedClassesSet() {
		Set<Class<?>> emptySet = Set.of();
		ReflectionException exception =
				assertThrows(ReflectionException.class, () -> Methods.Complete.getAllDeclaredInHierarchy(Object.class, emptySet));

		assertThat(exception.getMessage(),
				equalTo("Cannot traverse method hierarchy for class " + Object.class + ": excluded set is unmodifiable"));
	}

	@Test
	void shouldReturnEnumClassMethodsListForEmptyEnumsTooWithExcludedInterfaces() {
		List<Method> methods = Methods.Complete.getAllDeclaredInHierarchy(E.class, Classes.mutableSetOf(Constable.class, Comparable.class));

		int sizeEnum = Enum.class.getDeclaredMethods().length;
		int sizeE = E.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeEnum + sizeE + sizeObject));
	}

	@Test
	void shouldIncludeMethodsDeclaredByObject() {
		List<Method> methods = Methods.Complete.getAllDeclaredInHierarchy(C.class);

		assertThat(methods.stream().anyMatch(method -> Object.class.equals(method.getDeclaringClass())), is(true));
	}

	@Test
	void shouldIncludeObjectMethodsInTheSimplerVariant() {
		List<Method> methods = Methods.getAllDeclaredInHierarchy(C.class);

		assertThat(methods.stream().anyMatch(method -> Object.class.equals(method.getDeclaringClass())), is(true));
	}

	@Test
	void shouldReturnEmptyListIfClassAndExcludedSetAreBothNull() {
		List<Method> methods = Methods.Complete.getAllDeclaredInHierarchy(null, null);

		assertThat(methods, empty());
	}

	@Test
	void shouldReturnEmptyListForNullClass() {
		var result = Methods.Complete.getAllDeclaredInHierarchy((Class<?>) null);

		assertThat(result, empty());
	}

}
