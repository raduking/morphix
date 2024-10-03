package org.morphix.function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.function.FieldValueFunction.from;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link FieldValueFunction}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldValueFunctionTest {

	@Test
	void shouldCreateAFieldValueFunction() throws Exception {
		Long expected = 13L;

		Long result = from(expected).value();

		assertThat(result, equalTo(expected));
	}

}
