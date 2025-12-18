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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Messages}.
 *
 * @author Radu Sebastian LAZIN
 */
class MessagesTest {

	@Test
	void shouldInterpolateMessageCorrectly() {
		String result = Messages.message("Hello, {}!", "World");

		assertThat(result, equalTo("Hello, World!"));
	}

	@Test
	void shouldReturnTheSameMessageIfNoPlaceholdersArePresent() {
		String result = Messages.message("Hello, World!", "Ignored");

		assertThat(result, equalTo("Hello, World!"));
	}

	@Test
	void shouldReturnTheSameMessageIfNoArgumentsAreProvided() {
		String result = Messages.message("Hello, {} and {}!");

		assertThat(result, equalTo("Hello, {} and {}!"));
	}

	@Test
	void shouldReturnTheSameMessageIfArgumentsAreNull() {
		String result = Messages.message("Hello, {}!", (Object[]) null);

		assertThat(result, equalTo("Hello, {}!"));
	}

	@Test
	void shouldReturnTheSameMessageIfArgumentsAreEmpty() {
		String result = Messages.message("Hello, {}!", new Object[] { });

		assertThat(result, equalTo("Hello, {}!"));
	}

	@Test
	void shouldReturnNullMessageIfMessageIsNull() {
		String result = Messages.message(null, "Ignored");

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnTheMessageWithCorrectPlaceholderInsertedIfPlaceholderIsStartedButNotEnded() {
		String result = Messages.message("Value: { is {}", 42);

		assertThat(result, equalTo("Value: { is 42"));
	}

	@Test
	void shouldReturnTheSameMessageIfPlaceholderIsStartedButNotEndedAtTheEndOfTheMessage() {
		String result = Messages.message("Value: {", 42);

		assertThat(result, equalTo("Value: {"));
	}

	@Test
	void shouldHandleMorePlaceholdersThanArguments() {
		String result = Messages.message("Values: {}, {}, {}", 1, 2);

		assertThat(result, equalTo("Values: 1, 2, {}"));
	}
}
