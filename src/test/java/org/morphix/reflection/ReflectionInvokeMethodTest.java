package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.A;

/**
 * Test class for {@link Methods#invokeIgnoreAccess(Method, Object, Object...)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionInvokeMethodTest {

	private static final String TEST_STRING = "Test";

	public static class StaticA {
		static String s;

		public static void foo(final String s) {
			StaticA.s = s;
		}
	}

	@Test
	void shouldInvokeMethod() throws Exception {
		A obj = new A();

		Method method = A.class.getDeclaredMethod("foo", String.class);
		Methods.invokeIgnoreAccess(method, obj, TEST_STRING);

		assertThat(obj.getS(), equalTo(TEST_STRING));
	}

	@Test
	void shouldKeepAccessModifierOnInvokeMethod() throws Exception {
		A obj = new A();

		Method method = A.class.getDeclaredMethod("fooPrivate", String.class);
		Methods.invokeIgnoreAccess(method, obj, TEST_STRING);

		assertThat(method.canAccess(obj), equalTo(false));
	}

	@Test
	void shouldThrowConverterExceptionWhenInvokeWithInvalidArgument() throws Exception {
		A obj = new A();

		Method method = A.class.getDeclaredMethod("foo", String.class);
		assertThrows(ReflectionException.class, () -> Methods.invokeIgnoreAccess(method, obj, obj));
	}

	@Test
	void shouldInvokeStaticMethod() throws Exception {
		Method method = StaticA.class.getDeclaredMethod("foo", String.class);
		Methods.invokeIgnoreAccess(method, null, TEST_STRING);

		assertThat(StaticA.s, equalTo(TEST_STRING));
	}

	@Test
	void shouldInvokeClassMethod() throws Exception {
		Method method = Class.class.getDeclaredMethod("getName");
		String name = Methods.invokeIgnoreAccess(method, Class.class);

		assertThat(name, equalTo(Class.class.getName()));
	}

	@Test
	void shouldThrowConverterExceptionWhenFailInvokeClassMethod() throws Exception {
		Method method = Class.class.getDeclaredMethod("forName", String.class);
		assertThrows(ReflectionException.class, () -> Methods.invokeIgnoreAccess(method, Class.class, "$NonExistingClass$"));
	}

	@Test
	void shouldThrowConverterExceptionWhenFailWithBadArgumentsInvokeClassMethod() throws Exception {
		Method method = Class.class.getDeclaredMethod("forName", String.class);
		assertThrows(ReflectionException.class, () -> Methods.invokeIgnoreAccess(method, Class.class));
	}

	@Test
	void shouldThrowConverterExceptionWhenInvokeStaticMethodWithInvalidArgument() throws Exception {
		Method method = StaticA.class.getDeclaredMethod("foo", String.class);
		assertThrows(ReflectionException.class, () -> Methods.invokeIgnoreAccess(method, null, Class.class));
	}

	public static class B {
		public void foo() {
			throw new NullPointerException();
		}
	}

	public static class StaticB {
		@SuppressWarnings("unused")
		public static void foo(final String s) {
			throw new NullPointerException();
		}
	}

	@Test
	void shouldThrowConverterExceptionWhenInvokeFailsWithCause() throws Exception {
		B obj = new B();

		Method method = B.class.getDeclaredMethod("foo");
		assertThrows(ReflectionException.class, () -> Methods.invokeIgnoreAccess(method, obj));
	}

	@Test
	void shouldThrowConverterExceptionWhenInvokeStaticMethodFailsWithCause() throws Exception {
		Method method = StaticB.class.getDeclaredMethod("foo", String.class);
		assertThrows(ReflectionException.class, () -> Methods.invokeIgnoreAccess(method, null, TEST_STRING));
	}

}
