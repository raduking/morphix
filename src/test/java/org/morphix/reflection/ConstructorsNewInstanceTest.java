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

import static org.hamcrest.CoreMatchers.equalTo;
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
class ConstructorsNewInstanceTest {

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

		assertThat(e.getMessage(), equalTo("Could not instantiate class, default constructor threw exception: "
				+ NullPointerException.class.getCanonicalName() + ", for class: " + D.class.getCanonicalName()));
	}

	public abstract static class E {
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
