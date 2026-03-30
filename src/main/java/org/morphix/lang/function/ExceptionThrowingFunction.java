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

import java.util.function.Function;

/**
 * Functional interface to re-throw checked exceptions in functional calls. This class specifically accommodates any
 * {@link Function}.
 *
 * @param <T> argument type
 * @param <R> return type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ExceptionThrowingFunction<T, R> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 * @throws Exception on any error
	 */
	R apply(T t) throws Exception; // NOSONAR this declaration is only to accommodate all checked exceptions
}
