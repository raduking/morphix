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
package org.morphix.lang.accumulator;

import java.io.Serial;

import org.morphix.lang.JavaObjects;

/**
 * Exception that indicates an error in the accumulator.
 *
 * @author Radu Sebastian LAZIN
 */
public class AccumulatorException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -1040952495776314643L;

	/**
	 * The accumulator that caused the exception.
	 */
	private final transient Accumulator<?> accumulator;

	/**
	 * Constructor.
	 *
	 * @param cause exception cause
	 * @param accumulator accumulator which caused the exception
	 */
	public AccumulatorException(final Throwable cause, final Accumulator<?> accumulator) {
		super(cause);
		this.accumulator = accumulator;
	}

	/**
	 * Constructor.
	 *
	 * @param message exception message
	 * @param cause exception cause
	 * @param accumulator accumulator which caused the exception
	 */
	public AccumulatorException(final String message, final Throwable cause, final Accumulator<?> accumulator) {
		super(message, cause);
		this.accumulator = accumulator;
	}

	/**
	 * Returns the accumulator that caused the exception.
	 *
	 * @param <T> accumulated element type
	 * @return the accumulator that caused the exception
	 */
	public <T> Accumulator<T> getAccumulator() {
		return JavaObjects.cast(accumulator);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(super.toString())
				.append(" ")
				.append("Accumulated exceptions: ")
				.append(accumulator.getInformationList());
		return result.toString();
	}
}
