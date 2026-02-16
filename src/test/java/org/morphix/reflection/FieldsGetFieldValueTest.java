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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.A;

/**
 * Test class for {@link Fields#get(Object, Field)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsGetFieldValueTest {

	@Test
	void shouldReturnFieldValue() throws Exception {
		A a = new A();
		a.b = Boolean.TRUE;

		Field bField = A.class.getDeclaredField("b");
		Boolean result = Fields.get(a, bField);

		assertThat(result, equalTo(Boolean.TRUE));
	}

	@Test
	void shouldThrowExceptionIfFiledIsNotAccessible() throws Exception {
		A a = new A();

		Field field = A.class.getDeclaredField(A.FIELD_NAME);
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.get(a, field));

		assertThat(e.getMessage(), equalTo("Could not get field " + A.FIELD_NAME));
	}

}
