package org.morphix;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.MemberAccessor;
import org.morphix.reflection.testdata.A;

/**
 * Tests the {@link MemberAccessor} class.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterMemberAccessorTest {

	private static final String FIELD_I = "i";

	private static final int INT_13 = 13;
	private static final int INT_11 = 11;

	@Test
	void shouldReleaseNonAccessibleFields() throws Exception {
		A a = new A();
		a.setI(INT_11);

		Field field = a.getClass().getDeclaredField(FIELD_I);

		try (MemberAccessor<Field> fieldAccessor = new MemberAccessor<>(a, field)) {
			field.set(a, INT_13);
		}

		assertFalse(field.canAccess(a));
	}

	@Test
	void shouldReleaseAccessibleFields() throws Exception {
		A a = new A();
		a.setI(INT_11);

		Field field = a.getClass().getDeclaredField(FIELD_I);
		field.setAccessible(true);

		try (MemberAccessor<Field> fieldAccessor = new MemberAccessor<>(a, field)) {
			field.set(a, INT_13);
		}

		assertTrue(field.canAccess(a));
	}
}
