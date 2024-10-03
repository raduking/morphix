package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

/**
 * Test class for
 * {@link Methods#getSafeDeclaredMethodInHierarchy(String, Class, Class...)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionGetSafeDeclaredMethodInHierarchyTest {

	public enum E {
		// empty enum
	}

	public static class A {
		public void fooA() {
			// empty
		}
	}

	public static class B extends A {
		public void fooB() {
			// empty
		}
	}

	public static class C {
		// empty class
	}

	@Test
	void shouldReturnDeclaredMethodInHierarchy() throws Exception {
		Method expected = A.class.getDeclaredMethod("fooA");
		Method result = Methods.getSafeDeclaredMethodInHierarchy("fooA", B.class);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnNullIfMethodNotFound() {
		Method method = Methods.getSafeDeclaredMethodInHierarchy("$NonExistingMethod$", C.class);

		assertThat(method, equalTo(null));

		method = Methods.getSafeDeclaredMethodInHierarchy("$NonExistingMethod$", B.class);

		assertThat(method, equalTo(null));
	}

}
