package org.morphix.async;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link CompletableFutures}.
 *
 * @author Radu Sebastian LAZIN
 */
class CompletableFuturesTest {

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateWithConstructor() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(CompletableFutures.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
