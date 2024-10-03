package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.C;

/**
 * Test class for {@link Constructors#newInstanceIgnoreAccess(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionNewInstanceIgnoreAccessTest {

	public static class A {
		// empty
	}

	public static class B {
		private final int i;

		public B(final int i) {
			this.i = i;
		}

		public int getI() {
			return i;
		}
	}

	@Test
	void shouldCreateNewInstance() {
		A a = Constructors.newInstanceIgnoreAccess(A.class);

		assertNotNull(a);
	}

	@Test
	void shouldThrowExceptionIfNoDefaultConstructorIsFound() {
		assertThrows(ReflectionException.class, () -> Constructors.newInstanceIgnoreAccess(B.class));
	}

	@Test
	void shouldCreateNewInstanceWithPrivateConstructor() {
		C c = Constructors.newInstanceIgnoreAccess(C.class);

		assertNotNull(c);
	}

	public static class D {
		public D() {
			throw new NullPointerException();
		}
	}

	@Test
	void shouldThrowExceptionIfConstructorThrowsException() {
		assertThrows(ReflectionException.class, () -> Constructors.newInstanceIgnoreAccess(D.class));
	}

	@Test
	void shouldCreateNewInstanceWithConstructor() throws Exception {
		Constructor<A> ctor = A.class.getDeclaredConstructor();
		A a = Constructors.newInstanceIgnoreAccess(ctor);

		assertNotNull(a);
	}

	@Test
	void shouldKeepAccessModifiersUnchangedAfterCall() throws Exception {
		Constructor<C> ctor = C.class.getDeclaredConstructor();
		Constructors.newInstanceIgnoreAccess(ctor);

		assertThat(ctor.canAccess(null), equalTo(false));
	}
}
