package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for
 * {@link Methods#getGenericReturnType(Method, int)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsGetGenericReturnTypeTest {

	public static class A {

		public List<String> getList1() {
			return null;
		}

		public List<List<String>> getList2() {
			return null;
		}

		@SuppressWarnings("rawtypes")
		public List getList3() {
			return null;
		}
	}

	@Test
	void shouldReturnCorrectClass() throws Exception {
		Method method = A.class.getMethod("getList1");
		Class<?> cls = Methods.getGenericReturnClass(method, 0);

		assertThat(cls, equalTo(String.class));
	}

	@Test
	void shouldFailToCastFromParameterizedClass() throws Exception {
		Method method = A.class.getMethod("getList2");
		assertThrows(ReflectionException.class, () -> Methods.getGenericReturnClass(method, 0));
	}

	@Test
	void shouldFailForRawTypes() throws Exception {
		Method method = A.class.getMethod("getList3");
		assertThrows(ReflectionException.class, () -> Methods.getGenericReturnClass(method, 0));
	}

}
