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

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Methods.Safe#getOneDeclared(String, Class, Class...)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsSafeGetOneDeclaredTest {

	public static class A {
		int x;

		int getX() {
			return x;
		}

		int plus(final int y) {
			return x + y;
		}
	}

	public static class C {
		// empty class
	}

	@Test
	void shouldGetMethod() throws Exception {
		Method expected = A.class.getDeclaredMethod("getX");
		Method method = Methods.Safe.getOneDeclared("getX", A.class);

		assertThat(method, equalTo(expected));
	}

	@Test
	void shouldGetMethodWithParams() throws Exception {
		Method expected = A.class.getDeclaredMethod("plus", int.class);
		Method method = Methods.Safe.getOneDeclared("plus", A.class, int.class);

		assertThat(method, equalTo(expected));
	}

	@Test
	void shouldGetMethodWithParamsWithObjectIfObjectIsClass() throws Exception {
		Method expected = A.class.getDeclaredMethod("plus", int.class);
		Object obj = A.class;
		Method method = Methods.Safe.getOneDeclared("plus", obj, int.class);

		assertThat(method, equalTo(expected));
	}

	@Test
	void shouldReturnNullIfMethodNotFound() {
		Method method = Methods.Safe.getOneDeclared("$NonExistingMethod$", C.class);

		assertThat(method, equalTo(null));
	}

	@Test
	void shouldReturnNullIfMethodNotFoundWithDifferentParams() {
		Method method = Methods.Safe.getOneDeclared("plus", A.class);

		assertThat(method, equalTo(null));
	}

	@Test
	void shouldReturnNullIfClassIsNull() {
		Method method = Methods.Safe.getOneDeclared("getX", (Class<?>) null);

		assertThat(method, equalTo(null));
	}

	@Test
	void shouldReturnNullIfObjectIsNull() {
		Method method = Methods.Safe.getOneDeclared("plus", (Object) null, int.class);

		assertThat(method, equalTo(null));
	}

	@Test
	void shouldReturnNullIfMethodNameIsNull() {
		Method method = Methods.Safe.getOneDeclared(null, A.class);

		assertThat(method, equalTo(null));
	}

	@Test
	void shouldReturnNullIfMethodNameIsNullFromObject() {
		Method method = Methods.Safe.getOneDeclared(null, new Object());

		assertThat(method, equalTo(null));
	}

	@Test
	void shouldReturnMethodOnObject() throws Exception {
		Method expected = A.class.getDeclaredMethod("getX");
		Method method = Methods.Safe.getOneDeclared("getX", new A());

		assertThat(method, equalTo(expected));
	}
}
