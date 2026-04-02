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

import java.util.function.Supplier;

/**
 * Functional interface representing a wrapper for executing code with additional behavior. This interface allows you to
 * wrap the execution of a supplier or a runnable with custom logic, such as logging, error handling, or performance
 * monitoring.
 *
 * @param <T> the type of the result produced by the supplier
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ExecutionWrapper<T> {

	/**
	 * Wraps the execution of a supplier with additional behavior.
	 *
	 * @param supplier the supplier to be wrapped
	 * @return a new supplier that includes the additional behavior
	 */
	Supplier<T> wrap(Supplier<T> supplier);

	/**
	 * Composes this wrapper with another wrapper, allowing you to chain multiple wrappers together. The order of execution
	 * will be: this wrapper's behavior will be applied first, followed by the other wrapper's behavior.
	 *
	 * @param other the other wrapper to compose with
	 * @return a new wrapper that combines the behavior of both wrappers
	 */
	default ExecutionWrapper<T> andThen(final ExecutionWrapper<T> other) {
		return supplier -> wrap(other.wrap(supplier));
	}

	/**
	 * Composes this wrapper with another wrapper, allowing you to chain multiple wrappers together. The order of execution
	 * will be: the other wrapper's behavior will be applied first, followed by this wrapper's behavior.
	 *
	 * @param other the other wrapper to compose with
	 * @return a new wrapper that combines the behavior of both wrappers
	 */
	default ExecutionWrapper<T> compose(final ExecutionWrapper<T> other) {
		return supplier -> other.wrap(wrap(supplier));
	}

	/**
	 * Executes the given supplier using this wrapper, applying any additional behavior defined in the wrapper.
	 *
	 * @param supplier the supplier to be executed
	 * @return the result of executing the supplier with the wrapper's behavior applied
	 */
	default T execute(final Supplier<T> supplier) {
		return wrap(supplier).get();
	}

	/**
	 * Executes the given runnable using this wrapper, applying any additional behavior defined in the wrapper. The result
	 * of the execution will be {@code null}.
	 *
	 * @param runnable the runnable to be executed
	 */
	default void execute(final Runnable runnable) {
		execute(Runnables.toSupplier(runnable));
	}

	/**
	 * Returns an identity wrapper that does not modify the behavior of the supplier. This wrapper simply returns the
	 * original supplier without any additional behavior.
	 *
	 * @param <T> the type of the supplier's result
	 *
	 * @return an identity wrapper that does not modify the behavior of the supplier
	 */
	static <T> ExecutionWrapper<T> identity() {
		return supplier -> supplier;
	}

	/**
	 * Creates a wrapper that executes the given {@code before} runnable before the supplier and the given {@code after}
	 * runnable after the supplier. This allows you to easily add pre- and post-execution behavior around a supplier.
	 *
	 * @param <T> the type of the supplier's result
	 *
	 * @param before the runnable to be executed before the supplier
	 * @param after the runnable to be executed after the supplier
	 * @return a wrapper that executes the given runnables before and after the supplier
	 */
	static <T> ExecutionWrapper<T> around(final Runnable before, final Runnable after) {
		return supplier -> () -> {
			before.run();
			try {
				return supplier.get();
			} finally {
				after.run();
			}
		};
	}
}
