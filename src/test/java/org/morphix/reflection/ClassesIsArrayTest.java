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

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Classes#isArray(Class)}</li>
 * <li>{@link Classes#isNotArray(Class)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class ClassesIsArrayTest {

	@Test
	void shouldReturnTrueForArrayTypes() {
		assertThat(Classes.isArray(String[].class), equalTo(true));
		assertThat(Classes.isArray(int[].class), equalTo(true));
		assertThat(Classes.isArray(Object[][].class), equalTo(true));
	}

	@Test
	void shouldReturnFalseForNonArrayTypes() {
		assertThat(Classes.isArray(String.class), equalTo(false));
		assertThat(Classes.isArray(int.class), equalTo(false));
		assertThat(Classes.isArray(Object.class), equalTo(false));
	}

	@Test
	void shouldReturnFalseForNullClassOnIsArray() {
		assertThat(Classes.isArray(null), equalTo(false));
	}

	@Test
	void shouldReturnTrueForNonArrayTypesOnIsNotArray() {
		assertThat(Classes.isNotArray(String.class), equalTo(true));
		assertThat(Classes.isNotArray(int.class), equalTo(true));
	}

	@Test
	void shouldReturnFalseForArrayTypesOnIsNotArray() {
		assertThat(Classes.isNotArray(String[].class), equalTo(false));
		assertThat(Classes.isNotArray(int[].class), equalTo(false));
	}

	@Test
	void shouldReturnTrueForNullClassOnIsNotArray() {
		assertThat(Classes.isNotArray(null), equalTo(true));
	}
}
