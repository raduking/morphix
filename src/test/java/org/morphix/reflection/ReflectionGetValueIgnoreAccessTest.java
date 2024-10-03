package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields#getIgnoreAccess(Object, Field)}
 *
 * @author Radu Sebastian LAZIN
 */
public class ReflectionGetValueIgnoreAccessTest {

	public static final String TEST_STRING = "testString";
	public static final Long TEST_LONG = 17L;
	public static final Integer TEST_INTEGER = 13;

	public static class A {
		public String s;
		protected Long l;
		@SuppressWarnings("unused")
		private Integer i;
	}

	@Test
	void shouldIgnoreAccessOnReturningTheValue() throws Exception {
		A a = new A();
		a.s = TEST_STRING;
		a.l = TEST_LONG;
		a.i = TEST_INTEGER;

		String s = Fields.getIgnoreAccess(a, A.class.getDeclaredField("s"));
		Long l = Fields.getIgnoreAccess(a, A.class.getDeclaredField("l"));
		Integer i = Fields.getIgnoreAccess(a, A.class.getDeclaredField("i"));

		assertThat(s, equalTo(TEST_STRING));
		assertThat(l, equalTo(TEST_LONG));
		assertThat(i, equalTo(TEST_INTEGER));
	}

	@Test
	void shouldThrowExceptionOnInvalidField() throws Exception {
		Object o = new Object();
		Field s = A.class.getDeclaredField("s");
		assertThrows(ReflectionException.class, () -> Fields.getIgnoreAccess(o, s));
	}

}
