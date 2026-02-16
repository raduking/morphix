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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link AccessSetter}.
 *
 * @author Radu Sebastian LAZIN
 */
class AccessSetterTest {

	public static class A {

		private String x;

		public String foo(final String s) {
			return s;
		}

		public void setX(final String x) {
			this.x = x;
		}

		public String getX() {
			return x;
		}

		public static String setStaticX(final Field x, final Boolean b) {
			return x.getName() + "#" + b;
		}
	}

	@Test
	void shouldReturnFalseWhenSetAccessibleFails() throws Exception {
		MethodHandle handle = MethodHandles.lookup().findVirtual(A.class, "foo", MethodType.methodType(String.class, String.class));

		AccessSetter<?> accessSetter = AccessSetter.ofOverride(handle);
		boolean result = accessSetter.setAccessible(null, false);

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueWhenSetAccessibleSuccedes() throws Exception {
		MethodHandle handle =
				MethodHandles.lookup().findStatic(A.class, "setStaticX", MethodType.methodType(String.class, Field.class, Boolean.class));
		Field field = A.class.getDeclaredField("x");

		AccessSetter<Field> accessSetter = AccessSetter.ofOverride(handle);
		boolean result = accessSetter.setAccessible(field, true);

		assertTrue(result);
	}

}
