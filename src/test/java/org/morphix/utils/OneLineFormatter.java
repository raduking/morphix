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
package org.morphix.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.morphix.lang.Messages;

/**
 * Custom log formatter that formats log records in a single line with a specific format.
 * <p>
 * The format includes the timestamp, log level, logger name, and the message. The message supports SLF4J-style
 * placeholders (e.g., "User {} logged in") which are replaced with the actual parameters from the log record.
 * <p>
 * This can be configured in a logging properties file (e.g., logging.properties) by setting the formatter for a handler
 * to this class:
 *
 * <pre>
 * java.util.logging.ConsoleHandler.formatter = org.morphix.utils.OneLineFormatter
 * </pre>
 *
 * The file can be configured to be used by the JVM with the following system property:
 *
 * <pre>
 * -Djava.util.logging.config.file=path/to/logging.properties
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
public class OneLineFormatter extends Formatter {

	private static final String INDENT = "  ";

	/**
	 * Date pattern for the timestamp.
	 */
	private static final String DATE_PATTERN = "HH:mm:ss.SSS";

	/**
	 * The date format used to format the timestamp in log records.
	 */
	private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);

	/**
	 * @see Formatter#format(LogRecord)
	 */
	@Override
	public String format(final LogRecord logRecord) {
		StringBuilder sb = new StringBuilder();

		// timestamp
		sb.append(dateFormat.format(new Date(logRecord.getMillis())));
		sb.append(" ");

		// thread name
		String threadName = Thread.currentThread().getName();
		sb.append(String.format("[%s]", threadName));
		sb.append(" ");

		// level (padded to 7 characters for alignment)
		sb.append(String.format("%-7s", logRecord.getLevel()));
		sb.append(" ");

		// logger name, only the class name (strip the package name)
		String loggerName = logRecord.getLoggerName();
		if (loggerName != null) {
			int lastDot = loggerName.lastIndexOf('.');
			if (lastDot > 0) {
				loggerName = loggerName.substring(lastDot + 1);
			}
			sb.append(loggerName);
		}
		sb.append(" - ");

		// message (replace SLF4J style {} with actual values)
		String message = logRecord.getMessage();
		Object[] params = logRecord.getParameters();
		if (params != null && params.length > 0) {
			message = Messages.message(message, params);
		}
		sb.append(message);

		// important! newline at the end of the log record
		sb.append("\n");

		// exception handling
		Throwable thrown = logRecord.getThrown();
		if (thrown != null) {
			appendException(sb, thrown, INDENT);
		}

		return sb.toString();
	}

	/**
	 * Appends exception in classic SLF4J / Logback style.
	 *
	 * @param sb the StringBuilder to append to
	 * @param throwable the exception to append
	 * @param indent the current indentation level (used for nested exceptions)
	 */
	private static void appendException(final StringBuilder sb, final Throwable throwable, final String indent) {
		if (null == throwable) {
			return;
		}
		appendException(sb, throwable, indent, new HashSet<>());
	}

	/**
	 * Appends exception in classic SLF4J / Logback style, with circular reference detection.
	 *
	 * @param sb the StringBuilder to append to
	 * @param throwable the exception to append
	 * @param indent the current indentation level (used for nested exceptions)
	 * @param visited the set of already visited exceptions to detect circular references
	 */
	private static void appendException(final StringBuilder sb, final Throwable throwable, final String indent, final Set<Throwable> visited) {
		if (null == throwable) {
			return;
		}
		if (!visited.add(throwable)) {
			sb.append("[CIRCULAR REFERENCE]\n");
			return;
		}
		sb.append(throwable.getClass().getName());

		String msg = throwable.getMessage();
		if (msg != null) {
			sb.append(": ").append(msg);
		}
		sb.append("\n");

		for (StackTraceElement element : throwable.getStackTrace()) {
			sb.append(indent)
					.append("at ")
					.append(element)
					.append("\n");
		}
		for (Throwable suppressed : throwable.getSuppressed()) {
			sb.append(indent).append("Suppressed: ");
			appendException(sb, suppressed, indent, visited);
		}
		Throwable cause = throwable.getCause();
		if (cause != null) {
			sb.append("Caused by: ");
			appendException(sb, cause, indent, visited);
		}
	}
}
