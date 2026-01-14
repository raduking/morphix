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
package org.morphix.convert;

import static org.morphix.convert.Conversions.convertFrom;

import java.util.Collection;

import org.morphix.convert.function.SimpleConverter;
import org.morphix.convert.pipeline.ArrayConversionPipeline;
import org.morphix.lang.function.InstanceFunction;

/**
 * Extended conversions that can specify array element return types.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ArrayConversions {

	/**
	 * Convenience static conversion method for converting array to any {@link Collection} or array using an external
	 * conversion method for each element.
	 * <p>
	 * The externalConvertFunction has the form:
	 * <p>
	 * <code>public D convert(S source) { ... }</code>
	 *
	 * @param <S> source element type
	 * @param <D> destination element type
	 *
	 * @param sourceArray an array of source objects
	 * @param elementConverterFunction external conversion function (method)
	 * @return list of converted destination objects
	 */
	static <S, D> ArrayConversionPipeline<S, D> convertArray(final S[] sourceArray,
			final SimpleConverter<S, D> elementConverterFunction) {
		return new ArrayConversionPipeline<>(sourceArray, elementConverterFunction);
	}

	/**
	 * Convenience static conversion method for converting array to any {@link Collection} using an instance function for
	 * creating instances for each element.
	 *
	 * @param <S> source element type
	 * @param <D> destination element type
	 *
	 * @param sourceArray an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the destination
	 * @return list of converted destination objects
	 */
	static <S, D> ArrayConversionPipeline<S, D> convertArray(final S[] sourceArray,
			final InstanceFunction<D> elementInstanceFunction) {
		return convertArray(sourceArray, (final S source) -> convertFrom(source, elementInstanceFunction));
	}

}
