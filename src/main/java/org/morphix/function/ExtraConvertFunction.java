package org.morphix.function;

import static org.morphix.function.SetterFunction.nonNullSetter;

import org.morphix.Converter;

/**
 * Conversion functional interface for defining extra conversions.
 * {@link Converter#convert(Object, InstanceFunction, ExtraConvertFunction)}
 *
 * @param <S> source type
 * @param <D> destination type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface ExtraConvertFunction<S, D> {

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
	 * @return an empty extra convert function
	 */
	static <S, D> ExtraConvertFunction<S, D> empty() {
		return (s, d) -> {
			// empty
		};
	}

	/**
	 * Returns a composed function similar to the mathematical function
	 * composition.
	 *
	 * @param before function to call before
	 * @return composed function
	 */
	default ExtraConvertFunction<S, D> compose(final ExtraConvertFunction<S, D> before) {
		return (src, dst) -> {
			before.convert(src, dst);
			convert(src, dst);
		};
	}

	/**
	 * Maps a setter to a getter.
	 *
	 * @param setter setter function
	 * @param getter getter function
	 */
	static <T> void map(final SetterFunction<T> setter, final GetterFunction<T> getter) {
		setter.set(getter.get());
	}

	/**
	 * Maps a getter to a setter.
	 *
	 * @param getter getter function
	 * @param setter setter function
	 */
	static <T> void map(final GetterFunction<T> getter, final SetterFunction<T> setter) {
		map(setter, getter);
	}

	/**
	 * Maps a setter to a getter, the setter will only be called if the value
	 * returned by the getter in non null.
	 *
	 * @param setter setter function
	 * @param getter getter function
	 */
	static <T> void mapNonNull(final SetterFunction<T> setter, final GetterFunction<T> getter) {
		map(nonNullSetter(setter), getter);
	}

	/**
	 * Maps a getter to a setter, the setter will only be called if the value
	 * returned by the getter in non null.
	 *
	 * @param getter getter function
	 * @param setter setter function
	 */
	static <T> void mapNonNull(final GetterFunction<T> getter, final SetterFunction<T> setter) {
		mapNonNull(setter, getter);
	}

}
