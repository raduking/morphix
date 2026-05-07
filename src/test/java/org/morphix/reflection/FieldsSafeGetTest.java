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

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.A;
import org.morphix.reflection.testdata.B;

/**
 * Test class for {@link Fields.Safe#get(Object, Field)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsSafeGetTest {

	private static final String TEST_STRING = "testString";
	private static final Long TEST_LONG = 17L;
	private static final Integer TEST_INTEGER = 13;
	private static final String NON_EXISTENT_FIELD = "$NonExistentField$";

	@Test
	void shouldGetTheFieldValue() throws Exception {
		B b = new B();
		b.s = TEST_STRING;

		String s = Fields.Safe.get(b, B.class.getDeclaredField("s"));

		assertThat(s, equalTo(TEST_STRING));
	}

	@Test
	void shouldAccessAllFields() throws Exception {
		B b = new B();
		b.s = TEST_STRING;
		b.setL(TEST_LONG);
		b.setI(TEST_INTEGER);

		String s = Fields.Safe.get(b, B.class.getDeclaredField("s"));
		Long l = Fields.Safe.get(b, B.class.getDeclaredField("l"));
		Integer i = Fields.Safe.get(b, B.class.getDeclaredField("i"));

		assertThat(s, equalTo(TEST_STRING));
		assertThat(l, equalTo(TEST_LONG));
		assertThat(i, equalTo(TEST_INTEGER));
	}

	@Test
	void shouldThrowExceptionOnInvalidField() throws Exception {
		Object o = new Object();
		Field s = B.class.getDeclaredField("s");

		String result = Fields.Safe.get(o, s);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldAccessAllFieldsOnCallWithFieldName() {
		B b = new B();
		b.s = TEST_STRING;
		b.setL(TEST_LONG);
		b.setI(TEST_INTEGER);

		String s = Fields.Safe.get(b, "s");
		Long l = Fields.Safe.get(b, "l");
		Integer i = Fields.Safe.get(b, "i");

		assertThat(s, equalTo(TEST_STRING));
		assertThat(l, equalTo(TEST_LONG));
		assertThat(i, equalTo(TEST_INTEGER));
	}

	@Test
	void shouldThrowExceptionOnNonExistentField() {
		Object o = new Object();

		Object result = Fields.Safe.get(o, NON_EXISTENT_FIELD);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldKeepAccessModifiersUnchangedAfterCall() throws Exception {
		B b = new B();
		b.setI(TEST_INTEGER);

		Field field = B.class.getDeclaredField("i");
		Integer i = Fields.Safe.get(b, field);

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

		String s = Fields.Safe.get(b, B.class.getDeclaredField("s"));

		assertThat(s, equalTo(TEST_STRING));
	}

	@Test
	void shouldReturnFieldValue() throws Exception {
		A a = new A();
		a.b = Boolean.TRUE;
		Field bField = A.class.getDeclaredField(A.FieldName.B);

		Boolean result = Fields.Safe.get(a, bField);

		assertThat(result, equalTo(Boolean.TRUE));
	}

	@Test
	void shouldReturnNullIfFieldIsNotAccessible() throws Exception {
		A a = new A();
		Field field = A.class.getDeclaredField(A.FieldName.FIELD);

		String result = Fields.Safe.get(a, field);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullIfFieldIsNotPresentInClassAndNotAccesible() throws Exception {
		Field field = A.class.getDeclaredField(A.FieldName.FIELD);
		B b = new B();

		String result = Fields.Safe.get(b, field);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullIfFieldIsNotPresentInClass() throws Exception {
		Field field = A.class.getDeclaredField(A.FieldName.B);
		B b = new B();

		Boolean result = Fields.Safe.get(b, field);

		assertThat(result, equalTo(null));
	}
}
