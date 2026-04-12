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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link LoggerAdapter}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class LoggerAdapterTest {

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

			verify(mockAdapter).log(LoggerAdapter.Level.TRACE, message, args);
		}

		@Test
		void shouldCallLogWithDebugLevel() {
			String message = "debug message";

			mockAdapter.debug(message);

			verify(mockAdapter).log(LoggerAdapter.Level.DEBUG, message);
		}

		@Test
		void shouldCallLogWithInfoLevel() {
			String message = "info message: {} {}";
			Object[] args = new Object[] { "arg1", "arg2" };

			mockAdapter.info(message, args);

			verify(mockAdapter).log(LoggerAdapter.Level.INFO, message, args);
		}

		@Test
		void shouldCallLogWithWarnLevel() {
			String message = "warn message";

			mockAdapter.warn(message);

			verify(mockAdapter).log(LoggerAdapter.Level.WARN, message);
		}

		@Test
		void shouldCallLogWithErrorLevel() {
			String message = "error message: {}";
			Object[] args = new Object[] { "error detail" };

			mockAdapter.error(message, args);

			verify(mockAdapter).log(LoggerAdapter.Level.ERROR, message, args);
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

			verify(mockAdapter).log(LoggerAdapter.Level.TRACE, traceMsg);
			verify(mockAdapter).log(LoggerAdapter.Level.DEBUG, debugMsg);
			verify(mockAdapter).log(LoggerAdapter.Level.INFO, infoMsg);
			verify(mockAdapter).log(LoggerAdapter.Level.WARN, warnMsg);
			verify(mockAdapter).log(LoggerAdapter.Level.ERROR, errorMsg);
		}
	}

	@Nested
	class LevelEnumTests {

		@Test
		void shouldHaveFiveLevels() {
			int levelCount = LoggerAdapter.Level.values().length;

			assertThat(levelCount, is(5));
		}

		@Test
		void shouldHaveAllExpectedLevels() {
			LoggerAdapter.Level[] levels = LoggerAdapter.Level.values();

			assertThat(levels, arrayContainingInAnyOrder(
					LoggerAdapter.Level.TRACE,
					LoggerAdapter.Level.DEBUG,
					LoggerAdapter.Level.INFO,
					LoggerAdapter.Level.WARN,
					LoggerAdapter.Level.ERROR));
		}

		@Test
		void shouldHaveCorrectOrdinalOrder() {
			assertThat(LoggerAdapter.Level.TRACE.ordinal(), is(0));
			assertThat(LoggerAdapter.Level.DEBUG.ordinal(), is(1));
			assertThat(LoggerAdapter.Level.INFO.ordinal(), is(2));
			assertThat(LoggerAdapter.Level.WARN.ordinal(), is(3));
			assertThat(LoggerAdapter.Level.ERROR.ordinal(), is(4));
		}

		@Test
		void shouldHaveCorrectLevelNames() {
			assertThat(LoggerAdapter.Level.TRACE.name(), is("TRACE"));
			assertThat(LoggerAdapter.Level.DEBUG.name(), is("DEBUG"));
			assertThat(LoggerAdapter.Level.INFO.name(), is("INFO"));
			assertThat(LoggerAdapter.Level.WARN.name(), is("WARN"));
			assertThat(LoggerAdapter.Level.ERROR.name(), is("ERROR"));
		}
	}
}
