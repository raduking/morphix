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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Constructors.Safe#getDefault(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConstructorsSafeGetDefaultTest {

	public static class A {
		// default no-args constructor
	}

	@Test
	void shouldReturnDefaultNoArgsConstructor() {
		Constructor<A> constructor = Constructors.Safe.getDefault(A.class);

		assertNotNull(constructor);
	}

	public static class B {
		private B() {
			// private constructor
		}
	}

	@Test
	void shouldReturnPrivateNoArgsConstructor() {
		Constructor<B> constructor = Constructors.Safe.getDefault(B.class);

		assertNotNull(constructor);
	}

	public static class C {
		public C(@SuppressWarnings("unused") final String arg) {
			// public constructor with argument
		}
	}

	@Test
	void shouldReturnNullWhenNoNoArgsConstructor() {
		Constructor<C> constructor = Constructors.Safe.getDefault(C.class);

		assertNull(constructor);
	}
}
