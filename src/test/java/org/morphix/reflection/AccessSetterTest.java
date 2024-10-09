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

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link AccessSetter}.
 *
 * @author Radu Sebastian LAZIN
 */
class AccessSetterTest {

	public static class A {

		public String foo(final String s) {
			return s;
		}

	}

	@Test
	void shouldReturnFalseWhenSetAccessibleFails() throws Exception {
		MethodHandle handle = MethodHandles.lookup().findVirtual(A.class, "foo", MethodType.methodType(String.class, String.class));

		AccessSetter<?> accessSetter = AccessSetter.ofOverride(handle);
		boolean result = accessSetter.setAccessible(null, false);

		assertFalse(result);
	}

}
