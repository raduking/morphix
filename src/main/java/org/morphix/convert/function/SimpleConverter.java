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
package org.morphix.convert.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Conversion functional interface for conversions defined in other classes. The {@link Serializable} base interface is
 * needed for lambda type resolving inside the converter.
 *
 * @param <S> source type
 * @param <D> destination type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface SimpleConverter<S, D> extends Function<S, D>, Serializable {

	/**
	 * Converts from source to destination.
	 *
	 * @param source source object
	 * @return destination object
	 */
	D convert(S source);

	/**
	 * @see Function#apply(Object)
	 */
	@Override
	default D apply(final S source) {
		return convert(source);
	}
}
