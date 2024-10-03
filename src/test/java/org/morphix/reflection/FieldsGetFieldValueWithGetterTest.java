package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields#get(Object, String)}
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsGetFieldValueWithGetterTest {

	@Test
	void shouldRetrieveFieldValueByGetter() {
		Integer result = Fields.get(new A(), "x");

		assertThat(result, equalTo(2));
	}

	@Test
	void shouldRetrieveFieldValueByField() {
		Integer result = Fields.get(new A(), "y");

		assertThat(result, equalTo(3));
	}

	public static class A {
		Integer x = 2;
		Integer y = 3;

		public Integer getX() {
			return x;
		}
	}

}