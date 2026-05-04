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
package org.morphix.lang.logging;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OneLineFormatter}.
 *
 * @author Radu Sebastian LAZIN
 */
class OneLineFormatterTest {

	@Test
	void shouldNotModifyBuilderIfThrowableIsNull() {
		StringBuilder builder = new StringBuilder();

		OneLineFormatter.appendThrowable(builder, null, "");

		assertEquals("", builder.toString());
	}

	@Test
	void shouldNotModifyBuilderIfThrowableIsNullWithVisitedSet() {
		StringBuilder builder = new StringBuilder();

		OneLineFormatter.appendThrowable(builder, null, "", Set.of());

		assertEquals("", builder.toString());
	}

	@Test
	void shouldAppendCircularReference() {
		StringBuilder builder = new StringBuilder();
		Throwable throwable = new Throwable();
		Set<Throwable> visited = new HashSet<>();
		visited.add(throwable);

		OneLineFormatter.appendThrowable(builder, throwable, "", visited);

		assertEquals(" [CIRCULAR REFERENCE: java.lang.Throwable]" + System.lineSeparator(), builder.toString());
	}

	@Test
	void shouldAppendThrowableWithMessage() {
		StringBuilder builder = new StringBuilder();
		Throwable throwable = new Throwable("Test message");

		OneLineFormatter.appendThrowable(builder, throwable, "", new HashSet<>());

		assertThat(builder.toString(), startsWith("java.lang.Throwable: Test message" + System.lineSeparator()));
	}

	@Test
	void shouldAppendThrowableWithoutMessage() {
		StringBuilder builder = new StringBuilder();
		Throwable throwable = new Throwable();

		OneLineFormatter.appendThrowable(builder, throwable, "", new HashSet<>());

		assertThat(builder.toString(), startsWith("java.lang.Throwable" + System.lineSeparator()));
	}

	@Test
	void shouldAppendNestedThrowable() {
		StringBuilder builder = new StringBuilder();
		Throwable cause = new Throwable("Cause message");
		Throwable throwable = new Throwable("Test message", cause);

		OneLineFormatter.appendThrowable(builder, throwable, "", new HashSet<>());

		String result = builder.toString();
		assertThat(result, startsWith("java.lang.Throwable: Test message" + System.lineSeparator()));
		assertThat(result, containsString("at "));
		assertThat(result, containsString("Caused by: java.lang.Throwable: Cause message" + System.lineSeparator()));
	}

	@Test
	void shouldAppendSuppressedThrowable() {
		StringBuilder builder = new StringBuilder();
		Throwable suppressed = new Throwable("Suppressed message");
		Throwable throwable = new Throwable("Test message");
		throwable.addSuppressed(suppressed);

		OneLineFormatter.appendThrowable(builder, throwable, "", new HashSet<>());

		String result = builder.toString();
		assertThat(result, startsWith("java.lang.Throwable: Test message" + System.lineSeparator()));
		assertThat(result, containsString("at "));
		assertThat(result, containsString("Suppressed: java.lang.Throwable: Suppressed message" + System.lineSeparator()));
	}
}
