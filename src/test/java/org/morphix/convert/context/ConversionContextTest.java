package org.morphix.convert.context;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ConversionContext}.
 *
 * @author morphix
 */
class ConversionContextTest {

	static class TestConversionContext implements ConversionContext {
		// No need to override enter and exit for this test
	}

	@Test
	void shouldReturnObjectOnVisitWithoutVisitedResultSupplier() {
		final ConversionContext context = new TestConversionContext();
		final Object obj = new Object();

		final Object result = context.visit(obj, () -> obj, () -> null);

		assertThat(result, equalTo(obj));
	}
}
