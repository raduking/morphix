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
import static org.hamcrest.Matchers.nullValue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Classes.Safe#getCanonicalName(Class)}</li>
 * <li>{@link Classes.Safe#getCanonicalName(Object)}</li>
 * <li>{@link Classes.Safe#getCanonicalName(Method, Object)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class ClassesSafeGetCanonicalNameTest {

	private static final String NESTED_CANONICAL_NAME = ClassesSafeGetCanonicalNameTest.class.getCanonicalName() + ".Nested";

	private static final String STRING_CANONICAL_NAME = String.class.getCanonicalName();

	static class Nested {

		public String getValue() {
			return "value";
		}
	}

	static class WithValue {

		String value = "value";

		public String getValue() {
			return value;
		}
	}

	private static Method valueMethod() throws NoSuchMethodException {
		return Nested.class.getDeclaredMethod("getValue");
	}

	@Test
	void shouldReturnCanonicalNameOfClass() {
		assertThat(Classes.Safe.getCanonicalName(Nested.class), equalTo(NESTED_CANONICAL_NAME));
	}

	@Test
	void shouldReturnCanonicalNameOfObjectsClass() {
		assertThat(Classes.Safe.getCanonicalName(new WithValue()), equalTo(WithValue.class.getCanonicalName()));
	}

	@Test
	void shouldReturnCanonicalNameOfClassObject() {
		assertThat(Classes.Safe.getCanonicalName((Object) Nested.class), equalTo(NESTED_CANONICAL_NAME));
	}

	@Test
	void shouldReturnNullForNullClass() {
		assertThat(Classes.Safe.getCanonicalName((Class<?>) null), nullValue());
	}

	@Test
	void shouldReturnNullForNullObject() {
		assertThat(Classes.Safe.getCanonicalName((Object) null), nullValue());
	}

	@Test
	void shouldReturnDeclaringClassCanonicalNameForNullObjectAndMethod() throws NoSuchMethodException {
		assertThat(Classes.Safe.getCanonicalName(valueMethod(), null), equalTo(NESTED_CANONICAL_NAME));
	}

	@Test
	void shouldReturnNullForNullMethodAndNullObject() {
		assertThat(Classes.Safe.getCanonicalName((Method) null, null), nullValue());
	}

	@Test
	void shouldReturnObjectsClassCanonicalNameForMethodAndObject() throws NoSuchMethodException {
		assertThat(Classes.Safe.getCanonicalName(valueMethod(), new WithValue()), equalTo(WithValue.class.getCanonicalName()));
	}

	@Test
	void shouldReturnClassObjectCanonicalNameForMethodAndClassObject() throws NoSuchMethodException {
		assertThat(Classes.Safe.getCanonicalName(valueMethod(), String.class), equalTo(STRING_CANONICAL_NAME));
	}

	@Test
	void shouldIgnoreNullMethodWhenObjectIsNotNull() {
		assertThat(Classes.Safe.getCanonicalName((Method) null, new WithValue()), equalTo(WithValue.class.getCanonicalName()));
	}

	@Test
	void shouldIgnoreNullMethodWhenClassObjectIsNotNull() {
		assertThat(Classes.Safe.getCanonicalName((Method) null, String.class), equalTo(STRING_CANONICAL_NAME));
	}
}
