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
 * Test class for {@link Fields#setStatic(Class, String, Object)}
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionSetStaticFieldIgnoreAccessTest {

	private static final String MISSING_FIELD_NAME = "missingField";
	private static final String STATIC_FIELD_NAME = "staticField";
	private static final String VALUE = "someValue";

	public static class B {
		private static String staticField = null;

		public static String getStaticField() {
			return staticField;
		}
	}

	@Test
	void shouldSetStaticFieldIgnoringAccess() {
		Fields.IgnoreAccess.setStatic(B.class, STATIC_FIELD_NAME, VALUE);

		String result = B.getStaticField();
		assertThat(result, equalTo(VALUE));
	}

	@Test
	void shouldThrowExceptionForFieldThatDoesNotExist() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.setStatic(B.class, MISSING_FIELD_NAME, VALUE));
		assertThat(e.getMessage(),
				equalTo("Could not find static field with name " + MISSING_FIELD_NAME + " on class " + B.class));
	}

}
