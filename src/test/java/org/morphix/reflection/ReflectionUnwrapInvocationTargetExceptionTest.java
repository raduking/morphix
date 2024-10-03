package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Reflection#unwrapInvocationTargetException(InvocationTargetException)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionUnwrapInvocationTargetExceptionTest {

	private static final String TEST_MESSAGE = "Test";

	@Test
	void shouldUnwrapInvocationTargetException() {
		RuntimeException expected = new RuntimeException(TEST_MESSAGE);
		InvocationTargetException e = new InvocationTargetException(expected);

		Throwable result = Reflection.unwrapInvocationTargetException(e);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTheGivenInvocationTargetExceptionWhenCauseIsNull() {
		InvocationTargetException expected = new InvocationTargetException(null);

		Throwable result = Reflection.unwrapInvocationTargetException(expected);

		assertThat(result, equalTo(expected));
	}
}
