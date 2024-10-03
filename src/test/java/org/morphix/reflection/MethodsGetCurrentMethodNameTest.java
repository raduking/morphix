package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Methods#getCurrentMethodName(boolean)}
 */
class MethodsGetCurrentMethodNameTest {

	@Test
	void shouldReturnCurrentMethodName() {
		String methodName = Methods.getCurrentMethodName(false);

		assertThat(methodName, equalTo("shouldReturnCurrentMethodName"));
	}

	@Test
	void shouldReturnCurrentMethodNameWithClassName() {
		String methodName = Methods.getCurrentMethodName(true);

		assertThat(methodName, equalTo(MethodsGetCurrentMethodNameTest.class.getName() + ".shouldReturnCurrentMethodNameWithClassName"));
	}
}
