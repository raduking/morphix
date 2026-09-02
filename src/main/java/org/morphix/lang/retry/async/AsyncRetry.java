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
package org.morphix.lang.retry.async;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.morphix.lang.accumulator.Accumulator;
import org.morphix.lang.function.Consumers;
import org.morphix.lang.function.Runnables;

/**
 * A basic configurable async retry implementation. Retries an async supplier until a condition is met or the wait is
 * exhausted, without blocking the calling thread.
 *
 * @author Radu Sebastian LAZIN
 */
public class AsyncRetry {

	/**
	 * Default retry object.
	 */
	public static final AsyncRetry DEFAULT = AsyncRetry.of(AsyncWaitTimeout.DEFAULT);

	/**
	 * No wait object.
	 */
	public static final AsyncWait NO_WAIT = () -> false;

	/**
	 * No retry object.
	 */
	public static final AsyncRetry NO_RETRY = AsyncRetry.of(NO_WAIT);

	/**
	 * Wait prototype object which will serve as the source for copies.
	 */
	private final AsyncWait waitPrototype;

	/**
	 * Private constructor.
	 *
	 * @param wait the wait object
	 */
	private AsyncRetry(final AsyncWait wait) {
		this.waitPrototype = Objects.requireNonNull(wait, "wait cannot be null");
	}

	/**
	 * Builds a retry object.
	 *
	 * @param wait the wait object that configures wait intervals
	 * @return a retry object
	 */
	public static AsyncRetry of(final AsyncWait wait) {
		return new AsyncRetry(wait);
	}

	/**
	 * Returns the default retry object.
	 *
	 * @return the default retry object
	 */
	public static AsyncRetry defaultRetry() {
		return DEFAULT;
	}

	/**
	 * Returns a retry that does not retry.
	 *
	 * @return no retry
	 */
	public static AsyncRetry noRetry() {
		return NO_RETRY;
	}

	/**
	 * Returns a wait that does not wait.
	 *
	 * @return no wait
	 */
	public static AsyncWait noWait() {
		return NO_WAIT;
	}

	/**
	 * Returns a non-null object.
	 * <p>
	 * This method should be used when a retry is needed on a {@link Runnable} and since most retries check for a non
	 * <code>null</code> result this is a handy way to transform the runnable into an async supplier that returns non
	 * {@code null} since the runnable doesn't have a return value.
	 * <p>
	 * The method effectively returns an empty {@link Optional}.
	 *
	 * @return a non null object
	 */
	public static Object nonNull() {
		return Optional.empty();
	}

	/**
	 * Retries the async supplier until the predicate is satisfied or the wait is exhausted.
	 *
	 * @param <T> result type
	 *
	 * @param resultSupplier async result supplier
	 * @param exitCondition end predicate
	 * @return a {@link CompletableFuture} with the result
	 */
	public <T> CompletableFuture<T> deferUntil(final Supplier<CompletableFuture<T>> resultSupplier,
			final Predicate<T> exitCondition) {
		return deferUntil(resultSupplier, exitCondition, Runnables.doNothing());
	}

	/**
	 * Retries the async supplier until the predicate is satisfied or the wait is exhausted.
	 *
	 * @param <T> result type
	 *
	 * @param resultSupplier async result supplier
	 * @param exitCondition end predicate
	 * @param beforeWait code to run before wait
	 * @return a {@link CompletableFuture} with the result
	 */
	public <T> CompletableFuture<T> deferUntil(final Supplier<CompletableFuture<T>> resultSupplier,
			final Predicate<T> exitCondition, final Runnable beforeWait) {
		return deferUntil(resultSupplier, exitCondition, e -> beforeWait.run());
	}

	/**
	 * Retries the async supplier until the predicate is satisfied or the wait is exhausted.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier async result supplier
	 * @param exitCondition end predicate
	 * @param accumulator information accumulator
	 * @return a {@link CompletableFuture} with the result
	 */
	public <T, U> CompletableFuture<T> deferUntil(final Supplier<CompletableFuture<T>> resultSupplier,
			final Predicate<T> exitCondition, final Accumulator<U> accumulator) {
		return deferUntil(resultSupplier, exitCondition, Consumers.consumeNothing(), accumulator);
	}

	/**
	 * Retries the async supplier until the predicate is satisfied or the wait is exhausted.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier async result supplier
	 * @param exitCondition end predicate
	 * @param beforeWait code to run before wait
	 * @param accumulator information accumulator
	 * @return a {@link CompletableFuture} with the result
	 */
	public <T, U> CompletableFuture<T> deferUntil(final Supplier<CompletableFuture<T>> resultSupplier,
			final Predicate<T> exitCondition, final Consumer<U> beforeWait, final Accumulator<U> accumulator) {
		return deferUntil(resultSupplier, exitCondition, Consumers.noBiConsumer(), beforeWait, accumulator);
	}

	/**
	 * Retries the async supplier until the predicate is satisfied or the wait is exhausted.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier async result supplier
	 * @param exitCondition end predicate
	 * @param beforeWait code to run before wait
	 * @return a {@link CompletableFuture} with the result
	 */
	public <T, U> CompletableFuture<T> deferUntil(final Supplier<CompletableFuture<T>> resultSupplier,
			final Predicate<T> exitCondition, final Consumer<U> beforeWait) {
		return deferUntil(resultSupplier, exitCondition, beforeWait, Accumulator.noAccumulator());
	}

	/**
	 * Retries the async supplier until the predicate is satisfied or the wait is exhausted.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier async result supplier
	 * @param exitCondition end predicate
	 * @param afterResult code to run after the result supplier produced a value
	 * @param beforeWait code to run before wait
	 * @param accumulator information accumulator
	 * @return a {@link CompletableFuture} with the result
	 */
	public <T, U> CompletableFuture<T> deferUntil(final Supplier<CompletableFuture<T>> resultSupplier,
			final Predicate<T> exitCondition, final BiConsumer<T, U> afterResult, final Consumer<U> beforeWait,
			final Accumulator<U> accumulator) {
		AsyncWait wait = waitPrototype.copy();
		wait.start();
		return attempt(resultSupplier, exitCondition, afterResult, beforeWait, accumulator, wait);
	}

	/**
	 * Retries a sync supplier, wrapping it on the configured executor, until the predicate is satisfied or the wait is
	 * exhausted.
	 *
	 * @param <T> result type
	 *
	 * @param resultSupplier sync result supplier
	 * @param exitCondition end predicate
	 * @return a {@link CompletableFuture} with the result
	 */
	public <T> CompletableFuture<T> until(final Supplier<T> resultSupplier, final Predicate<T> exitCondition) {
		return deferUntil(() -> supplyAsync(resultSupplier), exitCondition);
	}

	/**
	 * Retries a sync supplier, wrapping it on the configured executor, until the predicate is satisfied or the wait is
	 * exhausted.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier sync result supplier
	 * @param exitCondition end predicate
	 * @param accumulator information accumulator
	 * @return a {@link CompletableFuture} with the result
	 */
	public <T, U> CompletableFuture<T> until(final Supplier<T> resultSupplier, final Predicate<T> exitCondition,
			final Accumulator<U> accumulator) {
		return deferUntil(() -> supplyAsync(resultSupplier), exitCondition, accumulator);
	}

	/**
	 * Retries a sync supplier, wrapping it on the configured executor, until the predicate is satisfied or the wait is
	 * exhausted.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier sync result supplier
	 * @param exitCondition end predicate
	 * @param beforeWait code to run before wait
	 * @param accumulator information accumulator
	 * @return a {@link CompletableFuture} with the result
	 */
	public <T, U> CompletableFuture<T> until(final Supplier<T> resultSupplier, final Predicate<T> exitCondition,
			final Consumer<U> beforeWait, final Accumulator<U> accumulator) {
		return deferUntil(() -> supplyAsync(resultSupplier), exitCondition, beforeWait, accumulator);
	}

	/**
	 * Runs a result supplier asynchronously on the wait's executor.
	 *
	 * @param <T> result type
	 *
	 * @param supplier result supplier
	 * @return a {@link CompletableFuture} with the result
	 */
	private static <T> CompletableFuture<T> supplyAsync(final Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier);
	}

	/**
	 * Returns a fluent {@link AsyncRetry} adapter for fluent style API.
	 *
	 * @param <T> result type
	 * @param <U> accumulated information type
	 * @return fluent retry adapter
	 */
	public <T, U> AsyncFluentRetry<T, U> policy() {
		return new AsyncFluentRetry<>(this);
	}

	/**
	 * Core attempt method that recursively chains retries until the exit condition is met or the wait is exhausted.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier async result supplier
	 * @param exitCondition end predicate
	 * @param afterResult code to run after result
	 * @param beforeWait code to run before wait
	 * @param accumulator information accumulator
	 * @param wait wait object
	 * @return a {@link CompletableFuture} with the result
	 */
	private static <T, U> CompletableFuture<T> attempt(final Supplier<CompletableFuture<T>> resultSupplier,
			final Predicate<T> exitCondition, final BiConsumer<T, U> afterResult, final Consumer<U> beforeWait,
			final Accumulator<U> accumulator, final AsyncWait wait) {
		final CompletableFuture<T> currentAttempt = asyncGet(resultSupplier);
		return currentAttempt.handle(AttemptOutcome::new).thenCompose(outcome -> {
			if (null != outcome.error) {
				return onError(outcome, resultSupplier, exitCondition, afterResult, beforeWait, accumulator, wait);
			}
			return onResult(outcome, resultSupplier, exitCondition, afterResult, beforeWait, accumulator, wait);
		});
	}

	/**
	 * Invokes the async supplier, converting any synchronous exception to a failed future.
	 *
	 * @param <T> result type
	 *
	 * @param resultSupplier async result supplier
	 * @return a {@link CompletableFuture} with the result
	 */
	private static <T> CompletableFuture<T> asyncGet(final Supplier<CompletableFuture<T>> resultSupplier) {
		try {
			return resultSupplier.get();
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	/**
	 * Handles a successful or non-exceptional result.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param outcome attempt outcome
	 * @param resultSupplier async result supplier
	 * @param exitCondition end predicate
	 * @param afterResult code to run after result
	 * @param beforeWait code to run before wait
	 * @param accumulator information accumulator
	 * @param wait wait object
	 * @return a {@link CompletableFuture} with the result
	 */
	private static <T, U> CompletableFuture<T> onResult(final AttemptOutcome<T> outcome,
			final Supplier<CompletableFuture<T>> resultSupplier, final Predicate<T> exitCondition,
			final BiConsumer<T, U> afterResult, final Consumer<U> beforeWait, final Accumulator<U> accumulator,
			final AsyncWait wait) {
		T result = outcome.result;
		U last = accumulator.lastInformation();
		afterResult.accept(result, last);

		if (exitCondition.test(result)) {
			return CompletableFuture.completedFuture(result);
		}
		return beforeNext(resultSupplier, exitCondition, afterResult, beforeWait, accumulator, wait, result, null);
	}

	/**
	 * Handles an exceptional completion of the current attempt.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param outcome attempt outcome
	 * @param resultSupplier async result supplier
	 * @param exitCondition end predicate
	 * @param afterResult code to run after result
	 * @param beforeWait code to run before wait
	 * @param accumulator information accumulator
	 * @param wait wait object
	 * @return a {@link CompletableFuture} with the result
	 */
	private static <T, U> CompletableFuture<T> onError(final AttemptOutcome<T> outcome,
			final Supplier<CompletableFuture<T>> resultSupplier, final Predicate<T> exitCondition,
			final BiConsumer<T, U> afterResult, final Consumer<U> beforeWait, final Accumulator<U> accumulator,
			final AsyncWait wait) {
		Throwable error = unwrap(outcome.error);

		if (accumulator == Accumulator.noAccumulator()) {
			return CompletableFuture.failedFuture(error);
		}

		accumulate(accumulator, error);
		U last = accumulator.lastInformation();
		afterResult.accept(null, last);

		return beforeNext(resultSupplier, exitCondition, afterResult, beforeWait, accumulator, wait, null, error);
	}

	/**
	 * Decides whether to retry after a failed attempt. If the wait says no more retries are available the
	 * {@link Accumulator#rest()} is invoked (which may throw) and the last result or error is returned.
	 *
	 * @param <T> result type
	 * @param <U> the accumulated type
	 *
	 * @param resultSupplier async result supplier
	 * @param exitCondition end predicate
	 * @param afterResult code to run after result
	 * @param beforeWait code to run before wait
	 * @param accumulator information accumulator
	 * @param wait wait object
	 * @param lastResult last result or null
	 * @param lastError last error or null
	 * @return a {@link CompletableFuture} with the result or error
	 */
	private static <T, U> CompletableFuture<T> beforeNext(final Supplier<CompletableFuture<T>> resultSupplier,
			final Predicate<T> exitCondition, final BiConsumer<T, U> afterResult, final Consumer<U> beforeWait,
			final Accumulator<U> accumulator, final AsyncWait wait, final T lastResult, final Throwable lastError) {
		if (!wait.keepWaiting()) {
			if (accumulator != Accumulator.noAccumulator() && accumulator.isNotEmpty()) {
				accumulator.rest();
			}
			if (null != lastError) {
				return CompletableFuture.failedFuture(lastError);
			}
			return CompletableFuture.completedFuture(lastResult);
		}
		beforeWait.accept(accumulator.lastInformation());
		return wait.defer().thenCompose(v -> attempt(resultSupplier, exitCondition, afterResult, beforeWait, accumulator, wait));
	}

	/**
	 * Unwraps a {@link CompletionException} to its root cause.
	 *
	 * @param error error to unwrap
	 * @return the root cause
	 */
	private static Throwable unwrap(final Throwable error) {
		Throwable cause = error;
		while (cause instanceof CompletionException && null != cause.getCause()) {
			cause = cause.getCause();
		}
		return cause;
	}

	/**
	 * Accumulates an error into the given accumulator, if it's an {@link Exception}.
	 *
	 * @param <U> the accumulated type
	 *
	 * @param accumulator information accumulator
	 * @param error error to accumulate
	 */
	@SuppressWarnings("unchecked")
	private static <U> void accumulate(final Accumulator<U> accumulator, final Throwable error) {
		if (error instanceof Exception e) {
			accumulator.addInformation((U) e);
		}
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof AsyncRetry that) {
			return Objects.equals(waitPrototype, that.waitPrototype);
		}
		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(waitPrototype);
	}

	/**
	 * Outcome of a single attempt, carrying either a result or an error.
	 *
	 * @param <T> result type
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

	/**
	 * A fluent adapter for the {@link AsyncRetry} class, providing a more expressive way to configure and execute async
	 * retry logic.
	 *
	 * @param <T> the type of the result produced by the retry operation.
	 * @param <U> the type of the accumulated information during retries.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class AsyncFluentRetry<T, U> {

		/**
		 * The underlying {@link AsyncRetry} instance used to execute the retry logic.
		 */
		private final AsyncRetry retry;

		/**
		 * The condition that determines when to stop retrying.
		 */
		private Predicate<T> exitCondition = Objects::nonNull;

		/**
		 * A consumer to process accumulated information before waiting between retries.
		 */
		private Consumer<U> consumeBeforeWait = Consumers.consumeNothing();

		/**
		 * The accumulator used to collect information during retries.
		 */
		private Accumulator<U> accumulator = Accumulator.empty();

		/**
		 * Constructs a new {@link AsyncFluentRetry} instance with the specified {@link AsyncRetry} configuration.
		 *
		 * @param retry the {@link AsyncRetry} instance to use for retry logic.
		 */
		private AsyncFluentRetry(final AsyncRetry retry) {
			this.retry = retry;
		}

		/**
		 * Sets the condition that determines when to stop retrying.
		 *
		 * @param exitCondition the predicate that evaluates whether to stop retrying.
		 * @return this {@link AsyncFluentRetry} instance for method chaining.
		 */
		public AsyncFluentRetry<T, U> stopWhen(final Predicate<T> exitCondition) {
			this.exitCondition = Objects.requireNonNull(exitCondition, "exitCondition cannot be null");
			return this;
		}

		/**
		 * Sets the consumer to process accumulated information before waiting between retries.
		 *
		 * @param consumeBeforeWait the consumer to process accumulated information.
		 * @return this {@link AsyncFluentRetry} instance for method chaining.
		 */
		public AsyncFluentRetry<T, U> consumeBeforeWait(final Consumer<U> consumeBeforeWait) {
			this.consumeBeforeWait = Objects.requireNonNull(consumeBeforeWait, "consumeBeforeWait cannot be null");
			return this;
		}

		/**
		 * Sets the action to execute before waiting between retries. This method effectively calls
		 * {@link #consumeBeforeWait(Consumer)} by transforming the given {@link Runnable} to a {@link Consumer} which doesn't
		 * use the input.
		 *
		 * @param doBeforeWait the action to execute before waiting.
		 * @return this {@link AsyncFluentRetry} instance for method chaining.
		 */
		public AsyncFluentRetry<T, U> doBeforeWait(final Runnable doBeforeWait) {
			Objects.requireNonNull(doBeforeWait, "doBeforeWait cannot be null");
			return consumeBeforeWait(u -> doBeforeWait.run());
		}

		/**
		 * Sets the accumulator used to collect information during retries.
		 *
		 * @param <A> the accumulator type
		 *
		 * @param accumulator the accumulator to use for collecting information.
		 * @return this {@link AsyncFluentRetry} instance for method chaining.
		 */
		public <A extends Accumulator<U>> AsyncFluentRetry<T, U> accumulateWith(final A accumulator) {
			this.accumulator = Objects.requireNonNull(accumulator, "accumulator cannot be null");
			return this;
		}

		/**
		 * Executes the async retry logic using the provided async result supplier.
		 *
		 * @param resultSupplier the supplier that produces an async result to evaluate.
		 * @return a {@link CompletableFuture} with the result of the retry operation.
		 */
		public CompletableFuture<T> onAsync(final Supplier<CompletableFuture<T>> resultSupplier) {
			return retry.deferUntil(resultSupplier, exitCondition, consumeBeforeWait, accumulator);
		}

		/**
		 * Executes the async retry logic using the provided sync result supplier, wrapping it on the configured executor.
		 *
		 * @param resultSupplier the supplier that produces a result to evaluate.
		 * @return a {@link CompletableFuture} with the result of the retry operation.
		 */
		public CompletableFuture<T> on(final Supplier<T> resultSupplier) {
			return retry.until(resultSupplier, exitCondition, consumeBeforeWait, accumulator);
		}
	}
}
