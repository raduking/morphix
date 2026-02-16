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

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Methods#getAllDeclared(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsGetAllDeclaredTest {

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
		List<Method> methods = Methods.getAllDeclared(B.class);

		Method[] methodsB = B.class.getDeclaredMethods();
		int sizeB = B.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeB));

		for (int i = 0; i < sizeB; ++i) {
			assertThat(methods.get(i), equalTo(methodsB[i]));
		}
	}

	@Test
	void shouldReturnEmptyListForClassesWithNoMethods() {
		List<Method> methods = Methods.getAllDeclared(C.class);

		Method[] methodsC = C.class.getDeclaredMethods();
		int size = C.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(size));

		for (int i = 0; i < size; ++i) {
			assertThat(methods.get(i), equalTo(methodsC[i]));
		}
	}

	@Test
	void shouldReturnEnumClassMethodsListForEmptyEnumsToo() {
		List<Method> methods = Methods.getAllDeclared(E.class);

		Method[] methodsE = E.class.getDeclaredMethods();
		int sizeE = E.class.getDeclaredMethods().length;

		assertThat(methods, hasSize(sizeE));

		for (int i = 0; i < sizeE; ++i) {
			assertThat(methods.get(i), equalTo(methodsE[i]));
		}
	}

}
