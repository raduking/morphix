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

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.morphix.lang.JavaArrays;
import org.morphix.lang.Messages;
import org.morphix.lang.function.LoggerAdapter;

/**
 * JUL-based implementation of {@link LoggerAdapter}. This adapter allows using the Java Util Logging (JUL) framework
 * for logging purposes. It maps the logging levels defined in {@link LoggerAdapter.LoggingLevel} to the corresponding
 * levels in JUL and formats log messages accordingly.
 * <p>
 * The adapter also handles cases where the last argument is a Throwable, allowing for proper logging of exceptions with
 * stack traces.
 *
 * @author Radu Sebastian LAZIN
 */
public final class JulLoggerAdapter implements LoggerAdapter {

	/**
	 * The underlying JUL logger instance that this adapter delegates to for logging messages.
	 */
	private final Logger logger;

	/**
	 * Private constructor to create a new instance of {@link JulLoggerAdapter} with the specified JUL logger. This
	 * constructor is private to enforce the use of factory methods for creating instances of this adapter.
	 *
	 * @param logger the JUL logger instance to delegate logging to, must not be null
	 * @throws NullPointerException if the provided logger is null
	 */
	private JulLoggerAdapter(final Logger logger) {
		this.logger = Objects.requireNonNull(logger, "logger must not be null");
	}

	/**
	 * Factory method to create a new instance of {@link JulLoggerAdapter} for the specified class. This method retrieves a
	 * JUL logger using the class name as the logger name.
	 *
	 * @param type the class for which to create the logger adapter, must not be null
	 * @return a new instance of {@link JulLoggerAdapter} associated with the specified class
	 * @throws NullPointerException if the provided class is null
	 */
	public static JulLoggerAdapter of(final Class<?> type) {
		return of(type.getName());
	}

	/**
	 * Factory method to create a new instance of {@link JulLoggerAdapter} for the specified logger name. This method
	 * retrieves a JUL logger using the provided name.
	 *
	 * @param name the name of the logger to create, must not be null
	 * @return a new instance of {@link JulLoggerAdapter} associated with the specified logger name
	 * @throws NullPointerException if the provided logger name is null
	 */
	public static JulLoggerAdapter of(final String name) {
		return of(Logger.getLogger(name));
	}

	/**
	 * Factory method to create a new instance of {@link JulLoggerAdapter} for the specified JUL logger. This method
	 * directly uses the provided JUL logger for logging.
	 *
	 * @param logger the JUL logger instance to use, must not be null
	 * @return a new instance of {@link JulLoggerAdapter} that delegates to the specified JUL logger
	 * @throws NullPointerException if the provided logger is null
	 */
	public static JulLoggerAdapter of(final Logger logger) {
		return new JulLoggerAdapter(logger);
	}

	/**
	 * @see LoggerAdapter#log(LoggingLevel, String, Object...)
	 */
	@Override
	public void log(final LoggerAdapter.LoggingLevel level, final String message, final Object... args) {
		Level julLevel = level.toJulLevel();
		if (!logger.isLoggable(julLevel)) {
			return;
		}
		if (JavaArrays.isEmpty(args)) {
			logger.log(julLevel, message);
			return;
		}
		final Object lastArg = args[args.length - 1];
		if (lastArg instanceof Throwable throwable) {
			String formattedMessage;
			if (args.length == 1) {
				// only exception was passed as argument, use the message as is
				formattedMessage = message;
			} else {
				Object[] messageArgs = new Object[args.length - 1];
				System.arraycopy(args, 0, messageArgs, 0, args.length - 1);
				formattedMessage = Messages.message(message, messageArgs);
			}
			logger.log(julLevel, formattedMessage, throwable);
		} else {
			String formattedMessage = Messages.message(message, args);
			logger.log(julLevel, formattedMessage);
		}
	}

	/**
	 * @see LoggerAdapter#isEnabled(LoggingLevel)
	 */
	@Override
	public boolean isEnabled(final LoggingLevel level) {
		return logger.isLoggable(level.toJulLevel());
	}

	/**
	 * Returns the underlying JUL logger instance that this adapter delegates to for logging messages. This method is
	 * protected to allow subclasses to access the logger if needed, while still encapsulating the logger within the
	 * adapter.
	 *
	 * @return the underlying JUL logger instance used by this adapter
	 */
	protected Logger getLogger() {
		return logger;
	}
}
