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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for
 * <ul>
 * <li>{@link Fields.IgnoreAccess#getByPaths(Object, String)}</li>
 * <li>{@link Fields.IgnoreAccess#getByPaths(Object, String...)}</li>
 * <li>{@link Fields.IgnoreAccess#getByPath(Object, String)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsIgnoreAccessGetByPathsTest {

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
	void shouldGetFieldByPath() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		String x = Fields.IgnoreAccess.getByPath(b, "a.x");

		assertThat(x, equalTo(TEST_STRING));
	}

	@Test
	void shouldReturnNullOnGetFieldByPathWhenFieldDoesNotExist() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		String x = Fields.IgnoreAccess.getByPath(b, "x.y");

		assertThat(x, equalTo(null));
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

		result = Fields.IgnoreAccess.getByPaths(b, new String[] { });

		assertThat(result, nullValue());
	}

	@Test
	void shouldThrowWhenPathsArrayIsNull() {
		B b = new B();

		ReflectionException exception = assertThrows(ReflectionException.class,
				() -> Fields.IgnoreAccess.getByPaths(b, (String[]) null));

		assertThat(exception.getMessage(), equalTo("Cannot resolve field paths: paths are null"));
	}

	@Test
	void shouldThrowWhenCommaSeparatedPathsAreNull() {
		B b = new B();

		ReflectionException exception = assertThrows(ReflectionException.class,
				() -> Fields.IgnoreAccess.getByPaths(b, (String) null));

		assertThat(exception.getMessage(), equalTo("Cannot resolve field paths: paths are null"));
	}

	@Test
	void shouldThrowWhenSinglePathIsNull() {
		B b = new B();

		ReflectionException exception = assertThrows(ReflectionException.class,
				() -> Fields.IgnoreAccess.getByPath(b, null));

		assertThat(exception.getMessage(), equalTo("Cannot resolve field path: path is null"));
	}

	@Test
	void shouldSkipNullVarargsPath() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		var result = Fields.IgnoreAccess.getByPaths(b, null, "a.x");

		assertThat(result, equalTo(TEST_STRING));
	}

	@Test
	void shouldResolveFirstNonNullVarargsPath() {
		A a = new A();
		a.x = TEST_STRING;
		B b = new B();
		b.a = a;

		var result = Fields.IgnoreAccess.getByPaths(b, "missing", "a.x");

		assertThat(result, equalTo(TEST_STRING));
	}
}
