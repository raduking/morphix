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
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Methods#getGenericReturnClass(Method, int)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsGetGenericReturnClassTest {

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
	void shouldReturnCorrectClass() throws Exception {
		Method method = A.class.getMethod("getList1");
		Class<?> cls = Methods.getGenericReturnClass(method, 0);

		assertThat(cls, equalTo(String.class));
	}

	@Test
	void shouldFailToCastFromParameterizedClass() throws Exception {
		Method method = A.class.getMethod("getList2");
		assertThrows(ReflectionException.class, () -> Methods.getGenericReturnClass(method, 0));
	}

	@Test
	void shouldFailForRawTypes() throws Exception {
		Method method = A.class.getMethod("getList3");
		assertThrows(ReflectionException.class, () -> Methods.getGenericReturnClass(method, 0));
	}

}
