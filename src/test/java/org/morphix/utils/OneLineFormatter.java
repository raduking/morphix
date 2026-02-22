package org.morphix.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.morphix.lang.Messages;

/**
 * Custom log formatter that formats log records in a single line with a specific format.
 * <p>
 * The format includes the timestamp, log level, logger name, and the message. The message supports SLF4J-style
 * placeholders (e.g., "User {} logged in") which are replaced with the actual parameters from the log record.
 *
 * @author Radu Sebastian LAZIN
 */
public class OneLineFormatter extends Formatter {

	/**
	 * Date pattern for the timestamp.
	 */
	private static final String DATE_PATTERN = "HH:mm:ss.SSS";

	/**
	 * The date format used to format the timestamp in log records.
	 */
	private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);

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

		return sb.toString();
	}
}
