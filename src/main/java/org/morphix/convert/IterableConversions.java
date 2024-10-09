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
package org.morphix.convert;

import static org.morphix.convert.Conversions.convertFrom;

import java.util.Collection;
import java.util.List;

import org.morphix.convert.annotation.Expandable;
import org.morphix.convert.extras.ExpandableFields;
import org.morphix.convert.extras.SimpleConverters;
import org.morphix.convert.function.ConvertFunction;
import org.morphix.convert.function.InstanceConvertFunction;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.convert.pipeline.IterableConversionPipeline;
import org.morphix.lang.function.InstanceFunction;

/**
 * Extended conversions that can specify {@link Iterable} return types.
 *
 * @author Radu Sebastian LAZIN
 */
public interface IterableConversions {

	/**
	 * Convenience static conversion method for converting {@link Iterable} to any {@link Collection} using an instance
	 * function for creating instances for each element.
	 * <p>
	 * You can select the result container using the following syntax:
	 *
	 * <pre>
	 * {@code
	 * List<A> source = ...;
	 * Set<B> result = convertIterable(source, B::new).toSet();
	 * }
	 * </pre>
	 *
	 * @param <S> source element type
	 * @param <D> destination element type
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the destination
	 * @param extraConvertFunction extra convert function
	 * @param configuration converter configuration
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionPipeline<S, D> convertIterable(
			final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction,
			final ConvertFunction<S, D> extraConvertFunction,
			final Configuration configuration) {
		return new IterableConversionPipeline<>(sourceIterable,
				(final S source) -> convertFrom(source, elementInstanceFunction, extraConvertFunction, configuration));
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to any {@link Collection} using an instance
	 * function for creating instances for each element.
	 *
	 * @param <S> source element type
	 * @param <D> destination element type
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the destination
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionPipeline<S, D> convertIterable(final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction) {
		return convertIterable(sourceIterable, elementInstanceFunction, ConvertFunction.empty(),
				Configuration.defaultConfiguration());
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to any {@link Collection} using an instance
	 * function for creating instances for each element.
	 *
	 * @param <S> source element type
	 * @param <D> destination element type
	 * @param <T> conversion method source type
	 * @param <E> conversion method destination type
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the destination
	 * @param convertMethod external conversion method
	 * @return list of converted destination objects
	 */
	static <S, D, T, E> IterableConversionPipeline<S, D> convertIterable(final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction,
			final SimpleConverter<T, E> convertMethod) {
		return convertIterable(sourceIterable, elementInstanceFunction, ConvertFunction.empty(),
				Configuration.of(ExpandableFields.expandAll(), SimpleConverters.of(convertMethod)));
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to any {@link Collection} using an instance
	 * function for creating instances for each element.
	 *
	 * @param <S> source element type
	 * @param <D> destination element type
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the destination
	 * @param simpleConverters external conversion method
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionPipeline<S, D> convertIterable(final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction,
			final SimpleConverters simpleConverters) {
		return convertIterable(sourceIterable, elementInstanceFunction, ConvertFunction.empty(),
				Configuration.of(ExpandableFields.expandAll(), simpleConverters));
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to any {@link Collection} using an external
	 * conversion method for each element.
	 * <p>
	 * The externalConvertFunction has the form:
	 * <p>
	 * <code>public D convert(S source) { ... }</code>
	 *
	 * @param <S> source element type
	 * @param <D> destination element type
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementConverterFunction external conversion function (method)
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionPipeline<S, D> convertIterable(final Iterable<S> sourceIterable,
			final SimpleConverter<S, D> elementConverterFunction) {
		return new IterableConversionPipeline<>(sourceIterable, elementConverterFunction);
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to any {@link Collection} using an external
	 * conversion method for each element and instance function for creating instances for each element.
	 * <p>
	 * The externalConvertFunction has the form:
	 * <p>
	 * <code>public D convert(S source, InstanceFunction&lt;D&gt; instanceFunction) { ... }</code>
	 *
	 * @param <S> source element type
	 * @param <D> destination element type
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param externalElementConvertFunction conversion method for each element which has an instance as parameter
	 * @param elementInstanceFunction instance function for each element in the destination
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionPipeline<S, D> convertIterable(final Iterable<S> sourceIterable,
			final InstanceConvertFunction<S, D> externalElementConvertFunction, final InstanceFunction<D> elementInstanceFunction) {
		return new IterableConversionPipeline<>(sourceIterable,
				(final S source) -> externalElementConvertFunction.convert(source, elementInstanceFunction));
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to any {@link Collection} using an extra
	 * conversion method for each element and instance function for creating instances for each element.
	 * <p>
	 * The extraConvertFunction has the form:
	 * <p>
	 * <code>public void convert(S source, D destination) { ... }</code>
	 *
	 * @param <S> source element type
	 * @param <D> destination element type
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the destination
	 * @param extraConvertFunction extra conversion function
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionPipeline<S, D> convertIterable(final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction,
			final ConvertFunction<S, D> extraConvertFunction) {
		return new IterableConversionPipeline<>(sourceIterable,
				(final S source) -> convertFrom(source, elementInstanceFunction, extraConvertFunction));
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to any {@link Collection} using an instance
	 * function for creating instances for each element.
	 *
	 * @param <S> source element type
	 * @param <D> destination element type
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the destination
	 * @param expandedFieldNames fields that will be expanded and have the {@link Expandable} annotation.
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionPipeline<S, D> convertIterable(final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction,
			final List<String> expandedFieldNames) {
		return convertIterable(sourceIterable, elementInstanceFunction, ConvertFunction.empty(),
				Configuration.of(expandedFieldNames, SimpleConverters.empty()));
	}

}
