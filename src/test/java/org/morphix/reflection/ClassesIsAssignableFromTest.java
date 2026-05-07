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
 * Test class for: {@link Classes#isAssignableFrom(Class, Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClassesIsAssignableFromTest {

	@Test
	void shouldReturnTrueForAssignableClasses() {
		assertThat(Classes.isAssignableFrom(Number.class, Integer.class), equalTo(true));
		assertThat(Classes.isAssignableFrom(Object.class, String.class), equalTo(true));
		assertThat(Classes.isAssignableFrom(String.class, String.class), equalTo(true));
	}

	@Test
	void shouldReturnFalseForNonAssignableClasses() {
		assertThat(Classes.isAssignableFrom(Integer.class, Number.class), equalTo(false));
		assertThat(Classes.isAssignableFrom(String.class, Object.class), equalTo(false));
		assertThat(Classes.isAssignableFrom(Integer.class, String.class), equalTo(false));
	}

	@Test
	void shouldReturnTrueForPrimitiveTypes() {
		assertThat(Classes.isAssignableFrom(int.class, int.class), equalTo(true));
		assertThat(Classes.isAssignableFrom(Integer.class, int.class), equalTo(true));
		assertThat(Classes.isAssignableFrom(int.class, Integer.class), equalTo(true));
		assertThat(Classes.isAssignableFrom(Double.class, double.class), equalTo(true));
		assertThat(Classes.isAssignableFrom(double.class, Double.class), equalTo(true));
		assertThat(Classes.isAssignableFrom(double.class, double.class), equalTo(true));
		assertThat(Classes.isAssignableFrom(Number.class, double.class), equalTo(true));
	}

	@Test
	void shouldReturnFalseForUnassignablePrimitiveTypes() {
		assertThat(Classes.isAssignableFrom(Long.class, int.class), equalTo(false));
		assertThat(Classes.isAssignableFrom(Integer.class, Number.class), equalTo(false));
		assertThat(Classes.isAssignableFrom(double.class, Long.class), equalTo(false));
	}

	@Test
	void shouldReturnFalseForNullClasses() {
		assertThat(Classes.isAssignableFrom(Integer.class, null), equalTo(false));
		assertThat(Classes.isAssignableFrom(null, Number.class), equalTo(false));
	}
}
