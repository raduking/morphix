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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.morphix.convert.extras.ConversionContext;
import org.morphix.convert.function.ConvertFunction;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.convert.pipeline.MapConversionPipeline;
import org.morphix.convert.strategy.ConversionStrategy;
import org.morphix.lang.function.InstanceFunction;
import org.morphix.lang.function.PutFunction;
import org.morphix.reflection.ExtendedField;

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
		return convertFromMap(sourceMap, instanceFunction, Configuration.defaults());
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
	 * @param configuration configuration object
	 * @return destination object
	 */
	static <V, D> D convertFromMap(final Map<String, V> sourceMap, final InstanceFunction<D> instanceFunction, final Configuration configuration) {
		return ConverterFactory.<V, D>newMapObjectConverter(configuration).convert(sourceMap, instanceFunction);
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
	 * @param configuration configuration object
	 * @return destination object
	 */
	static <V, D> D convertFromMap(final Map<String, V> sourceMap, final InstanceFunction<D> instanceFunction,
			final ConvertFunction<Map<String, V>, D> extraConvertFunction, final Configuration configuration) {
		return ConverterFactory.<V, D>newMapObjectConverter(configuration).convert(sourceMap, instanceFunction, extraConvertFunction);
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
		return convertFromMap(sourceMap, instanceFunction, extraConvertFunction, Configuration.defaults());
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
	 * @param putFunction put function
	 * @return destination map
	 */
	static <K, S, H, D> MapConversionPipeline<K, S, H, D> convertMap(final Map<K, S> sourceMap, final SimpleConverter<K, H> keyConverter,
			final SimpleConverter<S, D> valueConverter, final PutFunction<H, D> putFunction) {
		return new MapConversionPipeline<>(sourceMap, keyConverter, valueConverter, putFunction);
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
		return convertMap(sourceMap, keyConverter, valueConverter, Map::put);
	}

	/**
	 * Convenience static method to convert an object to a map conversion pipeline. If the source is null, an empty map is
	 * returned. The putFunction is used to put values into the map and can be used to customize the behavior (e.g., to
	 * handle specific types or to apply transformations or even filtering).
	 * <p>
	 * If the source is null, an empty map conversion pipeline is returned.
	 * <p>
	 * For sources that are already maps, use the {@link #convertMap(Map, SimpleConverter, SimpleConverter, PutFunction)}.
	 *
	 * @param <S> source type
	 * @param <H> map key type
	 * @param <D> map value type
	 *
	 * @param source source object
	 * @param keyConverter key converter function
	 * @param valueConverter value converter function
	 * @param putFunction map put function
	 * @return destination map conversion pipeline
	 * @throws NullPointerException if any of the converters or the map instance function is null
	 */
	static <S, H, D> MapConversionPipeline<String, Object, H, D> convert(final S source, final SimpleConverter<String, H> keyConverter,
			final SimpleConverter<Object, D> valueConverter, final PutFunction<String, Object> putFunction) {
		if (source == null) {
			return convertMap(Map.of(), keyConverter, valueConverter);
		}
		List<ExtendedField> fields = ConversionStrategy.findFields(source);
		Map<String, Object> map = HashMap.newHashMap(fields.size());
		for (ExtendedField field : fields) {
			putFunction.put(map, field.getName(), field.getFieldValue());
		}
		return convertMap(map, keyConverter, valueConverter);
	}

	/**
	 * Convenience static method to convert an object to a map. If the source is null, an empty map is returned. The
	 * putFunction is used to put values into the map and can be used to customize the behavior (e.g., to handle specific
	 * types or to apply transformations or even filtering).
	 * <p>
	 * If the source is null, an empty map returned.
	 * <p>
	 * For sources that are already maps, use the {@link #convertMap(Map, SimpleConverter, SimpleConverter, PutFunction)}.
	 *
	 * @param <S> source type
	 * @param <H> map key type
	 * @param <D> map value type
	 *
	 * @param source source object
	 * @param keyConverter key converter function
	 * @param valueConverter value converter function
	 * @param putFunction map put function
	 * @return destination map
	 */
	static <S, H, D> Map<H, D> convertToMap(final S source, final SimpleConverter<String, H> keyConverter,
			final SimpleConverter<Object, D> valueConverter, final PutFunction<String, Object> putFunction) {
		return convert(source, keyConverter, valueConverter, putFunction).toMap();
	}

	/**
	 * Convenience static method to convert an object to a map. If the source is null, an empty map is returned.
	 * <p>
	 * If the source is null, an empty map returned.
	 * <p>
	 * For sources that are already maps, use the {@link #convertMap(Map, SimpleConverter, SimpleConverter)}.
	 *
	 * @param <S> source type
	 * @param <H> map key type
	 * @param <D> map value type
	 *
	 * @param source source object
	 * @param keyConverter key converter function
	 * @param valueConverter value converter function
	 * @return destination map
	 * @throws NullPointerException if any of the converters or the map instance function is null
	 */
	static <S, H, D> Map<H, D> convertToMap(final S source, final SimpleConverter<String, H> keyConverter,
			final SimpleConverter<Object, D> valueConverter) {
		return convertToMap(source, keyConverter, valueConverter, Map::put);
	}

	/**
	 * Convenience static method to convert an object to a properties map. The keys are the field names and the values are
	 * the field values. The values are converted to simple types (e.g., String, Number, Boolean, Enum, UUID) or to maps or
	 * collections of simple types. If a value is an object that is not a simple type, it is converted to a map recursively.
	 * <p>
	 * If the source is null, an empty map returned.
	 * <p>
	 * For sources that are already maps, use the {@link #convertMap(Map, SimpleConverter, SimpleConverter)}.
	 *
	 * @param <S> source type
	 *
	 * @param source source object
	 * @return destination map
	 */
	static <S> Map<String, Object> toPropertiesMap(final S source) {
		return toPropertiesMap(source, new ConversionContext());
	}

	/**
	 * Convenience static method to convert an object to a properties map. The keys are the field names and the values are
	 * the field values. The values are converted to simple types (e.g., String, Number, Boolean, Enum, UUID) or to maps or
	 * collections of simple types. If a value is an object that is not a simple type, it is converted to a map recursively.
	 * <p>
	 * If the source is null, an empty map returned.
	 * <p>
	 * For sources that are already maps, use the {@link #convertMap(Map, SimpleConverter, SimpleConverter)}.
	 *
	 * @param <S> source type
	 *
	 * @param source source object
	 * @param context conversion context
	 * @return destination map
	 */
	static <S> Map<String, Object> toPropertiesMap(final S source, final ConversionContext context) {
		if (null == source) {
			return Map.of();
		}
		return context.onObject(source,
				() -> MapConversions.convertToMap(source, k -> k, v -> convertValue(v, context)),
				() -> Map.of(ConversionContext.CYCLIC_REFERENCE, source.getClass().getSimpleName()));
	}

	/**
	 * Convenience static method to convert a value to a simple type (e.g., String, Number, Boolean, Enum, UUID) or to a map
	 * or collection of simple types. If the value is an object that is not a simple type, it is converted to a map
	 * recursively.
	 *
	 * @param v value to convert
	 * @param context conversion context
	 * @return converted value
	 */
	private static Object convertValue(final Object v, final ConversionContext context) {
		return switch (v) {
			case null -> null;

			case CharSequence cs -> cs.toString();
			case Number n -> n.toString();
			case Boolean b -> b.toString();
			case Enum<?> e -> e.name();
			case UUID u -> u.toString();

			case Optional<?> opt -> opt
					.map(o -> MapConversions.convertValue(o, context))
					.orElse(null);

			case Map<?, ?> map -> MapConversions.convertMap(map, String::valueOf, value -> convertValue(value, context)).toMap();
			case Collection<?> col -> col.stream()
					.map(o -> MapConversions.convertValue(o, context))
					.toList();

			case Object[] arr -> Arrays.stream(arr)
					.map(o -> MapConversions.convertValue(o, context))
					.toList();

			default -> toPropertiesMap(v, context);
		};
	}
}
