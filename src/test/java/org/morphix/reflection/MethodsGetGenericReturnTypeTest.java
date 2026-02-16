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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Methods#getGenericReturnType(Method, int)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsGetGenericReturnTypeTest {

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
		Type cls = Methods.getGenericReturnType(method, 0);

		assertThat(cls, equalTo(String.class));
	}

	@Test
	void shouldReturnParameterizedTypeFromParameterizedClass() throws Exception {
		Method method = A.class.getMethod("getList2");
		Type type = Methods.getGenericReturnType(method, 0);

		assertThat(type, instanceOf(ParameterizedType.class));
	}

	@Test
	void shouldFailForRawTypes() throws Exception {
		Method method = A.class.getMethod("getList3");
		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.getGenericReturnType(method, 0));

		assertThat(e.getMessage(),
				equalTo(method.getGenericReturnType().getTypeName() + " is a raw return type for method "
						+ method.getDeclaringClass().getCanonicalName()
						+ "." + method.getName()));
	}

}
