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
 * Test class for {@link Classes#getCanonicalName(Class)}, {@link Classes#getCanonicalName(Object)} and
 * {@link Classes#getCanonicalName(Method, Object)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClassesGetCanonicalNameTest {

	private static final String NESTED_CANONICAL_NAME = ClassesGetCanonicalNameTest.class.getCanonicalName() + ".Nested";

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

	@Test
	void shouldReturnCanonicalNameOfClass() {
		assertThat(Classes.getCanonicalName(Nested.class), equalTo(NESTED_CANONICAL_NAME));
	}

	@Test
	void shouldReturnCanonicalNameOfObjectsClass() {
		assertThat(Classes.getCanonicalName(new WithValue()), equalTo(WithValue.class.getCanonicalName()));
	}

	@Test
	void shouldReturnCanonicalNameOfClassObject() {
		assertThat(Classes.getCanonicalName((Object) Nested.class), equalTo(NESTED_CANONICAL_NAME));
	}

	@Test
	void shouldReturnNullForNullObject() {
		assertThat(Classes.getCanonicalName((Object) null), nullValue());
	}

	@Test
	void shouldReturnDeclaringClassCanonicalNameForNullObjectAndMethod() throws NoSuchMethodException {
		Method method = Nested.class.getDeclaredMethod("getValue");

		assertThat(Classes.getCanonicalName(method, null), equalTo(NESTED_CANONICAL_NAME));
	}

	@Test
	void shouldReturnObjectsClassCanonicalNameForMethodAndObject() throws NoSuchMethodException {
		Method method = Nested.class.getDeclaredMethod("getValue");

		assertThat(Classes.getCanonicalName(method, new WithValue()), equalTo(WithValue.class.getCanonicalName()));
	}

	@Test
	void shouldReturnClassObjectCanonicalNameForMethodAndClassObject() throws NoSuchMethodException {
		Method method = Nested.class.getDeclaredMethod("getValue");

		assertThat(Classes.getCanonicalName(method, String.class), equalTo(STRING_CANONICAL_NAME));
	}
}
