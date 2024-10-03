package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.A;

/**
 * Test class for {@link Fields#setIgnoreAccess(Object, String, Object)} and
 * {@link Fields#setIgnoreAccess(Object, Field, Object)}
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionSetIgnoreAccessTest {

	private static final String MISSING_FIELD_NAME = "missingField";
	private static final String FIELD_NAME = "field";
	private static final String VALUE = "someValue";

	@Test
	void shouldSetIgnoreAccess() {
		A object = new A();
		Fields.setIgnoreAccess(object, FIELD_NAME, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
	}

	@Test
	void shouldKeepTheFieldModifierOnSetIgnoreAccess() throws Exception {
		A object = new A();
		Field field = A.class.getDeclaredField(FIELD_NAME);
		Fields.setIgnoreAccess(object, field, VALUE);

		assertThat(object.getField(), equalTo(VALUE));
		assertFalse(field.canAccess(object));
	}

	@Test
	void shouldThrowExceptionForFieldThatDoesNotExist() {
		A object = new A();
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.setIgnoreAccess(object, MISSING_FIELD_NAME, VALUE));
		assertThat(e.getMessage(), equalTo("Could not find field '" + MISSING_FIELD_NAME + "' on object of type " + object.getClass()));
	}

	@Test
	void shouldThrowExceptionIfBadValueIsSet() {
		A object = new A();
		Object other = new Object();
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.setIgnoreAccess(object, FIELD_NAME, other));
		assertThat(e.getMessage(), equalTo("Could not set field " + FIELD_NAME));
	}
}
