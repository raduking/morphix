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
package org.morphix.invoke;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ReflectionException;
import org.morphix.utils.Tests;

/**
 * Test class for {@link Handles}.
 *
 * @author Radu Sebastian LAZIN
 */
class HandlesTest {

	private static final String SOME_STRING = "mumu";
	private static final String UNKNOWN = "unknown";

	public static class A {

		String s = SOME_STRING;

		public String getS() {
			return s;
		}

		public static String getStaticS() {
			return SOME_STRING;
		}

	}

	@Test
	void shouldInvokePublicMethodWithNoParams() throws Throwable {
		MethodHandle method = Handles.get("getS", A.class, String.class);
		A a = new A();

		String result = Handles.invoke(method, a);

		assertThat(result, equalTo(SOME_STRING));
	}

	@Test
	void shouldInvokePublicMethodWithNoParamsWithMethodHandleInvokeExact() throws Throwable {
		MethodHandle method = Handles.get("getS", A.class, String.class);
		A a = new A();

		String result = (String) method.invokeExact(a);

		assertThat(result, equalTo(SOME_STRING));
	}

	@Test
	void shouldInvokePublicStaticMethodWithNoParams() throws Throwable {
		MethodHandle method = Handles.getStatic("getStaticS", A.class, String.class);

		String result = Handles.invoke(method);

		assertThat(result, equalTo(SOME_STRING));
	}

	@Test
	void shouldInvokePublicStaticMethodWithNoParamsWithMethodHandleInvokeExact() throws Throwable {
		MethodHandle method = Handles.getStatic("getStaticS", A.class, String.class);

		String result = (String) method.invokeExact();

		assertThat(result, equalTo(SOME_STRING));
	}

	@Test
	void shouldThrowExceptionForPrivateLookupInNotOpenPackages() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Handles.getPrivateLookupIn(Function.class));

		assertThat(e.getMessage(), equalTo("Failed to get lookup for " + Function.class
				+ " because " + Function.class.getModule() + " does not open " + Function.class.getPackage()));
		assertThat(e.getCause().getClass(), equalTo(IllegalAccessException.class));
	}

	@Test
	void shouldTransformAnyThrowanleToReflectionExceptionOnInvoke() {
		MethodHandle method = Handles.getStatic("getStaticS", A.class, String.class);
		A a = new A();

		ReflectionException e = assertThrows(ReflectionException.class, () -> Handles.invoke(method, a));

		assertThat(e.getMessage(), equalTo("Error invoking method " + method));
	}

	@Test
	void shouldThrowExceptionForPrivateLookupInMethodDoesNotExist() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Handles.get(UNKNOWN, A.class, String.class));

		assertThat(e.getMessage(), equalTo("Method handle creation failed for " + A.class.getName() + "#" + UNKNOWN));
		assertThat(e.getCause().getClass(), equalTo(NoSuchMethodException.class));
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiate() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(Handles.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
