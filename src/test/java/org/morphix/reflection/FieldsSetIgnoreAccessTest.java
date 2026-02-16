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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.A;

/**
 * Test class for {@link Fields#set(Object, String, Object)} and {@link Fields#set(Object, Field, Object)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsSetIgnoreAccessTest {

	private static final String MISSING_FIELD_NAME = "missingField";
	private static final String FIELD_NAME = "field";
	private static final String VALUE = "someValue";

	@Test
	void shouldSetIgnoreAccess() {
		A object = new A();
		Fields.IgnoreAccess.set(object, FIELD_NAME, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
	}

	@Test
	void shouldKeepTheFieldModifierOnSetIgnoreAccess() throws Exception {
		A object = new A();
		Field field = A.class.getDeclaredField(FIELD_NAME);
		Fields.IgnoreAccess.set(object, field, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
		assertFalse(field.canAccess(object));
	}

	@Test
	void shouldThrowExceptionForFieldThatDoesNotExist() {
		A object = new A();
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.set(object, MISSING_FIELD_NAME, VALUE));
		assertThat(e.getMessage(), equalTo("Could not find field '" + MISSING_FIELD_NAME + "' on object of type " + object.getClass()));
	}

	@Test
	void shouldThrowExceptionIfBadValueIsSet() {
		A object = new A();
		Object other = new Object();
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.set(object, FIELD_NAME, other));
		assertThat(e.getMessage(), equalTo("Could not set field " + FIELD_NAME));
	}

	static class B extends A {
		// empty
	}

	@Test
	void shouldSetIgnoreAccessOnBaseClassField() {
		B object = new B();
		Fields.IgnoreAccess.set(object, FIELD_NAME, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
	}

	static class D {

		private static final String FIELD = init();

		private static String init() {
			return "test";
		}

		private final String field = VALUE;

		public String getField() {
			return field;
		}
	}

	@Test
	void shouldSetIgnoreAccessOnFinalField() {
		D object = new D();
		Fields.IgnoreAccess.set(object, FIELD_NAME, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
	}

	@Test
	void shouldSetIgnoreAccessOnFinalFieldWithUnsafe() throws NoSuchFieldException, SecurityException {
		D object = new D();
		Field field = D.class.getDeclaredField(FIELD_NAME);
		Fields.Unsafe.set(object, field, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
	}

	@Test
	void shouldSetIgnoreAccessOnStaticFinalField() {
		Fields.IgnoreAccess.setStatic(D.class, "FIELD", VALUE);

		assertThat(D.FIELD, equalTo(VALUE));
	}
}
