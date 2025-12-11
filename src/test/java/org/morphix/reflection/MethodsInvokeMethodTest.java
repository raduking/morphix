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
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.testdata.A;

/**
 * Test class for {@link Methods#invoke(Method, Object, Object...)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsInvokeMethodTest {

	private static final String TEST_STRING = "Test";

	public static class StaticA {
		static String s;

		public static void foo(final String s) {
			StaticA.s = s;
		}
	}

	@Test
	void shouldInvokeMethod() throws Exception {
		A obj = new A();

		Method method = A.class.getDeclaredMethod("foo", String.class);
		Methods.invoke(method, obj, TEST_STRING);

		assertThat(obj.getS(), equalTo(TEST_STRING));
	}

	@Test
	void shouldThrowConverterExceptionWhenInvokeWithInvalidArgument() throws Exception {
		A obj = new A();

		Method method = A.class.getDeclaredMethod("foo", String.class);

		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.invoke(method, obj, obj));
		assertThat(e.getMessage(),
				equalTo("Error invoking " + A.class.getCanonicalName() + "." + method.getName() + ": " + e.getCause().getMessage() + "."));
	}

	@Test
	void shouldInvokeStaticMethod() throws Exception {
		Method method = StaticA.class.getDeclaredMethod("foo", String.class);
		Methods.invoke(method, null, TEST_STRING);

		assertThat(StaticA.s, equalTo(TEST_STRING));
	}

	@Test
	void shouldInvokeClassMethod() throws Exception {
		Method method = Class.class.getDeclaredMethod("getName");
		String name = Methods.invoke(method, Class.class);

		assertThat(name, equalTo(Class.class.getName()));
	}

	@Test
	void shouldThrowReflectionExceptionWhenFailInvokeClassMethod() throws Exception {
		Method method = Class.class.getDeclaredMethod("forName", String.class);

		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.invoke(method, Class.class, "$NonExistingClass$"));
		Throwable cause = Reflection.unwrapInvocationTargetException(JavaObjects.cast(e.getCause()));

		assertThat(e.getMessage(),
				equalTo("Error invoking " + Class.class.getCanonicalName() + "." + method.getName() + ": " + cause.getMessage() + "."));
	}

	@Test
	void shouldThrowReflectionExceptionWhenFailWithBadArgumentsInvokeClassMethod() throws Exception {
		Method method = Class.class.getDeclaredMethod("forName", String.class);

		assertThrows(ReflectionException.class, () -> Methods.invoke(method, Class.class));
	}

	@Test
	void shouldThrowReflectionExceptionWhenInvokeStaticMethodWithInvalidArgument() throws Exception {
		Method method = StaticA.class.getDeclaredMethod("foo", String.class);

		assertThrows(ReflectionException.class, () -> Methods.invoke(method, null, Class.class));
	}

	public static class B {
		public void foo() {
			throw new NullPointerException();
		}
	}

	public static class StaticB {
		@SuppressWarnings("unused")
		public static void foo(final String s) {
			throw new NullPointerException();
		}
	}

	@Test
	void shouldThrowReflectionExceptionWhenInvokeFailsWithCause() throws Exception {
		B obj = new B();
		Method method = B.class.getDeclaredMethod("foo");

		assertThrows(ReflectionException.class, () -> Methods.invoke(method, obj));
	}

	@Test
	void shouldThrowReflectionExceptionWhenInvokeStaticMethodFailsWithCause() throws Exception {
		Method method = StaticB.class.getDeclaredMethod("foo", String.class);

		assertThrows(ReflectionException.class, () -> Methods.invoke(method, null, TEST_STRING));
	}

	@Test
	void shouldThrowReflectionExceptionWhenInvokingPrivateMethods() {
		A obj = new A();
		Method method = Methods.getOneDeclared("fooPrivate", A.class, String.class);

		assertThrows(ReflectionException.class, () -> Methods.invoke(method, obj, TEST_STRING));
	}
}
