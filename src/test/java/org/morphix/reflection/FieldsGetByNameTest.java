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

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.A;
import org.morphix.reflection.testdata.B;

/**
 * Test class for {@link Fields#get(Object, String)}. Unlike its {@link Fields.Safe} counterpart it retrieves the field
 * value by its getter method if it has one, and it throws instead of returning null when the field is not found.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsGetByNameTest {

	private static final String NON_EXISTENT_FIELD = "$NonExistentField$";

	private static final String TEST_STRING = "testString";

	@Test
	void shouldRetrieveFieldValueByGetter() {
		Integer result = Fields.get(new D(), "x");

		assertThat(result, equalTo(2));
	}

	@Test
	void shouldRetrieveFieldValueByField() {
		Integer result = Fields.get(new D(), "y");

		assertThat(result, equalTo(3));
	}

	@Test
	void shouldRetrieveFieldValueByFieldWhenTheGetterReturnsSomethingElse() {
		Integer result = Fields.get(new G(), "z");

		assertThat(result, equalTo(8));
	}

	@Test
	void shouldThrowExceptionIfFieldIsNotFound() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.get(new D(), NON_EXISTENT_FIELD));

		assertThat(e.getMessage(), equalTo("Object does not contain a field named: " + NON_EXISTENT_FIELD));
	}

	@Test
	void shouldThrowExceptionIfFieldIsNotFoundInHierarchy() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.get(new E(), NON_EXISTENT_FIELD));

		assertThat(e.getMessage(), equalTo("Object does not contain a field named: " + NON_EXISTENT_FIELD));
	}

	@Test
	void shouldRetrieveFieldValueInHierarchy() {
		E e = new E();
		e.b = Boolean.TRUE;

		Boolean result = Fields.get(e, "b");

		assertThat(result, equalTo(Boolean.TRUE));
	}

	@Test
	void shouldRetrieveFieldValueInHierarchyWhenTheFieldIsDeclaredInTheSuperClass() {
		F f = new F();
		f.s = TEST_STRING;

		String result = Fields.get(f, "s");

		assertThat(result, equalTo(TEST_STRING));
	}

	public static class D {
		Integer x = 2;
		Integer y = 3;

		public Integer getX() {
			return x;
		}
	}

	public static class G {
		public Integer z = 7;

		public Integer getZ() {
			return 8;
		}
	}

	public static class E extends A {
		// empty
	}

	public static class F extends B {
		// empty
	}
}
