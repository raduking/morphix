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
package org.morphix.lang.function;

import org.morphix.lang.function.LoggerAdapter.LoggingLevel;
import org.morphix.reflection.Constructors;

/**
 * Utility class providing common {@link ExecutionWrapper} implementations.
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * ExecutionWrapper<Void> loggingWrapper = ExecutionWrappers.log(logger, "refresh");
 * ExecutionWrapper<Void> timingWrapper = ExecutionWrappers.time(logger, "refresh");
 * ExecutionWrapper<Void> wrapper = loggingWrapper.andThen(timingWrapper);
 *
 * wrapper.execute(() -> refreshToken());
 * }</pre>
 *
 * @author Radu Sebastian LAZIN
 */
public final class ExecutionWrappers {

	/**
	 * Creates an {@link ExecutionWrapper} that logs the start and end of execution using the provided
	 * {@link LoggerAdapter}. The default logging level is {@link LoggingLevel#DEBUG}.
	 *
	 * @param logger the logger to use for logging execution events
	 * @param name a name to identify the execution in the log messages
	 * @param <T> the type of the result produced by the supplier
	 * @return an {@link ExecutionWrapper} that logs execution events
	 */
	public static <T> ExecutionWrapper<T> log(final LoggerAdapter logger, final String name) {
		return log(logger, LoggingLevel.DEBUG, name);
	}

	/**
	 * Creates an {@link ExecutionWrapper} that logs the start and end of execution using the provided {@link LoggerAdapter}
	 * and logging level.
	 *
	 * @param logger the logger to use for logging execution events
	 * @param level the logging level to use for logging execution events
	 * @param name a name to identify the execution in the log messages
	 * @param <T> the type of the result produced by the supplier
	 * @return an {@link ExecutionWrapper} that logs execution events
	 */
	public static <T> ExecutionWrapper<T> log(final LoggerAdapter logger, final LoggingLevel level, final String name) {
		return ExecutionWrapper.around(
				() -> logger.log(level, "[{}] Starting execution.", name),
				() -> logger.log(level, "[{}] Finished execution.", name));
	}

	/**
	 * Creates an {@link ExecutionWrapper} that measures and logs the execution time of the wrapped supplier using the
	 * provided {@link LoggerAdapter}.
	 *
	 * @param logger the logger to use for logging execution time
	 * @param name a name to identify the execution in the log messages
	 * @param <T> the type of the result produced by the supplier
	 * @return an {@link ExecutionWrapper} that measures and logs execution time
	 */
	public static <T> ExecutionWrapper<T> time(final LoggerAdapter logger, final String name) {
		return supplier -> () -> {
			long start = System.nanoTime();
			try {
				return supplier.get();
			} finally {
				long duration = System.nanoTime() - start;
				logger.debug("[{}] Execution took {}ms.", name, duration / 1_000_000);
			}
		};
	}

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private ExecutionWrappers() {
		throw Constructors.unsupportedOperationException();
	}
}
