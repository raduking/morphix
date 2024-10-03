package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ReflectionException}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionExceptionTest {

	private static final String TEST_MESSAGE = "testMessage";

	@Test
	void shouldSetTheMessage() {
		ReflectionException e = new ReflectionException(TEST_MESSAGE);

		assertThat(e.getMessage(), equalTo(TEST_MESSAGE));
	}

	@Test
	void shouldSetTheCauseAndMessage() {
		Throwable cause = new Exception();
		ReflectionException e = new ReflectionException(TEST_MESSAGE, cause);

		assertThat(e.getCause(), equalTo(cause));
		assertThat(e.getMessage(), equalTo(TEST_MESSAGE));
	}

	@Test
	void shouldSetTheCause() {
		Throwable cause = new Exception();
		ReflectionException e = new ReflectionException(cause);

		assertThat(e.getCause(), equalTo(cause));
	}

	@Test
	void shouldWrapExceptionIfIsRunnableThrows() {
		Runnable runnable = mock(Runnable.class);
		doThrow(new RuntimeException()).when(runnable).run();

		ReflectionException e = assertThrows(ReflectionException.class, () -> ReflectionException.wrapThrowing(runnable, TEST_MESSAGE));
		assertThat(e.getMessage(), equalTo(TEST_MESSAGE));
	}

	@Test
	void shouldWrapExceptionIfIsSupplierThrows() {
		Supplier<?> supplier = mock(Supplier.class);
		doThrow(new RuntimeException()).when(supplier).get();

		ReflectionException e = assertThrows(ReflectionException.class, () -> ReflectionException.wrapThrowing(supplier, TEST_MESSAGE));
		assertThat(e.getMessage(), equalTo(TEST_MESSAGE));
	}

}
