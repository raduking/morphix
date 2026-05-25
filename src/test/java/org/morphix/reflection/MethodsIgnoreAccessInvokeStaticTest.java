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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.morphix.lang.Messages;

/**
 * Test class for {@link Methods.IgnoreAccess#invokeStatic(Method, Class, Object...)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsIgnoreAccessInvokeStaticTest {

	private static final String TEST_STRING = "Test";

	public static class StaticA {
		static String s;

		public static void foo(final String s) {
			StaticA.s = s;
		}

		public void goo(final String s) {
			StaticA.s = s;
		}
	}

	@Test
	void shouldInvokeStaticMethod() throws Exception {
		Method method = StaticA.class.getDeclaredMethod("foo", String.class);
		Methods.IgnoreAccess.invokeStatic(method, StaticA.class, TEST_STRING);

		assertThat(StaticA.s, equalTo(TEST_STRING));
	}

	@Test
	void shouldThrowReflectionExceptionWhenInvokingNonStaticMethod() throws Exception {
		Method method = StaticA.class.getDeclaredMethod("goo", String.class);

		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.IgnoreAccess.invokeStatic(method, StaticA.class));
		assertThat(e.getMessage(),
				equalTo(Messages.message("Method {} is not static on class: {}", method.getName(), StaticA.class)));
	}

	public static class StaticB {
		@SuppressWarnings("unused")
		public static void foo(final String s) {
			throw new NullPointerException();
		}
	}

	@Test
	void shouldThrowReflectionExceptionWhenInvokeFailsWithCause() throws Exception {
		Method method = StaticB.class.getDeclaredMethod("foo", String.class);

		assertThrows(ReflectionException.class, () -> Methods.IgnoreAccess.invokeStatic(method, StaticB.class));
	}

	@Test
	void shouldThrowReflectionExceptionWhenInvokeStaticMethodFailsWithCause() throws Exception {
		Method method = StaticB.class.getDeclaredMethod("foo", String.class);
		assertThrows(ReflectionException.class, () -> Methods.IgnoreAccess.invokeStatic(method, null, TEST_STRING));
	}
}
