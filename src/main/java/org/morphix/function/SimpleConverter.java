package org.morphix.function;

import java.io.Serializable;

/**
 * Conversion functional interface for conversions defined in other classes. The
 * {@link Serializable} base interface is needed for lambda type resolving
 * inside the converter.
 *
 * @param <S> source type
 * @param <D> destination type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface SimpleConverter<S, D> extends Serializable {

	/**
	 * Converts from source to destination.
	 *
	 * @param source source object
	 * @return destination object
	 */
	D convert(S source);

}
