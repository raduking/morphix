package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

/**
 * Test class {@link Reflection#getClassWithPrefix(Class, String)}
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionGetSetterMethodTest {

	static class A {

		private String s;

		@SuppressWarnings("unused")
		private String t;

		private Boolean b;

		@SuppressWarnings("unused")
		private Integer i;

		public String getS() {
			return s;
		}

		public void setS(final String s) {
			this.s = s;
		}

		public Boolean getB() {
			return b;
		}

		public void setB(final boolean b) {
			this.b = b;
		}

	}

	@Test
	void shouldReturnSetterMethod() throws Exception {
		Field field = A.class.getDeclaredField("s");

		Method expected = A.class.getDeclaredMethod("setS", String.class);

		Method result = Methods.getSetterMethod(A.class, field);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldThrowExceptionWhenSetterMethodNotFound() throws Exception {
		Field field = A.class.getDeclaredField("t");

		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.getSetterMethod(A.class, field));

		assertThat(e.getMessage(), equalTo("Error finding method: "
				+ "setT(" + field.getType().getCanonicalName() + ")"));
	}

	@Test
	void shouldReturnSetterMethodWithPrimitive() throws Exception {
		Field field = A.class.getDeclaredField("b");

		Method expected = A.class.getDeclaredMethod("setB", boolean.class);

		Method result = Methods.getSetterMethod(A.class, field);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldThrowExceptionWhenSetterMethodWithPrimitiveNotFound() throws Exception {
		Field field = A.class.getDeclaredField("i");

		ReflectionException e = assertThrows(ReflectionException.class, () -> Methods.getSetterMethod(A.class, field));

		assertThat(e.getMessage(), equalTo("Error finding method: "
				+ "setI(" + field.getType().getCanonicalName() + ") or "
				+ "setI(" + int.class.getCanonicalName() + ")"));
	}

}
