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
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Reflection#requireNotNull(Object)} and {@link Reflection#requireNotNull(Object, String)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionRequireNotNullTest {

	private static final String VALUE = "value";
	private static final String MESSAGE = "paths";

	@Test
	void shouldReturnTheSameReferenceWhenItIsNotNull() {
		assertThat(Reflection.requireNotNull(VALUE), sameInstance(VALUE));
		assertThat(Reflection.requireNotNull(VALUE, MESSAGE), sameInstance(VALUE));
	}

	@Test
	void shouldThrowWhenTheReferenceIsNull() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Reflection.requireNotNull(null));

		assertThat(e.getMessage(), equalTo("Object cannot be null"));
	}

	@Test
	void shouldThrowWithTheGivenMessageWhenTheReferenceIsNull() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Reflection.requireNotNull(null, MESSAGE));

		assertThat(e.getMessage(), equalTo(MESSAGE));
	}

}
