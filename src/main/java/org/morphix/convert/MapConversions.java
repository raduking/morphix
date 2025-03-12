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

import java.util.Map;

import org.morphix.convert.function.ConvertFunction;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.convert.pipeline.MapConversionPipeline;
import org.morphix.lang.function.InstanceFunction;

/**
 * Utility interface for conversion static methods.
 *
 * @author Radu Sebastian LAZIN
 */
public interface MapConversions {

	/**
	 * Convenience static conversion method to convert from a map to an object. The map contains field names as keys and
	 * objects as values.
	 *
	 * @param <V> map value type
	 * @param <D> destination type
	 *
	 * @param sourceMap source map object
	 * @param instanceFunction destination instance function
	 * @return destination object
	 */
	static <V, D> D convertFromMap(final Map<String, V> sourceMap, final InstanceFunction<D> instanceFunction) {
		return ConverterFactory.<V, D>newMapObjectConverter().convert(sourceMap, instanceFunction);
	}

	/**
	 * Convenience static conversion method to convert from a map to an object. The map contains field names as keys and
	 * objects as values.
	 *
	 * @param <V> map value type
	 * @param <D> destination type
	 *
	 * @param sourceMap source map object
	 * @param instanceFunction destination instance function
	 * @param extraConvertFunction extra convert function
	 * @return destination object
	 */
	static <V, D> D convertFromMap(final Map<String, V> sourceMap, final InstanceFunction<D> instanceFunction,
			final ConvertFunction<Map<String, V>, D> extraConvertFunction) {
		return ConverterFactory.<V, D>newMapObjectConverter().convert(sourceMap, instanceFunction, extraConvertFunction);
	}

	/**
	 * Convenience static method for converting maps.
	 *
	 * @param <K> source key type
	 * @param <S> source value type
	 * @param <H> destination key type
	 * @param <D> destination value type
	 *
	 * @param sourceMap source map
	 * @param keyInstanceFunction key instance function
	 * @param valueInstanceFunction value instance function
	 * @return destination map
	 */
	static <K, S, H, D> MapConversionPipeline<K, S, H, D> convertMap(final Map<K, S> sourceMap, final InstanceFunction<H> keyInstanceFunction,
			final InstanceFunction<D> valueInstanceFunction) {
		return new MapConversionPipeline<>(sourceMap, keyInstanceFunction, valueInstanceFunction);
	}

	/**
	 * Convenience static method for converting maps.
	 *
	 * @param <K> source key type
	 * @param <S> source value type
	 * @param <H> destination key type
	 * @param <D> destination value type
	 *
	 * @param sourceMap source map
	 * @param keyConverter key converter function
	 * @param valueConverter value converter function
	 * @return destination map
	 */
	static <K, S, H, D> MapConversionPipeline<K, S, H, D> convertMap(final Map<K, S> sourceMap, final SimpleConverter<K, H> keyConverter,
			final SimpleConverter<S, D> valueConverter) {
		return new MapConversionPipeline<>(sourceMap, keyConverter, valueConverter);
	}
}
