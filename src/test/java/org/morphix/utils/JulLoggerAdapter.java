package org.morphix.utils;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.morphix.lang.Messages;
import org.morphix.lang.function.LoggerAdapter;

/**
 * JUL-based implementation of {@link LoggerAdapter}. <
 *
 * @author Radu Sebastian LAZIN
 */
public final class JulLoggerAdapter implements LoggerAdapter {

	private final Logger logger;

	private JulLoggerAdapter(final Logger logger) {
		this.logger = Objects.requireNonNull(logger, "logger must not be null");
	}

	public static JulLoggerAdapter of(final Class<?> type) {
		return new JulLoggerAdapter(Logger.getLogger(type.getName()));
	}

	public static JulLoggerAdapter of(final String name) {
		return new JulLoggerAdapter(Logger.getLogger(name));
	}

	@Override
	public void log(final LoggerAdapter.LoggingLevel level, final String message, final Object... args) {
		Level julLevel = toJulLevel(level);
		if (!logger.isLoggable(julLevel)) {
			return;
		}
		// TODO: log exceptions (if the last argument is a Throwable, log it as an exception)
		logger.log(julLevel, format(message, args));
	}

	private static Level toJulLevel(final LoggerAdapter.LoggingLevel level) {
		return switch (level) {
			case TRACE -> Level.FINER;
			case DEBUG -> Level.FINE;
			case INFO -> Level.INFO;
			case WARN -> Level.WARNING;
			case ERROR -> Level.SEVERE;
		};
	}

	private static String format(final String message, final Object... args) {
		if (args == null || args.length == 0) {
			return message;
		}
		return Messages.message(message, args);
	}
}
