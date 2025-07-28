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
import org.morphix.reflection.testdata.C;

/**
 * Test class for {@link Constructors#newInstance(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConstructorsNewInstanceIgnoreAccessTest {

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
		A a = Constructors.IgnoreAccess.newInstance(A.class);

		assertNotNull(a);
	}

	@Test
	void shouldThrowExceptionIfNoDefaultConstructorIsFound() {
		assertThrows(ReflectionException.class, () -> Constructors.IgnoreAccess.newInstance(B.class));
	}

	@Test
	void shouldCreateNewInstanceWithPrivateConstructor() {
		C c = Constructors.IgnoreAccess.newInstance(C.class);

		assertNotNull(c);
	}

	public static class D {
		public D() {
			throw new NullPointerException();
		}
	}

	@Test
	void shouldThrowExceptionIfConstructorThrowsException() {
		assertThrows(ReflectionException.class, () -> Constructors.IgnoreAccess.newInstance(D.class));
	}

	@Test
	void shouldCreateNewInstanceWithConstructor() throws Exception {
		Constructor<A> ctor = A.class.getDeclaredConstructor();
		A a = Constructors.IgnoreAccess.newInstance(ctor);

		assertNotNull(a);
	}

	@Test
	void shouldKeepAccessModifiersUnchangedAfterCall() throws Exception {
		Constructor<C> ctor = C.class.getDeclaredConstructor();
		Constructors.IgnoreAccess.newInstance(ctor);

		assertThat(ctor.canAccess(null), equalTo(false));
	}
}
