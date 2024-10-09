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

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Reflection#setFieldValue(Object, java.lang.reflect.Field, Object)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionSetFieldValueTest {

	public static class A {

		String a;
		String b;

		public void setA(final String a) {
			this.a = a;
		}
	}

	@Test
	void shouldUseSetterWhenSettingField() {
		A a = new A();

		Reflection.setFieldValue(a, "a", String.class, "b");

		assertThat(a.a, equalTo("b"));
	}

	@Test
	void shouldSetFieldDirectlyWhenNoSetterIsFound() {
		A a = new A();

		Reflection.setFieldValue(a, "b", String.class, "c");

		assertThat(a.b, equalTo("c"));
	}

	@Test
	void shouldNotSetFieldDirectlyWhenFieldTypeIsWrong() {
		A a = new A();

		Reflection.setFieldValue(a, "b", Integer.class, "c");

		assertThat(a.b, equalTo(null));
	}

}
