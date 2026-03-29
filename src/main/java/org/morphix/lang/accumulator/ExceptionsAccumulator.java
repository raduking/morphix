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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.Unchecked;

/**
 * Extends the {@link Accumulator} class for exception accumulation.
 *
 * @author Radu Sebastian LAZIN
 */
public class ExceptionsAccumulator extends Accumulator<Exception> {

	/**
	 * Accumulator modes.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public enum Throw {

		/**
		 * Throws the last caught exception as is when {@link #rest()} is called.
		 */
		RAW,

		/**
		 * Throws the last caught exception wrapped in an {@link AccumulatorException} when {@link #rest()} is called.
		 */
		WRAPPED,

		/**
		 * Does not throw any exception when {@link #rest()} is called.
		 */
		NONE
	}

	/**
	 * Indicates how exceptions are handled when {@link #rest()} is called.
	 */
	private final Throw throwMode;

	/**
	 * Specifies the exception types accumulated. If this list is empty, all exceptions are accumulated; otherwise only the
	 * types present in this list are accumulated.
	 * <p>
	 * Linked list is used because:
	 * <ul>
	 * <li>it is more efficient in terms of memory consumption</li>
	 * <li>accessing the first and last has O(1) complexity</li>
	 * <li>no random access is needed</li>
	 * </ul>
	 */
	private final List<Class<? super Exception>> exceptionTypes = new LinkedList<>();

	/**
	 * Private constructor.
	 *
	 * @param throwMode throw mode for the accumulator when {@link #rest()} is called
	 * @param exceptionTypes exception types to accumulate
	 */
	private ExceptionsAccumulator(final Throw throwMode, final Set<Class<?>> exceptionTypes) {
		this.throwMode = null == throwMode ? Throw.RAW : throwMode;
		if (null != exceptionTypes) {
			for (Class<?> exceptionType : exceptionTypes) {
				this.exceptionTypes.add(JavaObjects.cast(exceptionType));
			}
		}
	}

	/**
	 * Private constructor with accumulated exception types. If no type is specified, then all exceptions are accumulated,
	 * otherwise only the types given are accumulated.
	 *
	 * @param exceptionTypes exception types to accumulate
	 */
	private ExceptionsAccumulator(final Set<Class<?>> exceptionTypes) {
		this(Throw.RAW, exceptionTypes);
	}

	/**
	 * Returns a new exceptions accumulator. If no exception type is specified, then all exceptions are accumulated,
	 * otherwise only the types given are accumulated.
	 *
	 * @param throwMode throw mode for the accumulator when {@link #rest()} is called
	 * @param exceptionTypes exception types to accumulate
	 * @return a new exceptions accumulator
	 */
	public static ExceptionsAccumulator of(final Throw throwMode, final Set<Class<?>> exceptionTypes) {
		return new ExceptionsAccumulator(throwMode, exceptionTypes);
	}

	/**
	 * Returns a new exceptions accumulator.
	 *
	 * @param throwMode throw mode for the accumulator when {@link #rest()} is called
	 * @return a new exceptions accumulator
	 */
	public static ExceptionsAccumulator of(final Throw throwMode) {
		return new ExceptionsAccumulator(throwMode, Collections.emptySet());
	}

	/**
	 * Returns a new exceptions accumulator with no exception wrapping, accumulation of all exceptions and automatic
	 * throwing of the last exception when {@link #rest()} method is called.
	 * <p>
	 * If no exception type is specified, then all exceptions are accumulated, otherwise only the types given are
	 * accumulated.
	 *
	 * @param exceptionTypes exceptions to accumulate
	 * @return a new exceptions accumulator
	 */
	public static ExceptionsAccumulator of(final Set<Class<?>> exceptionTypes) {
		return new ExceptionsAccumulator(exceptionTypes);
	}

	/**
	 * Returns a new exceptions accumulator with no exception wrapping, accumulation of all exceptions and automatic
	 * throwing of the last exception when {@link #rest()} method is called.
	 *
	 * @return a new exceptions accumulator
	 */
	public static ExceptionsAccumulator of() {
		return of(Collections.emptySet());
	}

	/**
	 * Alias for {@link #getInformationList()}
	 *
	 * @return the exception list
	 */
	public List<Exception> getExceptions() {
		return getInformationList();
	}

	/**
	 * Returns the last accumulated exception.
	 *
	 * @return the last accumulated exception
	 */
	public Exception lastException() {
		return lastInformation();
	}

	/**
	 * Returns true if the accumulator has at least one exception, false otherwise.
	 *
	 * @return true if the accumulator has at least one exception
	 */
	public boolean hasExceptions() {
		return hasInformation();
	}

	/**
	 * @see Accumulator#accumulate(Supplier, Supplier)
	 */
	@Override
	public <T> T accumulate(final Supplier<T> supplier, final Supplier<T> defaultReturnSupplier) {
		try {
			return supplier.get();
		} catch (Exception e) {
			addException(e);
			return defaultReturnSupplier.get();
		}
	}

	/**
	 * @see Accumulator#rest()
	 */
	@Override
	public void rest() {
		if (Throw.NONE == throwMode) {
			return;
		}
		Exception lastException = lastException();
		if (null == lastException) {
			return;
		}
		if (Throw.RAW == throwMode) {
			Unchecked.reThrow(lastException);
		}
		throw new AccumulatorException(lastException, this);
	}

	/**
	 * Returns the exception types.
	 *
	 * @return the exception types
	 */
	public List<Class<Exception>> getExceptionTypes() {
		return JavaObjects.cast(exceptionTypes);
	}

	/**
	 * Adds an exception. If the exception type is not in the list of accumulated exception types, it is re-thrown
	 * immediately.
	 *
	 * @param e exception to add
	 */
	private void addException(final Exception e) {
		if (!exceptionTypes.isEmpty() && !exceptionTypes.contains(e.getClass())) {
			Unchecked.reThrow(e);
		}
		getExceptions().add(e);
	}

	/**
	 * Returns the wrap exception flag.
	 *
	 * @return the wrap exception flag
	 */
	public boolean isWrapException() {
		return Throw.WRAPPED == throwMode;
	}

	/**
	 * Returns the throw exception flag.
	 *
	 * @return the throw exception flag
	 */
	public boolean isThrowException() {
		return Throw.NONE != throwMode;
	}
}
