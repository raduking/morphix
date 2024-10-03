package org.morphix;

import java.io.Serial;

/**
 * Converter exceptions.
 *
 * @author Radu Sebastian LAZIN
 */
public class ConverterException extends RuntimeException {

	/**
	 * Serial Version UID.
	 */
	@Serial
	private static final long serialVersionUID = -332076440357406861L;

	public ConverterException(final String message) {
		super(message);
	}

	public ConverterException(final String message, final Throwable cause) {
		super(message, cause);
	}
}