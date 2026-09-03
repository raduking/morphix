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
package org.morphix.async.accumulator;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import org.morphix.async.CompletableFutures;
import org.morphix.lang.Unchecked;
import org.morphix.lang.Throwables;
import org.morphix.lang.accumulator.Accumulator;
import org.morphix.lang.function.Runnables;
import org.morphix.lang.function.Suppliers;

/**
 * An async-aware wrapper around an {@link Accumulator} which invokes an asynchronous supplier and absorbs the
 * result/exception plumbing of a single attempt: a synchronous {@link Exception} thrown by the supplier is converted
 * into a failed future, a {@link CompletionException} is unwrapped, and any resulting error is accumulated in the
 * wrapped accumulator.
 *
 * @param <U> accumulated information type
 *
 * @author Radu Sebastian LAZIN
 */
public class AsyncAccumulator<U> {

	/**
	 * The wrapped information accumulator.
	 */
	private final Accumulator<U> accumulator;

	/**
	 * The error of the last failed attempt, null if the last attempt did not fail. It allows the exhausted state to surface
	 * the error even when the accumulator does not throw on {@link #rest()}.
	 */
	private Throwable lastError;

	/**
	 * Constructor.
	 *
	 * @param accumulator information accumulator to wrap
	 */
	public AsyncAccumulator(final Accumulator<U> accumulator) {
		this.accumulator = Objects.requireNonNull(accumulator, "accumulator cannot be null");
	}

	/**
	 * Asynchronous information accumulator method. The second supplier is used to provide a default return value in case
	 * the attempt failed. It is a supplier to avoid unnecessary computation of the default return value.
	 *
	 * @param <T> result type
	 *
	 * @param resultSupplier asynchronous result supplier
	 * @param defaultReturnSupplier default return supplier
	 * @return a {@link CompletableFuture} with the supplier result or a default return
	 */
	public <T> CompletableFuture<T> accumulate(final Supplier<CompletableFuture<T>> resultSupplier,
			final Supplier<T> defaultReturnSupplier) {
		return CompletableFutures.invoke(resultSupplier).handle(AttemptOutcome::new).thenCompose(outcome -> {
			Throwable error = Throwables.unwrap(outcome.error, CompletionException.class);
			Supplier<T> valueSupplier = () -> {
				if (null != error) {
					return Unchecked.Undeclared.reThrow(error);
				}
				return outcome.result;
			};
			try {
				T value = accumulator.accumulate(valueSupplier, defaultReturnSupplier);
				lastError = error;
				return CompletableFuture.completedFuture(value);
			} catch (Throwable t) { // NOSONAR the accumulator may rethrow errors that it does not accumulate, which must become a failed future
				lastError = t;
				return CompletableFuture.failedFuture(t);
			}
		});
	}

	/**
	 * Asynchronous information accumulator method.
	 *
	 * @param <T> result type
	 *
	 * @param resultSupplier asynchronous result supplier
	 * @return a {@link CompletableFuture} with the supplier result
	 */
	public <T> CompletableFuture<T> accumulate(final Supplier<CompletableFuture<T>> resultSupplier) {
		return accumulate(resultSupplier, Suppliers.supplyNull());
	}

	/**
	 * Asynchronous information accumulator method.
	 *
	 * @param runnable asynchronous action
	 * @return a {@link CompletableFuture} with the result
	 */
	public CompletableFuture<Void> accumulate(final Runnable runnable) {
		return accumulate(Runnables.toSupplier(runnable), Suppliers.supplyNull());
	}

	/**
	 * Returns the last accumulated information.
	 *
	 * @return the last accumulated information
	 */
	public U lastInformation() {
		return accumulator.lastInformation();
	}

	/**
	 * Signals that the accumulator finished accumulating and resets it.
	 */
	public void rest() {
		accumulator.rest();
	}

	/**
	 * Finalizes the exhausted state. Resets the accumulator and, if the last attempt failed, surfaces the error as a failed
	 * future.
	 *
	 * @param <T> result type
	 *
	 * @param result the last result
	 * @return a {@link CompletableFuture} with the result or the last error
	 */
	public <T> CompletableFuture<T> exhaust(final T result) {
		Throwable error = lastError;
		lastError = null;
		accumulator.rest();
		if (null != error) {
			return CompletableFuture.failedFuture(error);
		}
		return CompletableFuture.completedFuture(result);
	}

	/**
	 * Outcome of a single attempt, carrying either a result or an error.
	 *
	 * @param <T> result type
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static final class AttemptOutcome<T> {

		/**
		 * The result, null if the attempt failed.
		 */
		private final T result;

		/**
		 * The error, null if the attempt succeeded.
		 */
		private final Throwable error;

		/**
		 * Constructor.
		 *
		 * @param result result
		 * @param error error
		 */
		private AttemptOutcome(final T result, final Throwable error) {
			this.result = result;
			this.error = error;
		}
	}
}
