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
import org.morphix.reflection.testdata.B;

/**
 * Test class for {@link Fields#get(Object, Field)}.
 *
 * @author Radu Sebastian LAZIN
 */
public class FieldsGetIgnoreAccessTest {

	private static final String TEST_STRING = "testString";
	private static final Long TEST_LONG = 17L;
	private static final Integer TEST_INTEGER = 13;
	private static final String NON_EXISTENT_FIELD = "$NonExistentField$";

	@Test
	void shouldGetTheFieldValue() throws Exception {
		B b = new B();
		b.s = TEST_STRING;

		String s = Fields.IgnoreAccess.get(b, B.class.getDeclaredField("s"));

		assertThat(s, equalTo(TEST_STRING));
	}

	@Test
	void shouldAccessAllFields() throws Exception {
		B b = new B();
		b.s = TEST_STRING;
		b.setL(TEST_LONG);
		b.setI(TEST_INTEGER);

		String s = Fields.IgnoreAccess.get(b, B.class.getDeclaredField("s"));
		Long l = Fields.IgnoreAccess.get(b, B.class.getDeclaredField("l"));
		Integer i = Fields.IgnoreAccess.get(b, B.class.getDeclaredField("i"));

		assertThat(s, equalTo(TEST_STRING));
		assertThat(l, equalTo(TEST_LONG));
		assertThat(i, equalTo(TEST_INTEGER));
	}

	@Test
	void shouldThrowExceptionOnInvalidField() throws Exception {
		Object o = new Object();
		Field s = B.class.getDeclaredField("s");
		assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.get(o, s));
	}

	@Test
	void shouldAccessAllFieldsOnCallWithFieldName() {
		B b = new B();
		b.s = TEST_STRING;
		b.setL(TEST_LONG);
		b.setI(TEST_INTEGER);

		String s = Fields.IgnoreAccess.get(b, "s");
		Long l = Fields.IgnoreAccess.get(b, "l");
		Integer i = Fields.IgnoreAccess.get(b, "i");

		assertThat(s, equalTo(TEST_STRING));
		assertThat(l, equalTo(TEST_LONG));
		assertThat(i, equalTo(TEST_INTEGER));
	}

	@Test
	void shouldThrowExceptionOnNonExistentField() {
		Object o = new Object();
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.get(o, NON_EXISTENT_FIELD));
		assertThat(e.getMessage(), equalTo("Could not find field '" + NON_EXISTENT_FIELD + "' on object of type " + o.getClass()));
	}

	@Test
	void shouldKeepAccessModifiersUnchangedAfterCall() throws Exception {
		B b = new B();
		b.setI(TEST_INTEGER);

		Field field = B.class.getDeclaredField("i");
		Integer i = Fields.IgnoreAccess.get(b, field);

		assertThat(i, equalTo(TEST_INTEGER));

		assertThat(field.canAccess(b), equalTo(false));
	}

	static class C extends B {
		// empty
	}

	@Test
	void shouldGetTheFieldValueInHierarchy() throws Exception {
		C b = new C();
		b.s = TEST_STRING;

		String s = Fields.IgnoreAccess.get(b, B.class.getDeclaredField("s"));

		assertThat(s, equalTo(TEST_STRING));
	}

}
