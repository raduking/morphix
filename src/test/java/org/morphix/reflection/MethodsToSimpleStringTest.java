package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class MethodsToSimpleStringTest {

	@Test
	void shouldConvertToString() throws Exception {
		Method method = A.class.getDeclaredMethod("foo", String.class, int.class);

		String result = Methods.toSimpleString(method);

		assertThat(result, equalTo("foo(java.lang.String, int)"));
	}

	public static class A {

		public String foo(final String s, final int y) {
			return s + y;
		}

	}

}
