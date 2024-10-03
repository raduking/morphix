package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.morphix.testdata.MethodName;

/**
 * Test class for {@link Methods#getCallerMethodName(Supplier, BiFunction)}
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsGetCallerMethodNameTest {

	private static final String SOME_STRING = "some-string";
	private static final String METHOD_NAME_SEPARATOR = "/";
	private static final String TEST_CLASS_NAME = MethodsGetCallerMethodNameTest.class.getCanonicalName();

	@Test
	void shouldReturnCallerMethodName() {
		A a = new A();
		String methodName = a.foo(() -> SOME_STRING);

		assertThat(methodName, equalTo(A.class.getTypeName() + METHOD_NAME_SEPARATOR + "foo"));
	}

	@Test
	void shouldReturnCallerMethodNameForDifferentMethod() {
		A a = new A();
		String methodName = a.goo(() -> SOME_STRING);

		assertThat(methodName, equalTo(A.class.getTypeName() + MethodName.SEPARATOR + "goo"));
	}

	@Test
	void shouldReturnCallerMethodNameForTestMethod() {
		String methodName = Methods.getCallerMethodName(() -> SOME_STRING, MethodsGetCallerMethodNameTest::nameFunction).get();

		assertThat(methodName, equalTo(TEST_CLASS_NAME + METHOD_NAME_SEPARATOR + "shouldReturnCallerMethodNameForTestMethod"));
	}

	@Test
	void shouldCallNameFunctionOnlyOnce() {
		A a = spy(new A());
		String methodName = Methods.getCallerMethodName(() -> SOME_STRING, a::nameFunction).get();

		assertThat(methodName, equalTo(TEST_CLASS_NAME + METHOD_NAME_SEPARATOR + "shouldCallNameFunctionOnlyOnce"));
		verify(a).nameFunction(TEST_CLASS_NAME, "shouldCallNameFunctionOnlyOnce");
	}

	private static String nameFunction(final String className, final String methodName) {
		return className + METHOD_NAME_SEPARATOR + methodName;
	}

	public static class A {

		public String foo(final Supplier<String> supplier) {
			return Methods.getCallerMethodName(supplier, MethodsGetCallerMethodNameTest::nameFunction).get();
		}

		public String goo(final Supplier<String> supplier) {
			return MethodName.get(supplier).get();
		}

		public String nameFunction(final String className, final String methodName) {
			return MethodsGetCallerMethodNameTest.nameFunction(className, methodName);
		}

	}

}
