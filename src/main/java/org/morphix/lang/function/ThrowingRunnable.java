/*
 * Copyright 2025 the original author or authors.
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

import org.morphix.lang.Unchecked;

/**
 * Functional interface to re-throw unchecked exceptions in functional calls. This class specifically accommodates any
 * {@link Runnable}.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ThrowingRunnable {

	/**
	 * Runs this operation.
	 *
	 * @throws Throwable on any error
	 */
	void run() throws Throwable; // NOSONAR this declaration is only to accommodate all cases

	/**
	 * Removes the need to surround code with try/catch for checked exceptions effectively working like an unchecked
	 * exception.
	 *
	 * @param r throwing runnable
	 * @return the runnable
	 */
	static Runnable unchecked(final ThrowingRunnable r) {
		return () -> {
			try {
				r.run();
			} catch (Throwable e) {
				Unchecked.reThrow(e);
			}
		};
	}
}
