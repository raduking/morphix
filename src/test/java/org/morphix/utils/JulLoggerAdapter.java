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
		if (args == null || args.length == 0) {
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

	private static Level toJulLevel(final LoggerAdapter.LoggingLevel level) {
		return switch (level) {
			case TRACE -> Level.FINER;
			case DEBUG -> Level.FINE;
			case INFO -> Level.INFO;
			case WARN -> Level.WARNING;
			case ERROR -> Level.SEVERE;
		};
	}
}
