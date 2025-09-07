/*
 * Copyright 2025 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Constructors#newInstance(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConstructorsGetDeclaredConstructorWithParamsTest {

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
		B a = Constructors.IgnoreAccess.newInstance(constructor, 10, "test");

		assertNotNull(a);
		assertThat(a.getI(), equalTo(10));
		assertThat(a.getS(), equalTo("test"));
	}

	@Test
	void shouldThrowExceptionIfNoConstructorIsFoundWithGivenParameters() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Constructors.getDeclaredConstructor(B.class, String.class));
		assertThat(e.getMessage(),
				equalTo("No constructor found for class: " + B.class.getCanonicalName() + " with parameters: [class java.lang.String]"));
	}

	@Test
	void shouldThrowExceptionIfNoConstructorIsFoundWithNullParameters() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Constructors.getDeclaredConstructor(B.class, (Class<?>[]) null));
		assertThat(e.getMessage(), equalTo("No constructor found for class: " + B.class.getCanonicalName() + " with parameters: none"));
	}

	@Test
	void shouldReturnPrivateConstructor() {
		Constructor<C> constructor = Constructors.getDeclaredConstructor(C.class, long.class);

		assertNotNull(constructor);
	}

}
