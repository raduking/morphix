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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.typeCompatibleWith;

import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for
 *
 * <ul>
 * <li>{@link Constructors.Safe#findOneMatching(Class, List)}</li>
 * <li>{@link Constructors.Safe#findOneMatching(Class, Class...)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class ConstructorsSafeFindOneMatchingTest {

	public static class B {

		public B() {
			// empty default constructor
		}

		@SuppressWarnings("unused")
		public B(final Number i, final String s) {
			// empty constructor
		}
	}

	public static class C {

		@SuppressWarnings("unused")
		public C(final String s, final String s2) {
			// empty constructor
		}
	}

	@Nested
	class FindOneMatchingWithList {

		@Test
		void shouldFindOneMatchingConstructor() {
			Constructor<B> constructor = Constructors.Safe.findOneMatching(B.class, List.of(Integer.class, String.class));

			assertThat(constructor.getParameterCount(), equalTo(2));
			assertThat(constructor.getParameterTypes()[0], typeCompatibleWith(Number.class));
			assertThat(constructor.getParameterTypes()[1], equalTo(String.class));
		}

		@Test
		void shouldFindOneMatchingConstructorWithPrimitiveTypes() {
			Constructor<B> constructor = Constructors.Safe.findOneMatching(B.class, List.of(int.class, String.class));

			assertThat(constructor.getParameterCount(), equalTo(2));
			assertThat(constructor.getParameterTypes()[0], typeCompatibleWith(Number.class));
			assertThat(constructor.getParameterTypes()[1], equalTo(String.class));
		}

		@Test
		void shouldFindDefaultConstructor() {
			Constructor<B> constructor = Constructors.Safe.findOneMatching(B.class, List.of());

			assertThat(constructor.getParameterCount(), equalTo(0));
		}

		@Test
		void shouldReturnNullWhenNoMatchingConstructor() {
			List<Class<?>> paramTypes = List.of(String.class, String.class);
			Constructor<B> constructor = Constructors.Safe.findOneMatching(B.class, paramTypes);

			assertThat(constructor, equalTo(null));
		}

		@Test
		void shouldReturnNullWhenNoDefaultConstructorFound() {
			List<Class<?>> paramTypes = List.of();
			Constructor<C> constructor = Constructors.Safe.findOneMatching(C.class, paramTypes);

			assertThat(constructor, equalTo(null));
		}
	}

	@Nested
	class FindOneMatchingWithVarargs {

		@Test
		void shouldFindOneMatchingConstructor() {
			Constructor<B> constructor = Constructors.Safe.findOneMatching(B.class, Integer.class, String.class);

			assertThat(constructor.getParameterCount(), equalTo(2));
			assertThat(constructor.getParameterTypes()[0], typeCompatibleWith(Number.class));
			assertThat(constructor.getParameterTypes()[1], equalTo(String.class));
		}

		@Test
		void shouldFindOneMatchingConstructorWithPrimitiveTypes() {
			Constructor<B> constructor = Constructors.Safe.findOneMatching(B.class, int.class, String.class);

			assertThat(constructor.getParameterCount(), equalTo(2));
			assertThat(constructor.getParameterTypes()[0], typeCompatibleWith(Number.class));
			assertThat(constructor.getParameterTypes()[1], equalTo(String.class));
		}

		@Test
		void shouldFindDefaultConstructor() {
			Constructor<B> constructor = Constructors.Safe.findOneMatching(B.class, List.of());

			assertThat(constructor.getParameterCount(), equalTo(0));
		}

		@Test
		void shouldReturnNullWhenNoMatchingConstructor() {
			Constructor<B> constructor = Constructors.Safe.findOneMatching(B.class, String.class, String.class);

			assertThat(constructor, equalTo(null));
		}

		@Test
		void shouldReturnNullWhenNoDefaultConstructorFound() {
			Constructor<C> constructor = Constructors.Safe.findOneMatching(C.class);

			assertThat(constructor, equalTo(null));
		}
	}
}
