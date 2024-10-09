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
package org.morphix.convert.pipeline;

import static org.morphix.convert.Conversions.convertEnvelopedFrom;

import java.util.HashMap;
import java.util.Map;

import org.morphix.convert.Configuration;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.InstanceFunction;

/**
 * Wrapper over the {@link Map} conversions as a pipeline between the source and destination.
 *
 * @param <I> source key type
 * @param <S> source value type
 * @param <J> destination key type
 * @param <D> destination value type
 *
 * @author Radu Sebastian LAZIN
 */
public class MapConversionPipeline<I, S, J, D> {

	private final Map<I, S> sourceMap;

	private InstanceFunction<J> keyInstanceFunction;
	private InstanceFunction<D> valueInstanceFunction;

	private SimpleConverter<I, J> keyConverter;
	private SimpleConverter<S, D> valueConverter;

	/**
	 * Constructor.
	 *
	 * @param sourceMap source map
	 * @param keyInstanceFunction key instance function
	 * @param valueInstanceFunction value instance function
	 */
	public MapConversionPipeline(final Map<I, S> sourceMap, final InstanceFunction<J> keyInstanceFunction,
			final InstanceFunction<D> valueInstanceFunction) {
		this.sourceMap = sourceMap;
		this.keyInstanceFunction = keyInstanceFunction;
		this.valueInstanceFunction = valueInstanceFunction;
	}

	/**
	 * Constructor.
	 *
	 * @param sourceMap source map
	 * @param keyConverter key converter
	 * @param valueConverter value converter
	 */
	public MapConversionPipeline(final Map<I, S> sourceMap, final SimpleConverter<I, J> keyConverter, final SimpleConverter<S, D> valueConverter) {
		this.sourceMap = sourceMap;
		this.keyConverter = keyConverter;
		this.valueConverter = valueConverter;
	}

	/**
	 * Destination conversion.
	 *
	 * @param <T> destination map type
	 *
	 * @param result result map
	 * @return destination map
	 */
	public <T extends Map<J, D>> T to(final Map<J, D> result) {
		for (Map.Entry<I, S> entry : this.sourceMap.entrySet()) {
			J destinationKey = hasKeyInstanceFunction()
					? convertEnvelopedFrom(entry.getKey(), keyInstanceFunction, Configuration.defaultConfiguration())
					: keyConverter.convert(entry.getKey());
			D destinationValue = hasValueInstanceFunction()
					? convertEnvelopedFrom(entry.getValue(), valueInstanceFunction, Configuration.defaultConfiguration())
					: valueConverter.convert(entry.getValue());
			result.put(destinationKey, destinationValue);
		}
		return JavaObjects.cast(result);
	}

	/**
	 * Destination conversion.
	 *
	 * @param <T> destination map type
	 *
	 * @param mapInstanceFunction destination map instance function
	 * @return destination map
	 */
	public <T extends Map<J, D>> T to(final InstanceFunction<Map<J, D>> mapInstanceFunction) {
		return to(mapInstanceFunction.instance());
	}

	/**
	 * Destination conversion to map.
	 *
	 * @return destination map
	 */
	public Map<J, D> toMap() {
		return to(HashMap::new);
	}

	/**
	 * Returns true if the pipeline has a key instance function, false otherwise.
	 *
	 * @return true if the pipeline has a key instance function, false otherwise
	 */
	private boolean hasKeyInstanceFunction() {
		return null != keyInstanceFunction;
	}

	/**
	 * Returns true if the pipeline has a value instance function, false otherwise.
	 *
	 * @return true if the pipeline has a value instance function, false otherwise
	 */
	private boolean hasValueInstanceFunction() {
		return null != valueInstanceFunction;
	}
}
