package org.morphix.function;

/**
 * Conversion functional interface for conversions defined in other classes.
 *
 * @param <S> source type
 * @param <D> destination type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ConverterWithInstance<S, D> {

	/**
	 * Converts the source to destination and supplies an instance function for
	 * the destination.
	 *
	 * @param source source object
	 * @param destinationInstanceFunction destination instance function
	 * @return destination object
	 */
	D convert(S source, InstanceFunction<D> destinationInstanceFunction);

}
