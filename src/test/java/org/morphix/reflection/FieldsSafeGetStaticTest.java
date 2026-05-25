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

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Fields.Safe#getStatic(Class, String)}.</li>
 * <li>{@link Fields.Safe#getStatic(Field)}</li>
 * <li>{@link Fields.Safe#get(Class, String)}. where it follows the static field path</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsSafeGetStaticTest {

	private static final String GOOD_NAME = "s";
	private static final String WRONG_NAME = "wrongName";
	private static final String FIELD_VALUE = "aaa";

	@Test
	void shouldReturnStaticFieldValue() {
		String staticField = Fields.Safe.getStatic(A.class, "STATIC_FIELD");

		assertThat(staticField, equalTo(FIELD_VALUE));
	}

	@Test
	void shouldReturnStaticFieldValueWithGet() {
		String staticField = Fields.Safe.get(A.class, "STATIC_FIELD");

		assertThat(staticField, equalTo(FIELD_VALUE));
	}

	@Test
	void shouldReturnNullIfFieldNotFound() {
		Object result = Fields.Safe.getStatic(A.class, WRONG_NAME);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullIfFieldIsNotStatic() {
		Object result = Fields.Safe.getStatic(A.class, GOOD_NAME);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullIfFieldIsNotStaticWithGet() {
		Object result = Fields.Safe.get(A.class, GOOD_NAME);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullIfFieldNameIsNull() {
		Object result = Fields.Safe.getStatic(A.class, null);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullIfClassIsNull() {
		Object result = Fields.Safe.getStatic(null, GOOD_NAME);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullIfFieldIsNull() {
		Object result = Fields.Safe.getStatic(null);

		assertThat(result, equalTo(null));
	}

	private static class A {
		@SuppressWarnings("unused")
		public static final String STATIC_FIELD = FIELD_VALUE;

		@SuppressWarnings("unused")
		public String s;
	}

	private static class B extends A {
		// empty
	}

	@Test
	void shouldReturnStaticFieldValueFromDerivedClass() {
		String staticField = Fields.Safe.getStatic(B.class, "STATIC_FIELD");

		assertThat(staticField, equalTo(FIELD_VALUE));
	}
}
