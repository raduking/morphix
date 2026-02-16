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
 * Test class for {@link Methods.Safe#getOneDeclaredInHierarchy(String, Class, Class...)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsSafeGetOneDeclaredInHierarchyTest {

	public enum E {
		// empty enum
	}

	public static class A {
		public void fooA() {
			// empty
		}
	}

	public static class B extends A {
		public void fooB() {
			// empty
		}
	}

	public static class C {
		// empty class
	}

	@Test
	void shouldReturnDeclaredMethodInHierarchy() throws Exception {
		Method expected = A.class.getDeclaredMethod("fooA");
		Method result = Methods.Safe.getOneDeclaredInHierarchy("fooA", B.class);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnNullIfMethodNotFound() {
		Method method = Methods.Safe.getOneDeclaredInHierarchy("$NonExistingMethod$", C.class);

		assertThat(method, equalTo(null));

		method = Methods.Safe.getOneDeclaredInHierarchy("$NonExistingMethod$", B.class);

		assertThat(method, equalTo(null));
	}

}
