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

/**
 * Minimal logging abstraction.
 * <p>
 * Implementations may delegate to JUL, SLF4J, Log4j, or any other logging framework.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface LoggerAdapter {

	/**
	 * A logger adapter that ignores all log messages.
	 */
	static LoggerAdapter EMPTY = (level, message, args) -> {
		// empty
	};

	/**
	 * Logs a message with the specified level, format, and arguments.
	 *
	 * @param level the log level
	 * @param message the log message format
	 * @param args the arguments to include in the log message
	 */
	void log(LoggingLevel level, String message, Object... args);

	/**
	 * Enumeration of log levels.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	enum LoggingLevel {

		/**
		 * Trace level, for very detailed logging.
		 */
		TRACE,

		/**
		 * Debug level, for debugging information.
		 */
		DEBUG,

		/**
		 * Info level, for informational messages.
		 */
		INFO,

		/**
		 * Warn level, for warning messages.
		 */
		WARN,

		/**
		 * Error level, for error messages.
		 */
		ERROR
	}

	/**
	 * Returns logger adapter that ignores all log messages.
	 *
	 * @return an empty logger adapter
	 */
	static LoggerAdapter none() {
		return EMPTY;
	}

	/**
	 * Logs a message at the {@link LoggingLevel#TRACE} level.
	 *
	 * @param message the log message format
	 * @param args the arguments to include in the log message
	 */
	default void trace(final String message, final Object... args) {
		log(LoggingLevel.TRACE, message, args);
	}

	/**
	 * Logs a message at the {@link LoggingLevel#DEBUG} level.
	 *
	 * @param message the log message format
	 * @param args the arguments to include in the log message
	 */
	default void debug(final String message, final Object... args) {
		log(LoggingLevel.DEBUG, message, args);
	}

	/**
	 * Logs a message at the {@link LoggingLevel#INFO} level.
	 *
	 * @param message the log message format
	 * @param args the arguments to include in the log message
	 */
	default void info(final String message, final Object... args) {
		log(LoggingLevel.INFO, message, args);
	}

	/**
	 * Logs a message at the {@link LoggingLevel#WARN} level.
	 *
	 * @param message the log message format
	 * @param args the arguments to include in the log message
	 */
	default void warn(final String message, final Object... args) {
		log(LoggingLevel.WARN, message, args);
	}

	/**
	 * Logs a message at the {@link LoggingLevel#ERROR} level.
	 *
	 * @param message the log message format
	 * @param args the arguments to include in the log message
	 */
	default void error(final String message, final Object... args) {
		log(LoggingLevel.ERROR, message, args);
	}
}
