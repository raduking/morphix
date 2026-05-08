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
import static org.hamcrest.Matchers.hasSize;

import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for
 *
 * <ul>
 * <li>{@link Constructors#findAllMatching(Class, List)}</li>
 * <li>{@link Constructors#findAllMatching(Class, Class...)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class ConstructorsFindOneMatchingTest {

	public static class B {

		public B() {
			// empty default constructor
		}

		@SuppressWarnings("unused")
		public B(final Number i, final String s) {
			// empty constructor
		}

		@SuppressWarnings("unused")
		public B(final Integer i, final String s) {
			// empty constructor
		}

		@SuppressWarnings("unused")
		public B(final int i, final String s) {
			// empty constructor
		}

		@SuppressWarnings("unused")
		public B(final Object i, final String s) {
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
	class FindAllMatchingWithList {

		@Test
		void shouldFindAllMatchingConstructors() {
			List<Constructor<B>> constructors = Constructors.findAllMatching(B.class, List.of(Integer.class, String.class));

			assertThat(constructors, hasSize(4));
		}

		@Test
		void shouldFindDefaultConstructor() {
			List<Constructor<B>> constructors = Constructors.findAllMatching(B.class, List.of());

			assertThat(constructors, hasSize(1));
		}

		@Test
		void shouldNotFindAnyConstructors() {
			List<Constructor<C>> constructors = Constructors.findAllMatching(C.class, List.of(Integer.class, String.class));

			assertThat(constructors, hasSize(0));
		}
	}

	@Nested
	class FindOneMatchingWithVarargs {

		@Test
		void shouldFindAllMatchingConstructors() {
			List<Constructor<B>> constructors = Constructors.findAllMatching(B.class, Integer.class, String.class);

			assertThat(constructors, hasSize(4));
		}

		@Test
		void shouldFindDefaultConstructor() {
			List<Constructor<B>> constructors = Constructors.findAllMatching(B.class);

			assertThat(constructors, hasSize(1));
		}

		@Test
		void shouldNotFindAnyConstructors() {
			List<Constructor<C>> constructors = Constructors.findAllMatching(C.class, Integer.class, String.class);

			assertThat(constructors, hasSize(0));
		}
	}
}
