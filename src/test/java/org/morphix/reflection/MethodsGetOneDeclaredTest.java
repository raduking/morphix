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
import static org.hamcrest.Matchers.nullValue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Methods#getOneDeclared(String, Class, Class...)}. This method returns null instead of throwing
 * when the method is not found, exactly like its {@link Methods.Safe} counterpart it delegates to.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsGetOneDeclaredTest {

	private static final String NON_EXISTENT_METHOD = "$NonExistentMethod$";

	public static class A {

		int getX() {
			return 0;
		}

		int plus(final int y) {
			return y;
		}
	}

	public static class C {
		// empty class
	}

	@Test
	void shouldGetMethod() throws NoSuchMethodException {
		Method expected = A.class.getDeclaredMethod("getX");

		Method method = Methods.getOneDeclared("getX", A.class);

		assertThat(method, equalTo(expected));
	}

	@Test
	void shouldGetMethodWithParams() throws NoSuchMethodException {
		Method expected = A.class.getDeclaredMethod("plus", int.class);

		Method method = Methods.getOneDeclared("plus", A.class, int.class);

		assertThat(method, equalTo(expected));
	}

	@Test
	void shouldReturnNullIfMethodNotFound() {
		Method method = Methods.getOneDeclared(NON_EXISTENT_METHOD, A.class);

		assertThat(method, nullValue());
	}

	@Test
	void shouldReturnNullIfMethodNotFoundWithDifferentParams() {
		Method method = Methods.getOneDeclared("plus", A.class);

		assertThat(method, nullValue());
	}

	@Test
	void shouldReturnNullIfClassIsNull() {
		Method method = Methods.getOneDeclared("getX", (Class<?>) null);

		assertThat(method, nullValue());
	}

	@Test
	void shouldReturnNullIfMethodNameIsNull() {
		Method method = Methods.getOneDeclared(null, A.class);

		assertThat(method, nullValue());
	}

	@Test
	void shouldNotSearchInTheHierarchy() {
		Method method = Methods.getOneDeclared("toString", A.class);

		assertThat(method, nullValue());
	}
}
