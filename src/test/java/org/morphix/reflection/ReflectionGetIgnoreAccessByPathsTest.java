package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for
 * <p>
 *
 * {@link Fields#getIgnoreAccessByPath(Object, String)}
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionGetIgnoreAccessByPathsTest {

	private static final String TEST_STRING = "testString";

	public static class A {
		public String x;
	}

	public static class B {
		public A a;
	}

	@Test
	void shouldGetFieldByPaths() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		String x = Fields.getIgnoreAccessByPaths(b, "x.y,a.x");

		assertThat(x, equalTo(TEST_STRING));
	}

	@Test
	void shouldGetFieldByPathsAtFirstLevel() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		A result = Fields.getIgnoreAccessByPaths(b, "a");

		assertThat(result, equalTo(a));
	}

	@Test
	void shouldReturnNullIfFieldDoesNotExist() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		A result = Fields.getIgnoreAccessByPaths(b, "z");

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullForEmptySearch() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		A result = Fields.getIgnoreAccessByPaths(b, "");

		assertThat(result, nullValue());

		result = Fields.getIgnoreAccessByPaths(b, new String[] {});

		assertThat(result, nullValue());
	}
}
