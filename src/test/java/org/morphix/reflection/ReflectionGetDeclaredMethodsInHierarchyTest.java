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
import static org.hamcrest.Matchers.hasSize;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Methods#getDeclaredMethodsInHierarchy(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionGetDeclaredMethodsInHierarchyTest {

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
		List<Method> fields = Methods.getDeclaredMethodsInHierarchy(B.class);

		int sizeB = B.class.getDeclaredMethods().length;
		int sizeA = A.class.getDeclaredMethods().length;

		assertThat(fields, hasSize(sizeA + sizeB));
	}

	@Test
	void shouldReturnEmptyListForClassesWithNoMethods() {
		List<Method> fields = Methods.getDeclaredMethodsInHierarchy(C.class);

		int size = C.class.getDeclaredMethods().length;

		assertThat(fields, hasSize(size));
	}

	@Test
	void shouldReturnEnumClassMethodsListForEmptyEnumsToo() {
		List<Method> fields = Methods.getDeclaredMethodsInHierarchy(E.class);

		int sizeEnum = Enum.class.getDeclaredMethods().length;
		int sizeE = E.class.getDeclaredMethods().length;

		assertThat(fields, hasSize(sizeEnum + sizeE));
	}

}
