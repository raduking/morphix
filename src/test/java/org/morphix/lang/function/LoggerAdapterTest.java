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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.utils.Tests;

/**
 * Test class for {@link LoggerAdapter}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class LoggerAdapterTest {

	@BeforeAll
	static void setup() throws Exception {
		Tests.configureLogging("src/test/resources/test-logging.properties");
	}

	@Nested
	class NoneLoggerAdapterTests {

		@Test
		void shouldIgnoreAllLogMessages() {
			LoggerAdapter noneAdapter = LoggerAdapter.none();

			assertDoesNotThrow(() -> noneAdapter.trace("trace message", "arg1"));
			assertDoesNotThrow(() -> noneAdapter.debug("debug message", "arg2"));
			assertDoesNotThrow(() -> noneAdapter.info("info message"));
			assertDoesNotThrow(() -> noneAdapter.warn("warn message", "arg1", "arg2"));
			assertDoesNotThrow(() -> noneAdapter.error("error message", new RuntimeException("error")));
		}
	}

	@Nested
	class DefaultMethodsTests {

		@Spy
		private LoggerAdapter mockAdapter;

		@Test
		void shouldCallLogWithTraceLevel() {
			String message = "trace message: {}";
			Object[] args = new Object[] { "arg1" };

			mockAdapter.trace(message, args);

			verify(mockAdapter).log(LoggerAdapter.LoggingLevel.TRACE, message, args);
		}

		@Test
		void shouldCallLogWithDebugLevel() {
			String message = "debug message";

			mockAdapter.debug(message);

			verify(mockAdapter).log(LoggerAdapter.LoggingLevel.DEBUG, message);
		}

		@Test
		void shouldCallLogWithInfoLevel() {
			String message = "info message: {} {}";
			Object[] args = new Object[] { "arg1", "arg2" };

			mockAdapter.info(message, args);

			verify(mockAdapter).log(LoggerAdapter.LoggingLevel.INFO, message, args);
		}

		@Test
		void shouldCallLogWithWarnLevel() {
			String message = "warn message";

			mockAdapter.warn(message);

			verify(mockAdapter).log(LoggerAdapter.LoggingLevel.WARN, message);
		}

		@Test
		void shouldCallLogWithErrorLevel() {
			String message = "error message: {}";
			Object[] args = new Object[] { "error detail" };

			mockAdapter.error(message, args);

			verify(mockAdapter).log(LoggerAdapter.LoggingLevel.ERROR, message, args);
		}

		@Test
		void shouldPassCorrectArgumentsToLog() {
			String traceMsg = "trace";
			String debugMsg = "debug";
			String infoMsg = "info";
			String warnMsg = "warn";
			String errorMsg = "error";

			mockAdapter.trace(traceMsg);
			mockAdapter.debug(debugMsg);
			mockAdapter.info(infoMsg);
			mockAdapter.warn(warnMsg);
			mockAdapter.error(errorMsg);

			verify(mockAdapter).log(LoggerAdapter.LoggingLevel.TRACE, traceMsg);
			verify(mockAdapter).log(LoggerAdapter.LoggingLevel.DEBUG, debugMsg);
			verify(mockAdapter).log(LoggerAdapter.LoggingLevel.INFO, infoMsg);
			verify(mockAdapter).log(LoggerAdapter.LoggingLevel.WARN, warnMsg);
			verify(mockAdapter).log(LoggerAdapter.LoggingLevel.ERROR, errorMsg);
		}
	}

	@Nested
	class LevelEnumTests {

		@Test
		void shouldHaveFiveLevels() {
			int levelCount = LoggerAdapter.LoggingLevel.values().length;

			assertThat(levelCount, is(5));
		}

		@Test
		void shouldHaveAllExpectedLevels() {
			LoggerAdapter.LoggingLevel[] levels = LoggerAdapter.LoggingLevel.values();

			assertThat(levels, arrayContainingInAnyOrder(
					LoggerAdapter.LoggingLevel.TRACE,
					LoggerAdapter.LoggingLevel.DEBUG,
					LoggerAdapter.LoggingLevel.INFO,
					LoggerAdapter.LoggingLevel.WARN,
					LoggerAdapter.LoggingLevel.ERROR));
		}

		@Test
		void shouldHaveCorrectOrdinalOrder() {
			assertThat(LoggerAdapter.LoggingLevel.TRACE.ordinal(), is(0));
			assertThat(LoggerAdapter.LoggingLevel.DEBUG.ordinal(), is(1));
			assertThat(LoggerAdapter.LoggingLevel.INFO.ordinal(), is(2));
			assertThat(LoggerAdapter.LoggingLevel.WARN.ordinal(), is(3));
			assertThat(LoggerAdapter.LoggingLevel.ERROR.ordinal(), is(4));
		}

		@Test
		void shouldHaveCorrectLevelNames() {
			assertThat(LoggerAdapter.LoggingLevel.TRACE.name(), is("TRACE"));
			assertThat(LoggerAdapter.LoggingLevel.DEBUG.name(), is("DEBUG"));
			assertThat(LoggerAdapter.LoggingLevel.INFO.name(), is("INFO"));
			assertThat(LoggerAdapter.LoggingLevel.WARN.name(), is("WARN"));
			assertThat(LoggerAdapter.LoggingLevel.ERROR.name(), is("ERROR"));
		}
	}
}
