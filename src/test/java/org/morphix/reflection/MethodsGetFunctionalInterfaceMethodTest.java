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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Methods#getFunctionalInterfaceMethod(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsGetFunctionalInterfaceMethodTest {

	public static interface A {
		void foo();
	}

	public abstract static class B implements A {
		// empty
	}

	@Test
	void shouldReturnTheFunctionalInterfaceMethod() {
		Method method = Methods.getFunctionalInterfaceMethod(A.class);

		assertThat(method.getName(), equalTo("foo"));
	}

	@Test
	void shouldReturnTheFunctionalInterfaceMethodFromDerivedClass() {
		Method method = Methods.getFunctionalInterfaceMethod(B.class);

		assertThat(method.getName(), equalTo("foo"));
	}

	public static interface C {
		void foo();

		void goo();
	}

	@Test
	void shouldThrowExceptionIfTheClassIsNotAFunctionalInterfaceHasMoreThanOneAbstractMethod() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.getFunctionalInterfaceMethod(C.class));

		assertThat(e.getMessage(), equalTo(C.class + " is not a functional interface because it has more than one abstract method"));
	}

	public static class D {
		// empty
	}

	@Test
	void shouldThrowExceptionIfTheClassIsNotAFunctionalInterfaceHasNoAbstractMethods() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.getFunctionalInterfaceMethod(D.class));

		assertThat(e.getMessage(), equalTo(D.class + " is not a functional interface because it has no abstract method"));
	}

}
