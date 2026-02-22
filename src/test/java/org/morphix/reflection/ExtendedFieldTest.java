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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.morphix.utils.Tests;

/**
 * Test class for {@link ExtendedField}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExtendedFieldTest {

	private static final String EOL = System.lineSeparator();
	private static final Long TEST_LONG = 666L;
	private static final String REFLECTION_SET_NAME = "reflectionName";

	@Test
	void shouldReturnObjectClassIfNoFieldIsSet() {
		ExtendedField sfo = ExtendedField.of(null);

		Class<?> result = sfo.toClass();

		assertThat(result, equalTo(Object.class));
	}

	public static class A {
		Long l;
	}

	@Test
	void shouldBuildStringWithToString() throws Exception {
		A a = new A();
		a.l = TEST_LONG;

		ExtendedField fop = ExtendedField.of(A.class.getDeclaredField("l"), a);
		String result = fop.toString();

		assertThat(result, equalTo("Field: l" + EOL
				+ "Type: class java.lang.Long" + EOL
				+ "Value: " + String.valueOf(TEST_LONG) + EOL
				+ "Object: " + a));
	}

	@Test
	void shouldBuildStringWithToStringWithNoField() {
		A a = new A();
		a.l = TEST_LONG;

		ExtendedField fop = ExtendedField.of((Field) null, a);
		String result = fop.toString();

		assertThat(result, equalTo("Object: " + a));
	}

	@Test
	void shouldBuildStringWithToStringWithNoObject() throws Exception {
		A a = new A();
		a.l = TEST_LONG;

		ExtendedField fop = ExtendedField.of(A.class.getDeclaredField("l"));
		String result = fop.toString();

		assertThat(result, equalTo("Field: l" + EOL
				+ "Type: class java.lang.Long"));
	}

	@Test
	void shouldReturnDefaultClassTypeWhenFieldAndGetterMethodIsNullOnToClass() {
		ExtendedField ef = ExtendedField.of(null);
		Fields.IgnoreAccess.set(ef, "name", REFLECTION_SET_NAME);

		Class<?> result = ef.toClass();

		assertThat(result, equalTo(Object.class));
	}

	@Test
	void shouldReturnDefaultClassTypeWhenFieldAndGetterMethodIsNullOnGetType() {
		ExtendedField ef = ExtendedField.of(null);
		Fields.IgnoreAccess.set(ef, "name", REFLECTION_SET_NAME);

		Type result = ef.getType();

		assertThat(result, equalTo(Object.class));
	}

	@Test
	void shouldReturnNullWhenFieldHasNoObjectOrGetterMethod() {
		ExtendedField ef = ExtendedField.of(null);
		Fields.IgnoreAccess.set(ef, "name", REFLECTION_SET_NAME);

		Object result = ef.getGenericReturnType(0);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldNotModifyFieldValueIfFieldAndGetterMethodAreNull() {
		ExtendedField ef = ExtendedField.of(null);

		ef.setFieldValue("test");

		AtomicReference<Object> result = Fields.IgnoreAccess.get(ef, "fieldValue");

		assertThat(result.get(), equalTo(null));
	}

	@Test
	void shouldNotModifyNameWhenSettingGetterMethodToNull() {
		ExtendedField ef = ExtendedField.of(null);
		Fields.IgnoreAccess.set(ef, "name", REFLECTION_SET_NAME);

		ef.setGetterMethod(null);

		String result = ef.getName();

		assertThat(result, equalTo(REFLECTION_SET_NAME));
	}

	@Test
	void shouldReturnNullOnGetGenericReturnTypeWhenObjectIsNullAndFieldIsSet() {
		Field field = Fields.getOneDeclared(ExtendedFieldTest.class, "TEST_LONG");
		ExtendedField ef = ExtendedField.of(field, null);

		Object result = ef.getGenericReturnType(0);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullForNoAnnotationOnField() {
		Field field = Fields.getOneDeclared(ExtendedFieldTest.class, "TEST_LONG");
		ExtendedField ef = ExtendedField.of(field, null);

		Deprecated annotation = ef.getAnnotation(Deprecated.class);

		assertThat(annotation, equalTo(null));
	}

	static class F {

		@Deprecated
		public String x;

		@Deprecated
		public String getY() {
			return "y";
		}
	}

	@Test
	void shouldReturnAnnotationFromField() {
		Field field = Fields.getOneDeclared(F.class, "x");
		ExtendedField ef = ExtendedField.of(field, null);

		Deprecated annotation = ef.getAnnotation(Deprecated.class);

		assertThat(annotation, equalTo(field.getAnnotation(Deprecated.class)));
	}

	@Test
	void shouldReturnAnnotationFromGetterMethod() {
		Method method = Methods.getOneDeclared(MethodType.GETTER.getMethodName("y"), F.class);
		ExtendedField ef = ExtendedField.of(method, null);

		Deprecated annotation = ef.getAnnotation(Deprecated.class);

		assertThat(annotation, equalTo(method.getAnnotation(Deprecated.class)));
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiate() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(ExtendedField.Default.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
