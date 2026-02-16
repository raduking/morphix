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
 * Test class for {@link Methods#toSimpleString(Method)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsToSimpleStringTest {

	@Test
	void shouldConvertToString() throws Exception {
		Method method = A.class.getDeclaredMethod("foo", String.class, int.class);

		String result = Methods.toSimpleString(method);

		assertThat(result, equalTo("foo(java.lang.String, int)"));
	}

	public static class A {

		public String foo(final String s, final int y) {
			return s + y;
		}

	}

}
