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

import java.util.List;

/**
 * Helper interface for method calling.
 *
 * @author Radu Sebastian LAZIN
 */
public interface MethodCaller {

	/**
	 * Calls the {@code setterFunction} with the value returned by the {@code fieldValueFunction}.
	 *
	 * @param <T> value type
	 *
	 * @param setterFunction a function which sets the value
	 * @param valueFunction function which provides the value to set
	 * @throws Exception in case of any error
	 */
	static <T> void call(final SetterFunction<T> setterFunction, final ValueFunction<T> valueFunction) throws Exception {
		setterFunction.set(valueFunction.value());
	}

	/**
	 * Calls the {@code setterFunction} with the value returned by the {@code fieldValueFunction}. Swallows any exception
	 * thrown by the {@code fieldValueFunction} and will return it in the {@code exceptions} list given as parameter.
	 *
	 * @param <T> value type
	 *
	 * @param setterFunction a function which sets the value
	 * @param valueFunction function which provides the value to set
	 * @param exceptions list of exceptions thrown by the operations in this call
	 */
	static <T> void call(final SetterFunction<T> setterFunction, final ValueFunction<T> valueFunction,
			final List<? super Exception> exceptions) {
		try {
			call(setterFunction, valueFunction);
		} catch (Exception e) {
			exceptions.add(e);
		}
	}

	/**
	 * Calls the setter function only if the value is not null.
	 *
	 * @param <T> value type
	 *
	 * @param setterFunction setter function
	 * @param value value to set
	 */
	static <T> void nonNullCall(final SetterFunction<T> setterFunction, final T value) {
		SetterFunction.nonNullSetter(setterFunction).set(value);
	}
}
