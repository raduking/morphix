package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ParameterizedClass}.
 *
 * @author Radu Sebastian LAZIN
 */
class ParameterizedClassTest {

	@Test
	void shouldDetectGenericArgumentType() {

		ParameterizedClass<String> pc = new ParameterizedClass<>() {
			// empty
		};

		assertThat(pc.getGenericArgumentType(), equalTo(String.class));
	}

	@Test
	void shouldReturnTrueForGenericClass() {
		boolean result = ParameterizedClass.isGenericClass(ArrayList.class);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseForNonGenericClass() {
		boolean result = ParameterizedClass.isGenericClass(Object.class);

		assertFalse(result);
	}

	@Test
	void shouldFailToExtractArgumentTypeForNonGenericClasses() {
		assertThrows(ReflectionException.class, () -> ParameterizedClass.getGenericParameterType(String.class, 0));
	}
}
