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
package org.morphix.reflection;

import java.io.Serial;
import java.util.function.Supplier;

import org.morphix.lang.Messages;

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
	private static final long serialVersionUID = -4208205939967495183L;

	/**
	 * Constructor with the message.
	 *
	 * @param message exception message
	 */
	public ReflectionException(final String message) {
		super(message);
	}

	/**
	 * Constructor with message and cause.
	 *
	 * @param message exception message
	 * @param cause cause of this exception
	 */
	public ReflectionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with cause.
	 *
	 * @param cause cause of this exception
	 */
	public ReflectionException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with formatted message.
	 *
	 * @param template message template
	 * @param args message arguments
	 */
	public ReflectionException(final String template, final Object... args) {
		super(Messages.message(template, args));
	}

	/**
	 * Constructor with cause and formatted message.
	 *
	 * @param cause cause of this exception
	 * @param template message template
	 * @param args message arguments
	 */
	public ReflectionException(final Throwable cause, final String template, final Object... args) {
		super(Messages.message(template, args), cause);
	}

	/**
	 * Wraps the runnable with a try/catch and the resulting exception (if any) is wrapped in a {@link ReflectionException}.
	 *
	 * @param runnable code to wrap
	 * @param reflectionExceptionMessage the resulting exception message
	 */
	public static void wrapThrowing(final Runnable runnable, final String reflectionExceptionMessage) {
		try {
			runnable.run();
		} catch (Exception e) {
			throw new ReflectionException(reflectionExceptionMessage, e);
		}
	}

	/**
	 * Wraps the supplier with a try/catch and the resulting exception (if any) is wrapped in a {@link ReflectionException}.
	 *
	 * @param <T> supplier value type
	 *
	 * @param supplier code to wrap
	 * @param reflectionExceptionMessage the resulting exception message
	 * @return the supplier value
	 */
	public static <T> T wrapThrowing(final Supplier<T> supplier, final String reflectionExceptionMessage) {
		try {
			return supplier.get();
		} catch (Exception e) {
			throw new ReflectionException(reflectionExceptionMessage, e);
		}
	}

}
