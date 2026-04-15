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
package org.morphix.lang.function;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.morphix.lang.function.LoggerAdapter.LoggingLevel;
import org.morphix.lang.thread.Threads;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;
import org.morphix.utils.logging.TestLoggerAdapter;

/**
 * Test class for {@link ExecutionWrappers}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExecutionWrappersTest {

	private static final String TEST_TASK = "TestTask";

	@Test
	void shouldThrowExceptionWhenTryingToInstantiate() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(ExecutionWrappers.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldLogStartAndFinishWhenUsingLogWrapperWithDefaultLevel() {
		TestLoggerAdapter logger = new TestLoggerAdapter();
		ExecutionWrapper<String> wrapper = ExecutionWrappers.log(logger, TEST_TASK);

		String result = wrapper.execute(() -> "done");

		assertThat(result, is("done"));
		assertThat(logger.getMessages(LoggingLevel.DEBUG), hasSize(2));
		assertThat(logger.getMessages(LoggingLevel.DEBUG).get(0), is("[" + TEST_TASK + "] Starting execution."));
		assertThat(logger.getMessages(LoggingLevel.DEBUG).get(1), is("[" + TEST_TASK + "] Finished execution."));
	}

	@ParameterizedTest
	@EnumSource(LoggingLevel.class)
	void shouldLogStartAndFinishWhenUsingLogWrapper(final LoggingLevel level) {
		TestLoggerAdapter logger = new TestLoggerAdapter();
		ExecutionWrapper<String> wrapper = ExecutionWrappers.log(logger, level, TEST_TASK);

		String result = wrapper.execute(() -> "done");

		assertThat(result, is("done"));
		assertThat(logger.getMessages(level), hasSize(2));
		assertThat(logger.getMessages(level).get(0), is("[" + TEST_TASK + "] Starting execution."));
		assertThat(logger.getMessages(level).get(1), is("[" + TEST_TASK + "] Finished execution."));
	}

	@Test
	void shouldLogExecutionTimeWhenUsingTimeWrapper() {
		TestLoggerAdapter logger = new TestLoggerAdapter();
		ExecutionWrapper<String> wrapper = ExecutionWrappers.time(logger, TEST_TASK);

		String result = wrapper.execute(() -> {
			Threads.safeSleep(Duration.ofMillis(10));
			return "done";
		});

		assertThat(result, is("done"));
		assertThat(logger.getMessages(LoggingLevel.DEBUG), hasSize(1));
		assertThat(logger.getMessages(LoggingLevel.DEBUG).get(0), containsString("[" + TEST_TASK + "] Execution took"));

		// ensure a non-negative duration is logged
		String message = logger.getMessages(LoggingLevel.DEBUG).get(0);
		long duration = extractDurationMillis(message);

		assertThat(duration, greaterThanOrEqualTo(0L));
	}

	/**
	 * Extracts the duration in milliseconds from the log message.
	 */
	private static long extractDurationMillis(final String message) {
		// Expected format: "[name] Execution took Xms."
		int start = message.lastIndexOf("took ") + 5;
		int end = message.lastIndexOf("ms");
		return Long.parseLong(message.substring(start, end).trim());
	}
}
