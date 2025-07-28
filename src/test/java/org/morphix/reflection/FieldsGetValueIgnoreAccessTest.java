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

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields#get(Object, Field)}.
 *
 * @author Radu Sebastian LAZIN
 */
public class FieldsGetValueIgnoreAccessTest {

	public static final String TEST_STRING = "testString";
	public static final Long TEST_LONG = 17L;
	public static final Integer TEST_INTEGER = 13;

	public static class A {
		public String s;
		protected Long l;
		@SuppressWarnings("unused")
		private Integer i;
	}

	@Test
	void shouldIgnoreAccessOnReturningTheValue() throws Exception {
		A a = new A();
		a.s = TEST_STRING;
		a.l = TEST_LONG;
		a.i = TEST_INTEGER;

		String s = Fields.IgnoreAccess.get(a, A.class.getDeclaredField("s"));
		Long l = Fields.IgnoreAccess.get(a, A.class.getDeclaredField("l"));
		Integer i = Fields.IgnoreAccess.get(a, A.class.getDeclaredField("i"));

		assertThat(s, equalTo(TEST_STRING));
		assertThat(l, equalTo(TEST_LONG));
		assertThat(i, equalTo(TEST_INTEGER));
	}

	@Test
	void shouldThrowExceptionOnInvalidField() throws Exception {
		Object o = new Object();
		Field s = A.class.getDeclaredField("s");
		assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.get(o, s));
	}

}
