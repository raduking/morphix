package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.morphix.annotation.Expandable;

class MethodsInvokeWithAnnotationTest {

	private static final int I11 = 11;
	private static final int I22 = 22;

	public static class A {

		private int x = I11;

		@Expandable
		public void foo() {
			x = I22;
		}

		public int getX() {
			return x;
		}

	}

	@Test
	void shouldInvokeMethods() {
		A a = new A();

		Methods.invokeMethodsWithAnnotation(a, Expandable.class);

		assertThat(a.getX(), equalTo(I22));
	}

	public static class B extends A {

		private int y = I11;

		@Expandable
		public void goo() {
			y = I22;
		}

		public int getY() {
			return y;
		}

	}

	@Test
	void shouldInvokeMethodsInHierarchy() {
		B b = new B();

		Methods.invokeMethodsWithAnnotation(b, Expandable.class);

		assertThat(b.getX(), equalTo(I22));
		assertThat(b.getY(), equalTo(I22));
	}

}
