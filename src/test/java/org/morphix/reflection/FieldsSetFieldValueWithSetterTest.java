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
 * Test class for {@link Fields#get(Object, String)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsSetFieldValueWithSetterTest {

	public static class A {
		Integer x;
		Integer y;

		public void setX(@SuppressWarnings("unused") final Integer x) {
			this.x = 4;
		}
	}

	public static class B extends A {
		// empty
	}

	@Test
	void shouldSetFieldValueBySetter() {
		A a = new A();
		Fields.set(a, "x", 2);

		assertThat(a.x, equalTo(4));
	}

	@Test
	void shouldSetFieldValueByField() {
		A a = new A();
		Fields.set(a, "y", 3);

		assertThat(a.y, equalTo(3));
	}

	@Test
	void shouldSetFieldValueBySetterInHierarchy() {
		B a = new B();
		Fields.set(a, "x", 2);

		assertThat(a.x, equalTo(4));
	}

	@Test
	void shouldSetFieldValueByFieldInHierarchy() {
		B a = new B();
		Fields.set(a, "y", 3);

		assertThat(a.y, equalTo(3));
	}

	@Test
	void shouldThrowExceptionIfFieldDoesNotExist() {
		A a = new A();
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.set(a, "z", 3));
		assertThat(e.getMessage(), equalTo("Object does not contain a field named: z"));
	}

}
