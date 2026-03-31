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
package org.morphix.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OptionalLibrary}.
 *
 * @author Radu Sebastian LAZIN
 */
class OptionalLibraryTest {

	static class TestType {
		// empty
	}

	@Test
	void shouldIndicateLibraryIsPresent() {
		OptionalLibrary<TestType> descriptor = OptionalLibrary.present(TestType.class);

		assertThat(descriptor.isPresent(), is(true));
	}

	@Test
	void shouldIndicateLibraryIsNotPresent() {
		OptionalLibrary<TestType> descriptor = OptionalLibrary.notPresent(TestType.class);

		assertThat(descriptor.isPresent(), is(false));
	}

	@Test
	void shouldReturnSpecificClass() {
		OptionalLibrary<TestType> descriptor = OptionalLibrary.present(TestType.class);

		assertThat(descriptor.getSpecificClass(), sameInstance(TestType.class));
	}

	@Test
	void shouldFailWhenSpecificClassIsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> OptionalLibrary.present(null));

		assertThat(exception.getMessage(), is("specificClass must not be null"));
	}

	@Test
	void shouldReturnTrueForPresentClassDescriptor() {
		OptionalLibrary<TestType> descriptor = OptionalLibrary.of(TestType.class.getName(), TestType.class);

		assertThat(descriptor.isPresent(), is(true));
	}

	@Test
	void shouldReturnFalseForNotPresentClassDescriptor() {
		OptionalLibrary<TestType> descriptor = OptionalLibrary.of(TestType.class.getName() + "$NonExistentClass", TestType.class);

		assertThat(descriptor.isPresent(), is(false));
	}
}
