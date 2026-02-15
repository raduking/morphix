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

import java.util.function.BinaryOperator;

/**
 * Utility methods for common {@link BinaryOperator} implementations.
 *
 * @author Radu Sebastian LAZIN
 */
public interface BinaryOperators {

	/**
	 * Returns a {@link BinaryOperator} that always returns the first argument.
	 *
	 * @param <T> the type of the operands and result of the operator
	 *
	 * @return a binary operator that always returns the first argument
	 */
	static <T> BinaryOperator<T> first() {
		return (first, second) -> first;
	}

	/**
	 * Returns a {@link BinaryOperator} that always returns the second argument.
	 *
	 * @param <T> the type of the operands and result of the operator
	 *
	 * @return a binary operator that always returns the second argument
	 */
	static <T> BinaryOperator<T> second() {
		return (first, second) -> second;
	}

}
