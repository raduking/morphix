package org.morphix;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link MethodCaller}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodCallerTest {

	private static final String TEST_STRING = "testString";

	@Test
	void shouldCallSetterIfArgumentIsNotNull() {
		A a = new A(null);

		MethodCaller.nonNullCall(a::setS, TEST_STRING);

		assertThat(a.s, equalTo(TEST_STRING));
	}

	@Test
	void shouldNotCallSetterIfArgumentIsNull() {
		A a = new A(TEST_STRING);

		MethodCaller.nonNullCall(a::setS, null);

		assertThat(a.s, equalTo(TEST_STRING));
	}

	public static class A {

		String s;

		public A(final String s) {
			this.s = s;
		}

		public void setS(final String s) {
			this.s = s;
		}
	}

}
