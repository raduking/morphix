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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Classes#getOne(String)}</li>
 * <li>{@link Classes#getOne(String, ClassLoader)}</li>
 * <li>{@link Classes.Safe#getOne(String)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class ClassesGetOneTest {

	private static final String UNKNOWN = "<unknown>";

	@Test
	void shouldReturnTheClass() {
		Class<?> cls = Classes.getOne("java.lang.String");

		assertThat(cls, equalTo(String.class));
	}

	@Test
	void shouldThrowExceptionIfClassIsNotFoundOnGetClass() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Classes.getOne(UNKNOWN));

		assertThat(e.getMessage(), equalTo("Could not load class: " + UNKNOWN));
		assertThat(e.getCause().getClass(), equalTo(ClassNotFoundException.class));
	}

	@Test
	void shouldSafeReturnTheClass() {
		Class<?> cls = Classes.Safe.getOne("java.lang.Integer");

		assertThat(cls, equalTo(Integer.class));
	}

	@Test
	void shouldSafeReturnNullIfClassIsNotFound() {
		Class<?> cls = Classes.Safe.getOne(UNKNOWN);

		assertThat(cls, nullValue());
	}

	@Test
	void shouldReturnTheClassWithClassLoader() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class<?> cls = Classes.getOne("java.lang.String", classLoader);

		assertThat(cls, equalTo(String.class));
	}

	@Test
	void shouldThrowExceptionIfClassIsNotFoundOnGetClassWithClassLoader() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		ReflectionException e = assertThrows(ReflectionException.class, () -> Classes.getOne(UNKNOWN, classLoader));

		assertThat(e.getMessage(), equalTo("Could not load class: " + UNKNOWN + ", using class loader: " + classLoader.getName()));
		assertThat(e.getCause().getClass(), equalTo(ClassNotFoundException.class));
	}
}
