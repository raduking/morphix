package org.morphix;

import java.util.Map;

import org.morphix.function.ExtraConvertFunction;
import org.morphix.function.InstanceFunction;
import org.morphix.function.SimpleConverter;

/**
 * Utility interface for conversion static methods.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ConversionFromMap {

	/**
	 * Convenience static conversion method to convert from a map to an object.
	 * The map contains field names as keys and objects as values.
	 *
	 * @param sourceMap source map object
	 * @param instanceFunction destination instance function
	 * @return destination object
	 */
	static <V, D> D convertFromMap(final Map<String, V> sourceMap, final InstanceFunction<D> instanceFunction) {
		return ConverterBuilder.<V, D>newMapConverter().convert(sourceMap, instanceFunction);
	}

	/**
	 * Convenience static conversion method to convert from a map to an object.
	 * The map contains field names as keys and objects as values.
	 *
	 * @param sourceMap source map object
	 * @param instanceFunction destination instance function
	 * @return destination object
	 */
	static <V, D> D convertFromMap(final Map<String, V> sourceMap, final InstanceFunction<D> instanceFunction,
			final ExtraConvertFunction<Map<String, V>, D> extraConvertFunction) {
		return ConverterBuilder.<V, D>newMapConverter().convert(sourceMap, instanceFunction, extraConvertFunction);
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
	static <K, S, H, D> MapConversionResult<K, S, H, D> convertMap(final Map<K, S> sourceMap, final InstanceFunction<H> keyInstanceFunction,
			final InstanceFunction<D> valueInstanceFunction) {
		return new MapConversionResult<>(sourceMap, keyInstanceFunction, valueInstanceFunction);
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
	static <K, S, H, D> MapConversionResult<K, S, H, D> convertMap(final Map<K, S> sourceMap, final SimpleConverter<K, H> keyConverter,
			final SimpleConverter<S, D> valueConverter) {
		return new MapConversionResult<>(sourceMap, keyConverter, valueConverter);
	}
}
