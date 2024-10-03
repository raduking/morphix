package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Constructors#newInstanceIgnoreAccess(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionGetDeclaredConstructorWithParamsTest {

	public static class B {
		private final int i;
		private String s;

		public B(final int i) {
			this.i = i;
		}

		public B(final int i, final String s) {
			this.i = i;
			this.s = s;
		}

		public int getI() {
			return i;
		}

		public String getS() {
			return s;
		}
	}

	public static class C {
		private final long x;

		private C(final long x) {
			this.x = x;
		}

		public long getX() {
			return x;
		}
	}

	@Test
	void shouldFindConstructor() {
		Constructor<B> constructor = Constructors.getDeclaredConstructor(B.class, int.class, String.class);

		assertNotNull(constructor);
	}

	@Test
	void shouldCreateNewInstanceWithConstructor() {
		Constructor<B> constructor = Constructors.getDeclaredConstructor(B.class, int.class, String.class);
		B a = Constructors.newInstanceIgnoreAccess(constructor, 10, "test");

		assertNotNull(a);
		assertThat(a.getI(), equalTo(10));
		assertThat(a.getS(), equalTo("test"));
	}

	@Test
	void shouldThrowExceptionIfNoConstructorIsFoundWithGivenParameters() {
		assertThrows(ReflectionException.class, () -> Constructors.getDeclaredConstructor(B.class, String.class));
	}

	@Test
	void shouldReturnPrivateConstructor() {
		Constructor<C> constructor = Constructors.getDeclaredConstructor(C.class, long.class);

		assertNotNull(constructor);
	}

}
