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
package org.morphix.convert.function;

import org.morphix.convert.ObjectConverter;
import org.morphix.lang.function.InstanceFunction;

/**
 * Conversion functional interface for defining extra conversions.
 * {@link ObjectConverter#convert(Object, InstanceFunction, ConvertFunction)}
 *
 * @param <S> source type
 * @param <D> destination type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ConvertFunction<S, D> {

	/**
	 * Encapsulates logic for converting source to destination.
	 *
	 * @param source source object
	 * @param destination destination object
	 */
	void convert(S source, D destination);

	/**
	 * Returns an empty extra convert function.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 *
	 * @return an empty extra convert function
	 */
	static <S, D> ConvertFunction<S, D> empty() {
		return (src, dst) -> {
			// empty
		};
	}

	/**
	 * Returns a composed function similar to the mathematical function composition.
	 *
	 * @param before function to call before
	 * @return composed function
	 */
	default ConvertFunction<S, D> compose(final ConvertFunction<S, D> before) {
		return (src, dst) -> {
			before.convert(src, dst);
			convert(src, dst);
		};
	}

}
