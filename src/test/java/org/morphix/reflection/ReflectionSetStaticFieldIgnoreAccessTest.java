package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields#setStaticIgnoreAccess(Class, String, Object)}
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionSetStaticFieldIgnoreAccessTest {

	private static final String MISSING_FIELD_NAME = "missingField";
	private static final String STATIC_FIELD_NAME = "staticField";
	private static final String VALUE = "someValue";

	public static class B {
		private static String staticField = null;

		public static String getStaticField() {
			return staticField;
		}
	}

	@Test
	void shouldSetStaticFieldIgnoringAccess() {
		Fields.setStaticIgnoreAccess(B.class, STATIC_FIELD_NAME, VALUE);

		String result = B.getStaticField();
		assertThat(result, equalTo(VALUE));
	}

	@Test
	void shouldThrowExceptionForFieldThatDoesNotExist() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.setStaticIgnoreAccess(B.class, MISSING_FIELD_NAME, VALUE));
		assertThat(e.getMessage(),
				equalTo("Could not find static field with name " + MISSING_FIELD_NAME + " on class " + B.class));
	}

}
