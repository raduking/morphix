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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.A;

/**
 * Test class for
 * <ul>
 * <li>{@link Fields.IgnoreAccess#set(Object, String, Object)}</li>
 * <li>{@link Fields.IgnoreAccess#set(Object, Field, Object)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsIgnoreAccessSetTest {

	private static final String MISSING_FIELD_NAME = "missingField";
	private static final String FIELD_NAME = "field";
	private static final String VALUE = "someValue";

	@Test
	void shouldSetIgnoreAccess() {
		A object = new A();
		Fields.IgnoreAccess.set(object, FIELD_NAME, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
	}

	@Test
	void shouldKeepTheFieldModifierOnSetIgnoreAccess() throws Exception {
		A object = new A();
		Field field = A.class.getDeclaredField(FIELD_NAME);
		Fields.IgnoreAccess.set(object, field, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
		assertFalse(field.canAccess(object));
	}

	@Test
	void shouldThrowExceptionForFieldThatDoesNotExist() {
		A object = new A();
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.set(object, MISSING_FIELD_NAME, VALUE));
		assertThat(e.getMessage(), equalTo("Cannot set field '" + MISSING_FIELD_NAME + "' in class " + object.getClass() + ": field not found"));
	}

	@Test
	void shouldThrowExceptionIfBadValueIsSet() {
		A object = new A();
		Object other = new Object();
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.set(object, FIELD_NAME, other));
		assertThat(e.getMessage(), equalTo("Cannot set field value for " + FIELD_NAME));
	}

	static class B extends A {
		// empty
	}

	@Test
	void shouldSetIgnoreAccessOnBaseClassField() {
		B object = new B();
		Fields.IgnoreAccess.set(object, FIELD_NAME, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
	}

	static class D {

		private static final String FIELD = init();

		private static String init() {
			return "test";
		}

		private final String field = VALUE;

		public String getField() {
			return field;
		}
	}

	@Test
	void shouldSetIgnoreAccessOnFinalField() {
		D object = new D();
		Fields.IgnoreAccess.set(object, FIELD_NAME, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
	}

	@Test
	void shouldSetIgnoreAccessOnFinalFieldWithUnsafe() throws NoSuchFieldException, SecurityException {
		D object = new D();
		Field field = D.class.getDeclaredField(FIELD_NAME);
		Fields.Unsafe.set(object, field, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
	}

	@Test
	void shouldSetNullOnReferenceFieldWithUnsafe() throws Exception {
		D object = new D();
		Field field = D.class.getDeclaredField(FIELD_NAME);

		Fields.Unsafe.set(object, field, null);

		assertThat(Fields.IgnoreAccess.get(object, field), equalTo(null));
	}

	@Test
	void shouldThrowExceptionIfValueIsNotAssignableWithUnsafe() throws Exception {
		D object = new D();
		Field field = D.class.getDeclaredField(FIELD_NAME);

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Fields.Unsafe.set(object, field, new Object()));

		assertThat(e.getMessage(), equalTo("Can not set java.lang.String field to java.lang.Object"));
		assertThat(Fields.IgnoreAccess.get(object, field), equalTo(VALUE));
	}

	@Test
	void shouldSetIgnoreAccessOnStaticFinalField() {
		Fields.IgnoreAccess.setStatic(D.class, "FIELD", VALUE);

		assertThat(D.FIELD, equalTo(VALUE));
	}

	@Test
	void shouldThrowExceptionIfFieldIsNull() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.set(new D(), (Field) null, VALUE));

		assertThat(e.getMessage(), equalTo("Cannot set field value: field descriptor is null"));
	}

	@Test
	void shouldThrowExceptionIfBadValueIsSetOnFinalField() {
		D object = new D();

		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.set(object, FIELD_NAME, new Object()));

		assertThat(e.getMessage(), equalTo("Cannot set field value for " + FIELD_NAME));
		assertThat(object.getField(), equalTo(VALUE));
	}

	static class E {
		final byte byteValue = Byte.valueOf((byte) 7);
		final short shortValue = Short.valueOf((short) 7);
		final int intValue = Integer.valueOf(7);
		final long longValue = Long.valueOf(7L);
		final float floatValue = Float.valueOf(7f);
		final double doubleValue = Double.valueOf(7d);
		final char charValue = Character.valueOf('a');
		final boolean booleanValue = Boolean.TRUE;
		static final int STATIC_INT = Integer.valueOf(9);
	}

	@Test
	void shouldSetFinalPrimitiveFields() {
		E object = new E();

		Fields.IgnoreAccess.set(object, "byteValue", (byte) 0);
		Fields.IgnoreAccess.set(object, "shortValue", (byte) 1);
		Fields.IgnoreAccess.set(object, "intValue", (byte) 2);
		Fields.IgnoreAccess.set(object, "longValue", (byte) 3);
		Fields.IgnoreAccess.set(object, "floatValue", (byte) 4);
		Fields.IgnoreAccess.set(object, "doubleValue", (byte) 5);
		Fields.IgnoreAccess.set(object, "charValue", (char) 0);
		Fields.IgnoreAccess.set(object, "booleanValue", false);

		assertThat(object.byteValue, equalTo((byte) 0));
		assertThat(object.shortValue, equalTo((short) 1));
		assertThat(object.intValue, equalTo(2));
		assertThat(object.longValue, equalTo(3L));
		assertThat(object.floatValue, equalTo(4f));
		assertThat(object.doubleValue, equalTo(5d));
		assertThat(object.charValue, equalTo((char) 0));
		assertThat(object.booleanValue, equalTo(false));
	}

	@Test
	void shouldSetStaticFinalPrimitiveByNameAndByField() throws Exception {
		Fields.IgnoreAccess.setStatic(E.class, "STATIC_INT", 1);
		assertThat(Fields.IgnoreAccess.getStatic(E.class, "STATIC_INT"), equalTo(1));

		Field field = E.class.getDeclaredField("STATIC_INT");
		Fields.IgnoreAccess.setStatic(E.class, field, 2);
		assertThat(Fields.IgnoreAccess.getStatic(E.class, field), equalTo(2));
	}

	@Test
	void shouldThrowExceptionIfBadValueIsSetOnStaticFinalField() {
		Integer before = Fields.IgnoreAccess.getStatic(E.class, "STATIC_INT");

		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.IgnoreAccess.setStatic(E.class, "STATIC_INT", "nope"));

		assertThat(e.getMessage(), equalTo("Cannot set field value for STATIC_INT"));
		assertThat(e.getCause(), instanceOf(IllegalArgumentException.class));
		assertThat(e.getCause().getMessage(), equalTo("Can not set int field to java.lang.String"));
		assertThat(Fields.IgnoreAccess.getStatic(E.class, "STATIC_INT"), equalTo(before));
	}

	@Test
	void shouldResetFinalPrimitiveFields() throws Exception {
		E object = new E();

		Fields.reset(E.class.getDeclaredField("intValue"), object);
		Fields.reset(E.class.getDeclaredField("booleanValue"), object);
		Fields.reset(E.class.getDeclaredField("charValue"), object);

		assertThat(object.intValue, equalTo(0));
		assertThat(object.booleanValue, equalTo(false));
		assertThat(object.charValue, equalTo((char) 0));
	}
}
