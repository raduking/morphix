package org.morphix.lang.thread;

import java.io.Serial;

/**
 * Exception thrown when a thread context error occurs.
 *
 * @author Radu Sebastian LAZIN
 */
public class ThreadContextException extends RuntimeException {

	/**
	 * Serial version UID for serialization.
	 */
	@Serial
	private static final long serialVersionUID = -4521059325987804768L;

	/**
	 * Constructs a new ThreadContextException with the specified detail message.
	 *
	 * @param message the detail message
	 */
	public ThreadContextException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new ThreadContextException with the specified detail message and cause.
	 *
	 * @param message the detail message
	 * @param cause the cause of the exception
	 */
	public ThreadContextException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
