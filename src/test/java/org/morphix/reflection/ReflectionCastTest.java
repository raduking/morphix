package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.morphix.reflection.Reflection.cast;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Reflection#cast(Object)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionCastTest {

	@Test
	void shouldCastToInferredType() {
		Object o = "Bubu";

		String bubu = cast(o);

		assertThat(bubu, notNullValue());
		assertThat(bubu, equalTo("Bubu"));
	}

}
