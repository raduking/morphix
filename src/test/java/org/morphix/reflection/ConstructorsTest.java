package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Constructors#newInstance(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConstructorsTest {

	public static class A {
		// empty
	}

	public static class B {
		private int i;

		public B(final int i) {
			this.i = i;
		}

		public int getI() {
			return i;
		}
	}

	public static class C {
		private C() {
			// empty
		}
	}

	@Test
	void shouldCreateNewInstance() {
		A a = Constructors.newInstance(A.class);

		assertNotNull(a);
	}

	@Test
	void shouldThrowExceptionIfNoDefaultConstructorIsFound() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Constructors.newInstance(B.class));
		assertThat(e.getMessage(), startsWith("Default constructor is not defined for class: "));
	}

	@Test
	void shouldThrowExceptionIfNewInstanceWithPrivateConstructor() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Constructors.newInstance(C.class));
		assertThat(e.getMessage(), startsWith("Default constructor is not accessible for class: "));
	}

	public static class D {
		public D() {
			throw new NullPointerException();
		}
	}

	@Test
	void shouldPropagateExceptionIfConstructorThrowsException() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Constructors.newInstance(D.class));
		assertThat(e.getMessage(), startsWith("Could not instantiate class, default constructor threw exception: "));
	}

	public static abstract class E {
		public E() {
			// empty
		}
	}

	@Test
	void shouldPropagateExceptionIfClassIsAbstract() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Constructors.newInstance(E.class));
		assertThat(e.getMessage(), startsWith("Could not instantiate class,"
				+ " the class object represents an abstract class, an interface,"
				+ " an array class, a primitive type, or void: "));
	}

}
