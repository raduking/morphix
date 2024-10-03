package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Reflection#unwrapException(Throwable)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionUnwrapExceptionTest {

	private static final String TEST_MESSAGE = "Test";

	@Test
	void shouldUnwrapInvocationTargetException() {
		RuntimeException expected = new RuntimeException(TEST_MESSAGE);
		InvocationTargetException e = new InvocationTargetException(expected);

		Throwable result = Reflection.unwrapException(e);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTheGivenInvocationTargetExceptionWhenCauseIsNull() {
		InvocationTargetException expected = new InvocationTargetException(null);

		Throwable result = Reflection.unwrapException(expected);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTheGivenInvocationTargetExceptionWhenCauseIsTheSame() {
		Throwable expected = new Throwable();

		Throwable result = Reflection.unwrapException(expected);

		assertThat(result, equalTo(expected));
	}

	public static class TestThrowable extends Throwable {
		@Serial
		private static final long serialVersionUID = 81486376788817020L;
		private final Throwable cause = this;

		@Override
		public synchronized Throwable getCause() {
			return cause;
		}
	}

	@Test
	void shouldReturnTheGivenInvocationTargetExceptionWhenCauseIsTheSameAsThis() {
		TestThrowable expected = new TestThrowable();

		Throwable result = Reflection.unwrapException(expected);

		assertThat(result, equalTo(expected));
	}

}
