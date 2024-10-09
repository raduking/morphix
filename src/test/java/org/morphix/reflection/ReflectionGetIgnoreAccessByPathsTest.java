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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for
 * <p>
 *
 * {@link Fields#getByPath(Object, String)}
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionGetIgnoreAccessByPathsTest {

	private static final String TEST_STRING = "testString";

	public static class A {
		public String x;
	}

	public static class B {
		public A a;
	}

	@Test
	void shouldGetFieldByPaths() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		String x = Fields.IgnoreAccess.getByPaths(b, "x.y,a.x");

		assertThat(x, equalTo(TEST_STRING));
	}

	@Test
	void shouldGetFieldByPathsAtFirstLevel() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		A result = Fields.IgnoreAccess.getByPaths(b, "a");

		assertThat(result, equalTo(a));
	}

	@Test
	void shouldReturnNullIfFieldDoesNotExist() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		A result = Fields.IgnoreAccess.getByPaths(b, "z");

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullForEmptySearch() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		A result = Fields.IgnoreAccess.getByPaths(b, "");

		assertThat(result, nullValue());

		result = Fields.IgnoreAccess.getByPaths(b, new String[] {});

		assertThat(result, nullValue());
	}
}
