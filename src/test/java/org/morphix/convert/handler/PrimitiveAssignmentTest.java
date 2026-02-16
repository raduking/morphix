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
package org.morphix.convert.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.convert.FieldHandlerResult;

/**
 * Tests conversions of primitives to their respective java classes.
 *
 * @author Radu Sebastian LAZIN
 */
class PrimitiveAssignmentTest {

	private static final int TEST_PRIMITIVE_INT = 15;
	private static final Integer TEST_INTEGER = TEST_PRIMITIVE_INT;

	public static class A {
		int testInt;
		long testLong;
	}

	public static class B {
		Integer testInt;
		Long testLong;
	}

	@Test
	void shouldConsiderHandledIfItPassedCondition() throws Exception {
		B src = new B();
		src.testInt = TEST_PRIMITIVE_INT;

		A dst = new A();

		Field dField = A.class.getDeclaredField("testInt");
		Field sField = B.class.getDeclaredField("testInt");

		FieldHandlerResult result = new PrimitiveAssignment().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
		assertThat(dst.testInt, equalTo(TEST_INTEGER));
	}

	@Test
	void shouldConsiderHandledIfItPassedConditionEvenIfNoValueSet() throws Exception {
		B src = new B();
		src.testInt = null;

		A dst = new A();
		dst.testInt = TEST_PRIMITIVE_INT + 1;

		Field dField = A.class.getDeclaredField("testInt");
		Field sField = B.class.getDeclaredField("testInt");

		FieldHandlerResult result = new PrimitiveAssignment().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
		assertThat(dst.testInt, equalTo(TEST_PRIMITIVE_INT + 1));
	}

	public static class C {
		Integer i;
		int j;
	}

	public static class D {
		Integer i;
		int j;
	}

	@Test
	void shouldSkipHandlingIfTheFieldsHaveTheSameType() throws Exception {
		Field sField = C.class.getDeclaredField("i");
		Field dField = D.class.getDeclaredField("i");

		boolean result = new PrimitiveAssignment().condition(of(sField), of(dField));

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldHandleIfOneIsPrimitiveAndOtherIsBoxingObject() throws Exception {
		PrimitiveAssignment victim = new PrimitiveAssignment();

		Field sField = C.class.getDeclaredField("i");
		Field dField = D.class.getDeclaredField("j");

		boolean result = victim.condition(of(sField), of(dField));

		assertThat(result, equalTo(true));

		sField = C.class.getDeclaredField("j");
		dField = D.class.getDeclaredField("i");

		result = victim.condition(of(sField), of(dField));

		assertThat(result, equalTo(true));
	}

}
