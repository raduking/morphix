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

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields#getDeclaredFieldInHierarchy(Class, String)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsGetDeclaredFieldInHierarchyTest {

	public enum E {
		// empty enum
	}

	public static class A {
		int x;
	}

	public static class B extends A {
		int y;
	}

	public static class C {
		// empty class
	}

	@Test
	void shouldGetFieldInHierarchy() throws Exception {
		Field expected = A.class.getDeclaredField("x");
		Field field = Fields.getDeclaredFieldInHierarchy(B.class, "x");

		assertThat(field, equalTo(expected));
	}

	@Test
	void shouldReturnNullIfFieldNotFound() {
		Field field = Fields.getDeclaredFieldInHierarchy(C.class, "$NonExistingField$");

		assertThat(field, equalTo(null));

		field = Fields.getDeclaredFieldInHierarchy(B.class, "$NonExistingField$");

		assertThat(field, equalTo(null));
	}

	@Test
	void shouldReturnNullIfClassIsNull() {
		Field field = Fields.getDeclaredFieldInHierarchy(null, "x");

		assertThat(field, equalTo(null));
	}

	@Test
	void shouldReturnFieldInHierarchyOnObject() throws Exception {
		Object o = new B();

		Field expected = A.class.getDeclaredField("x");
		Field field = Fields.getDeclaredFieldInHierarchy(o, "x");

		assertThat(field, equalTo(expected));
	}
}
