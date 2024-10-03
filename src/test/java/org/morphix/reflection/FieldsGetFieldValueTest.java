package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.A;

/**
 * Test class for
 * {@link Reflection#setFieldValue(Object, java.lang.reflect.Field, Object)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsGetFieldValueTest {

	@Test
	void shouldReturnFieldValue() throws Exception {
		A a = new A();
		a.b = Boolean.TRUE;

		Field bField = A.class.getDeclaredField("b");
		Boolean result = Fields.getFieldValue(a, bField);

		assertThat(result, equalTo(Boolean.TRUE));
	}

	@Test
	void shouldThrowExceptionIfFiledIsNotAccessible() throws Exception {
		A a = new A();

		Field field = A.class.getDeclaredField(A.FIELD_NAME);
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.getFieldValue(a, field));

		assertThat(e.getMessage(), equalTo("Could not get field " + A.FIELD_NAME));
	}

}
