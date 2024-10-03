package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields#getStaticIgnoreAccess(Class, String)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionGetStaticFieldsTest {

	private static final String FIELD_VALUE = "aaa";

	@Test
	void shouldReturnStaticFieldValue() {
		String staticField = Fields.getStaticIgnoreAccess(A.class, "staticField");

		assertThat(staticField, equalTo(FIELD_VALUE));
	}

	@Test
	void shouldThrowErrorIfFieldNotFound() {
		assertThrows(ReflectionException.class, () -> Fields.getStaticIgnoreAccess(A.class, "wrongName"));

	}

	private static class A {
		@SuppressWarnings("unused")
		public static final String staticField = FIELD_VALUE;
	}
}
