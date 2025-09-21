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
 * Test class for {@link Fields#getOneDeclaredInHierarchy(Class, String)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsGetOneDeclaredTest {

	public enum E {
		// empty enum
	}

	public static class A {
		int x;
	}

	public static class C {
		// empty class
	}

	@Test
	void shouldGetField() throws Exception {
		Field expected = A.class.getDeclaredField("x");
		Field field = Fields.getOneDeclared(A.class, "x");

		assertThat(field, equalTo(expected));
	}

	@Test
	void shouldReturnNullIfFieldNotFound() {
		Field field = Fields.getOneDeclared(C.class, "$NonExistingField$");

		assertThat(field, equalTo(null));
	}

	@Test
	void shouldReturnNullIfClassIsNull() {
		Field field = Fields.getOneDeclared((Class<?>) null, "x");

		assertThat(field, equalTo(null));
	}

	@Test
	void shouldReturnNullIfObjectIsNull() {
		Field field = Fields.getOneDeclared((Object) null, "x");

		assertThat(field, equalTo(null));
	}

	@Test
	void shouldReturnNullIfFieldNameIsNull() {
		Field field = Fields.getOneDeclared(A.class, null);

		assertThat(field, equalTo(null));
	}

	@Test
	void shouldReturnNullIfFieldNameIsNullFromObject() {
		Field field = Fields.getOneDeclared(new Object(), null);

		assertThat(field, equalTo(null));
	}

	@Test
	void shouldReturnFieldOnObject() throws Exception {
		Field expected = A.class.getDeclaredField("x");
		Field field = Fields.getOneDeclaredInHierarchy(new A(), "x");

		assertThat(field, equalTo(expected));
	}
}
