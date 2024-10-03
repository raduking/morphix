package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Methods#getDeclaredMethodsInHierarchy(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionGetDeclaredMethodsInHierarchyTest {

	public enum E {
		// empty enum
	}

	public static class A {
		void fooA() {
			// empty
		}
	}

	public static class B extends A {
		void fooB() {
			// empty
		}
	}

	public static class C {
		// empty class
	}

	@Test
	void shouldGetAllMethodsInHierarchy() {
		List<Method> fields = Methods.getDeclaredMethodsInHierarchy(B.class);

		int sizeB = B.class.getDeclaredMethods().length;
		int sizeA = A.class.getDeclaredMethods().length;

		assertThat(fields, hasSize(sizeA + sizeB));
	}

	@Test
	void shouldReturnEmptyListForClassesWithNoMethods() {
		List<Method> fields = Methods.getDeclaredMethodsInHierarchy(C.class);

		int size = C.class.getDeclaredMethods().length;

		assertThat(fields, hasSize(size));
	}

	@Test
	void shouldReturnEnumClassMethodsListForEmptyEnumsToo() {
		List<Method> fields = Methods.getDeclaredMethodsInHierarchy(E.class);

		int sizeEnum = Enum.class.getDeclaredMethods().length;
		int sizeE = E.class.getDeclaredMethods().length;

		assertThat(fields, hasSize(sizeEnum + sizeE));
	}

}
