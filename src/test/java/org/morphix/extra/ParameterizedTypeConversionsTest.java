package org.morphix.extra;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ParameterizedTypeConversions}.
 *
 * @author Radu Sebastian LAZIN
 */
class ParameterizedTypeConversionsTest {

	@Test
	void shouldCreateNewTypeInstanceFromArray() throws Exception {
		Method method = A.class.getDeclaredMethod("getOptionalArray");

		Object result = ParameterizedTypeConversions.newTypeInstance(new Object(), method.getGenericReturnType(), null);

		assertNull(result);
	}

	public static class A {

		public Optional<String>[] getOptionalArray() {
			return null;
		}

	}

}
