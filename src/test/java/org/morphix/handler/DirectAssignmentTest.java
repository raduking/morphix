package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

/**
 * Test class for {@link DirectAssignment}.
 *
 * @author Radu Sebastian LAZIN
 */
class DirectAssignmentTest {

	public static class A {
		public Integer i;
	}

	public static class B {
		public Integer i;
	}

	@Test
	void shouldReturnHandledIfValueIsNull() throws Exception {
		A src = new A();
		B dst = new B();

		Field sField = A.class.getDeclaredField("i");
		Field dField = B.class.getDeclaredField("i");

		FieldHandlerResult result = new DirectAssignment().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

}
