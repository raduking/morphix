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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields#getStatic(Class, String)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsGetStaticFieldsTest {

	private static final String FIELD_VALUE = "aaa";

	@Test
	void shouldReturnStaticFieldValue() {
		String staticField = Fields.IgnoreAccess.getStatic(A.class, "STATIC_FIELD");

		assertThat(staticField, equalTo(FIELD_VALUE));
	}

	@Test
	void shouldThrowErrorIfFieldNotFound() {
		assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.getStatic(A.class, "wrongName"));

	}

	private static class A {
		@SuppressWarnings("unused")
		public static final String STATIC_FIELD = FIELD_VALUE;
	}

	private static class B extends A {
		// empty
	}

	@Test
	void shouldReturnStaticFieldValueFromDerivedClass() {
		String staticField = Fields.IgnoreAccess.getStatic(B.class, "STATIC_FIELD");

		assertThat(staticField, equalTo(FIELD_VALUE));
	}
}
