/*
 * Copyright 2025 the original author or authors.
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
import static org.hamcrest.Matchers.hasSize;
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

	@Test
	void shouldGetAllMethodsInHierarchy() {
		List<Method> methods = Methods.Complete.getAllDeclaredInHierarchy(B.class);

		int sizeB = B.class.getDeclaredMethods().length;
		int sizeA = A.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeA + sizeB + sizeObject));
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

		assertThat(exception.getMessage(), equalTo("The excluded set is null. Please provide a non null modifiable set."));
	}

	@Test
	void shouldThrowExceptionForNonModifiableExcludedClassesSet() {
		ReflectionException exception = assertThrows(ReflectionException.class, () -> Methods.Complete.getAllDeclaredInHierarchy(Object.class, Set.of()));

		assertThat(exception.getMessage(), equalTo("The excluded set is unmodifiable. Please provide a non null modifiable set."));
	}

	@Test
	void shouldReturnEnumClassMethodsListForEmptyEnumsTooWithExcludedInterfaces() {
		List<Method> methods = Methods.Complete.getAllDeclaredInHierarchy(E.class, Classes.mutableSetOf(Constable.class, Comparable.class));

		int sizeEnum = Enum.class.getDeclaredMethods().length;
		int sizeE = E.class.getDeclaredMethods().length;
		int sizeObject = Object.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeEnum + sizeE + sizeObject));
	}
}
