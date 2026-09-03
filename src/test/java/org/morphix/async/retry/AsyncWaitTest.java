package org.morphix.async.retry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link AsyncWait}.
 *
 * @author Radu Sebastian LAZIN
 */
class AsyncWaitTest {

	@Test
	void shouldHaveFunctionalInterfaceAnnotation() {
		assertTrue(AsyncWait.class.isAnnotationPresent(FunctionalInterface.class));
	}

	@Test
	void shouldThrowExceptionOnCallingWaitDefaultConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(AsyncWait.Default.class);

		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
