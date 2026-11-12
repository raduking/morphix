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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

/**
 * Test class {@link Methods#getSetterMethod(Class, Field)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsGetSetterMethodTest {

	static class A {

		private String s;

		@SuppressWarnings("unused")
		private String t;

		private Boolean b;

		@SuppressWarnings("unused")
		private Integer i;

		@SuppressWarnings("unused")
		private int n;

		@SuppressWarnings("unused")
		private boolean p;

		public String getS() {
			return s;
		}

		public void setS(final String s) {
			this.s = s;
		}

		public Boolean getB() {
			return b;
		}

		public void setB(final boolean b) {
			this.b = b;
		}

		public void setN(final Integer n) {
			this.n = n;
		}
	}

	@Test
	void shouldReturnSetterMethod() throws Exception {
		Field field = A.class.getDeclaredField("s");

		Method expected = A.class.getDeclaredMethod("setS", String.class);

		Method result = Methods.getSetterMethod(A.class, field);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldThrowExceptionWhenSetterMethodNotFound() throws Exception {
		Field field = A.class.getDeclaredField("t");

		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.getSetterMethod(A.class, field));

		assertThat(e.getMessage(), equalTo("Cannot find setter method "
				+ "setT(" + field.getType().getTypeName() + ") in class " + A.class));
	}

	@Test
	void shouldReturnSetterMethodWithPrimitive() throws Exception {
		Field field = A.class.getDeclaredField("b");

		Method expected = A.class.getDeclaredMethod("setB", boolean.class);

		Method result = Methods.getSetterMethod(A.class, field);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldThrowExceptionWhenClassIsNull() throws Exception {
		Field field = A.class.getDeclaredField("s");

		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.getSetterMethod(null, field));

		assertThat(e.getMessage(), equalTo("Cannot find setter method: class is null"));
	}

	@Test
	void shouldThrowExceptionWhenFieldIsNull() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.getSetterMethod(A.class, null));

		assertThat(e.getMessage(), equalTo("Cannot find setter method in class " + A.class + ": field descriptor is null"));
	}

	@Test
	void shouldThrowExceptionWhenSetterMethodWithPrimitiveNotFound() throws Exception {
		Field field = A.class.getDeclaredField("i");

		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.getSetterMethod(A.class, field));

		assertThat(e.getMessage(), equalTo("Cannot find setter method "
				+ "setI(" + field.getType().getTypeName() + ") or "
				+ "setI(" + int.class.getTypeName() + ") in class " + A.class));
	}

	@Test
	void shouldReturnSetterMethodWhenFieldIsPrimitiveAndSetterTakesWrapper() throws Exception {
		Field field = A.class.getDeclaredField("n");

		Method expected = A.class.getDeclaredMethod("setN", Integer.class);

		Method result = Methods.getSetterMethod(A.class, field);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldThrowExceptionWhenPrimitiveAndWrapperSettersAreNotFound() throws Exception {
		Field field = A.class.getDeclaredField("p");

		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.getSetterMethod(A.class, field));

		assertThat(e.getMessage(), equalTo("Cannot find setter method "
				+ "setP(" + field.getType().getTypeName() + ") or "
				+ "setP(" + Boolean.class.getTypeName() + ") in class " + A.class));
	}

}
