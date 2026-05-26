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

import org.junit.jupiter.api.Test;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Classes#getFrom(Object)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class ClassesGetFromTest {

	@Test
	void shouldReturnTheClass() {
		Class<?> cls = Classes.getFrom(Class.class);

		assertThat(cls, equalTo(Class.class));
	}

	@Test
	void shouldReturnNullFromNull() {
		Class<?> cls = Classes.getFrom(null);

		assertThat(cls, nullValue());
	}

	@Test
	void shouldReturnClassFormObject() {
		Class<?> cls = Classes.getFrom(new Object());

		assertThat(cls, equalTo(Object.class));
	}
}
