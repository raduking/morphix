package org.morphix.reflection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.A;

/**
 * Test class for {@link MemberAccessor}.
 *
 * @author Radu Sebastian LAZIN
 */
class MemberAccessorTest {

	@Test
	void shouldChangeAccessInConstructorAndChangeBackOnRelease() throws Exception {
		A a = new A();
		Field field = A.class.getDeclaredField(A.FIELD_NAME);

		assertFalse(field.canAccess(a));

		boolean isAccessible;
		try (MemberAccessor<Field> ignored = new MemberAccessor<>(a, field)) {
			isAccessible = field.canAccess(a);
		}

		assertTrue(isAccessible);
		assertFalse(field.canAccess(a));
	}

}
