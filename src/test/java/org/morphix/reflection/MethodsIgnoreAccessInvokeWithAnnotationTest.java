/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.morphix.convert.annotation.Expandable;

/**
 * Test class for {@link Methods.IgnoreAccess#invokeWithAnnotation(Object, Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodsIgnoreAccessInvokeWithAnnotationTest {

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

		Methods.IgnoreAccess.invokeWithAnnotation(a, Expandable.class);

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

		Methods.IgnoreAccess.invokeWithAnnotation(b, Expandable.class);

		assertThat(b.getX(), equalTo(I22));
		assertThat(b.getY(), equalTo(I22));
	}

	public static class WithStaticAndInstance {

		static int statX = I11;

		private int instX = I11;

		static void reset() {
			statX = I11;
		}

		@Expandable
		public static void statFoo() {
			statX = I22;
		}

		@Expandable
		public void instFoo() {
			instX = I22;
		}

		public static int getStatX() {
			return statX;
		}

		public int getInstX() {
			return instX;
		}

	}

	@Test
	void shouldInvokeOnlyStaticMethodsWhenAClassIsGiven() {
		WithStaticAndInstance.reset();

		Methods.IgnoreAccess.invokeWithAnnotation(WithStaticAndInstance.class, Expandable.class);

		assertThat(WithStaticAndInstance.getStatX(), equalTo(I22));
	}

	@Test
	void shouldInvokeBothStaticAndInstanceMethodsWhenAnInstanceIsGiven() {
		WithStaticAndInstance.reset();
		WithStaticAndInstance obj = new WithStaticAndInstance();

		Methods.IgnoreAccess.invokeWithAnnotation(obj, Expandable.class);

		assertThat(WithStaticAndInstance.getStatX(), equalTo(I22));
		assertThat(obj.getInstX(), equalTo(I22));
	}

	public static interface WithInterfaceMethods {

		@Expandable
		static void statFoo() {
			Holder.statX = I22;
		}

		@Expandable
		default void defaultFoo() {
			Holder.defaultX = I22;
		}

	}

	public static class Holder {

		static int statX = I11;

		static int defaultX = I11;

		static void reset() {
			statX = I11;
			defaultX = I11;
		}

	}

	@Test
	void shouldInvokeOnlyStaticMethodsOfAnInterfaceWhenTheInterfaceClassIsGiven() {
		Holder.reset();

		Methods.IgnoreAccess.invokeWithAnnotation(WithInterfaceMethods.class, Expandable.class);

		assertThat(Holder.statX, equalTo(I22));
		assertThat(Holder.defaultX, equalTo(I11));
	}

	@Test
	void shouldNotInvokeMethodsDeclaredByInterfacesWhenAnInstanceIsGiven() {
		Holder.reset();
		WithInterfaceMethods obj = new WithInterfaceMethods() {
			// anonymous implementation
		};

		Methods.IgnoreAccess.invokeWithAnnotation(obj, Expandable.class);

		assertThat(Holder.statX, equalTo(I11));
		assertThat(Holder.defaultX, equalTo(I11));
	}

}
