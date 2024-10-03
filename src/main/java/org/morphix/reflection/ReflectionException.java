package org.morphix.reflection;

import java.io.Serial;
import java.util.function.Supplier;

/**
 * Runtime exception that is thrown when a reflection action cannot be done.
 *
 * @author Radu Sebastian LAZIN
 */
public class ReflectionException extends RuntimeException {

	/**
	 * Serial Version UID.
	 */
	@Serial
	private static final long serialVersionUID = -3257120274930060942L;

	public ReflectionException(final String message) {
		super(message);
	}

	public ReflectionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ReflectionException(final Throwable cause) {
		super(cause);
	}

	public static void wrapThrowing(final Runnable runnable, final String reflectionExceptionMessage) {
		try {
			runnable.run();
		} catch (Exception e) {
			throw new ReflectionException(reflectionExceptionMessage, e);
		}
	}

	public static <T> T wrapThrowing(final Supplier<T> supplier, final String reflectionExceptionMessage) {
		try {
			return supplier.get();
		} catch (Exception e) {
			throw new ReflectionException(reflectionExceptionMessage, e);
		}
	}

}
