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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Methods.Safe#getGenericReturnType(Method, int)}. It returns null instead of throwing when the
 * method is null, when the return type is not generic or when the index is out of bounds.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsSafeGetGenericReturnTypeTest {

	public static class A {

		public List<String> getList1() {
			return null;
		}

		public List<List<String>> getList2() {
			return null;
		}

		@SuppressWarnings("rawtypes")
		public List getList3() {
			return null;
		}
	}

	@Test
	void shouldReturnCorrectType() throws Exception {
		Method method = A.class.getMethod("getList1");

		Type type = Methods.Safe.getGenericReturnType(method, 0);

		assertThat(type, equalTo(String.class));
	}

	@Test
	void shouldReturnNullForIndexTooHigh() throws Exception {
		Method method = A.class.getMethod("getList1");

		Type type = Methods.Safe.getGenericReturnType(method, 1);

		assertThat(type, nullValue());
	}

	@Test
	void shouldReturnNullForNegativeIndex() throws Exception {
		Method method = A.class.getMethod("getList1");

		Type type = Methods.Safe.getGenericReturnType(method, -1);

		assertThat(type, nullValue());
	}

	@Test
	void shouldReturnNullForRawTypes() throws Exception {
		Method method = A.class.getMethod("getList3");

		Type type = Methods.Safe.getGenericReturnType(method, 0);

		assertThat(type, nullValue());
	}

	@Test
	void shouldReturnNullForNullMethod() {
		Type type = Methods.Safe.getGenericReturnType(null, 0);

		assertThat(type, nullValue());
	}

	@Test
	void shouldThrowClassCastExceptionOnFailToCastFromParameterizedClass() throws Exception {
		Method method = A.class.getMethod("getList2");

		assertThrows(ClassCastException.class, () -> {
			@SuppressWarnings("unused")
			Class<?> cls = Methods.Safe.getGenericReturnType(method, 0);
		});
	}
}
