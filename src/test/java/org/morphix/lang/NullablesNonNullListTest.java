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
package org.morphix.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link Nullables} {@code nonNullList} methods.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class NullablesNonNullListTest {

	@Test
	void shouldReturnEmptyListWhenVarargsInputIsNull() {
		String[] input = null;

		List<String> result = Nullables.nonNullList(input);

		assertThat(result, is(empty()));
	}

	@Test
	void shouldReturnListOfElementsWhenVarargsInputIsNotNull() {
		String[] input = { "a", "b", "c" };

		List<String> result = Nullables.nonNullList(input);

		assertThat(result, contains("a", "b", "c"));
	}

	@Test
	void shouldReturnEmptyListWhenCollectionInputIsNull() {
		Collection<String> input = null;

		List<String> result = Nullables.nonNullList(input);

		assertThat(result, is(empty()));
	}

	@Test
	void shouldReturnListCopyWhenCollectionInputIsNotNull() {
		Collection<String> input = Arrays.asList("a", "b", "c");

		List<String> result = Nullables.nonNullList(input);

		assertThat(result, contains("a", "b", "c"));
		assertThat(result, is(not(sameInstance(input))));
	}

	@Test
	void shouldReturnEmptyListWhenListInputIsNull() {
		List<String> input = null;

		List<String> result = Nullables.nonNullList(input);

		assertThat(result, is(empty())); // Verify the result is an empty list
	}

	@Test
	void shouldReturnSameListWhenListInputIsNotNull() {
		List<String> input = Arrays.asList("a", "b", "c");

		List<String> result = Nullables.nonNullList(input);

		assertThat(result, is(sameInstance(input)));
	}
}
