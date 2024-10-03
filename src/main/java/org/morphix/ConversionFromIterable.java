package org.morphix;

import static org.morphix.Conversion.convertFrom;

import java.util.Collection;
import java.util.List;

import org.morphix.annotation.Expandable;
import org.morphix.extra.ExpandableFields;
import org.morphix.extra.SimpleConverters;
import org.morphix.function.ConverterWithInstance;
import org.morphix.function.ExtraConvertFunction;
import org.morphix.function.InstanceFunction;
import org.morphix.function.SimpleConverter;

/**
 * Extended conversions that can specify {@link Iterable} return types.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ConversionFromIterable {

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * any {@link Collection} using an instance function for creating instances
	 * for each element.
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
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the
	 *            destination
	 * @param configuration converter configuration
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionResult<S, D> convertIterable(
			final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction,
			final ExtraConvertFunction<S, D> extraConvertFunction,
			final Configuration configuration) {
		return new IterableConversionResult<>(sourceIterable,
				(final S source) -> convertFrom(source, elementInstanceFunction, extraConvertFunction, configuration));
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * any {@link Collection} using an instance function for creating instances
	 * for each element.
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the
	 *            destination
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionResult<S, D> convertIterable(final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction) {
		return convertIterable(sourceIterable, elementInstanceFunction, ExtraConvertFunction.empty(),
				Configuration.defaultConfiguration());
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * any {@link Collection} using an instance function for creating instances
	 * for each element.
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the
	 *            destination
	 * @param convertMethod external conversion method
	 * @return list of converted destination objects
	 */
	static <S, D, S1, D1> IterableConversionResult<S, D> convertIterable(final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction,
			final SimpleConverter<S1, D1> convertMethod) {
		return convertIterable(sourceIterable, elementInstanceFunction, ExtraConvertFunction.empty(),
				Configuration.of(ExpandableFields.expandAll(), SimpleConverters.of(convertMethod)));
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * any {@link Collection} using an instance function for creating instances
	 * for each element.
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the
	 *            destination
	 * @param simpleConverters external conversion method
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionResult<S, D> convertIterable(final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction,
			final SimpleConverters simpleConverters) {
		return convertIterable(sourceIterable, elementInstanceFunction, ExtraConvertFunction.empty(),
				Configuration.of(ExpandableFields.expandAll(), simpleConverters));
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * any {@link Collection} using an external conversion method for each
	 * element.
	 * <p>
	 * The externalConvertFunction has the form:
	 * <p>
	 * <code>public D convert(S source) { ... }</code>
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementConverterFunction external conversion function (method)
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionResult<S, D> convertIterable(final Iterable<S> sourceIterable,
			final SimpleConverter<S, D> elementConverterFunction) {
		return new IterableConversionResult<>(sourceIterable, elementConverterFunction);
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * any {@link Collection} using an external conversion method for each
	 * element and instance function for creating instances for each element.
	 * <p>
	 * The externalConvertFunction has the form:
	 * <p>
	 * <code>public D convert(S source, InstanceFunction&lt;D&gt; instanceFunction) { ... }</code>
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param externalElementConvertFunction conversion method for each element
	 *            which has an instance as parameter
	 * @param elementInstanceFunction instance function for each element in the
	 *            destination
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionResult<S, D> convertIterable(final Iterable<S> sourceIterable,
			final ConverterWithInstance<S, D> externalElementConvertFunction, final InstanceFunction<D> elementInstanceFunction) {
		return new IterableConversionResult<>(sourceIterable,
				(final S source) -> externalElementConvertFunction.convert(source, elementInstanceFunction));
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * any {@link Collection} using an extra conversion method for each element
	 * and instance function for creating instances for each element.
	 * <p>
	 * The extraConvertFunction has the form:
	 * <p>
	 * <code>public void convert(S source, D destination) { ... }</code>
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the
	 *            destination
	 * @param extraConvertFunction extra conversion function
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionResult<S, D> convertIterable(final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction,
			final ExtraConvertFunction<S, D> extraConvertFunction) {
		return new IterableConversionResult<>(sourceIterable, (final S source) -> convertFrom(source, elementInstanceFunction, extraConvertFunction));
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * any {@link Collection} using an instance function for creating instances
	 * for each element.
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the
	 *            destination
	 * @param expandedFieldNames fields that will be expanded and have the
	 *            {@link Expandable} annotation.
	 * @return list of converted destination objects
	 */
	static <S, D> IterableConversionResult<S, D> convertIterable(final Iterable<S> sourceIterable,
			final InstanceFunction<D> elementInstanceFunction,
			final List<String> expandedFieldNames) {
		return convertIterable(sourceIterable, elementInstanceFunction, ExtraConvertFunction.empty(),
				Configuration.of(expandedFieldNames, SimpleConverters.empty()));
	}

	/**
	 * Convenience static conversion method for converting array to any
	 * {@link Collection} or array using an external conversion method for each
	 * element.
	 * <p>
	 * The externalConvertFunction has the form:
	 * <p>
	 * <code>public D convert(S source) { ... }</code>
	 *
	 * @param sourceArray an array of source objects
	 * @param elementConverterFunction external conversion function (method)
	 * @return list of converted destination objects
	 */
	static <S, D> ArrayConversionResult<S, D> convertArray(final S[] sourceArray,
			final SimpleConverter<S, D> elementConverterFunction) {
		return new ArrayConversionResult<>(sourceArray, elementConverterFunction);
	}

	/**
	 * Convenience static conversion method for converting array to any
	 * {@link Collection} using an instance function for creating instances for
	 * each element.
	 *
	 * @param sourceArray an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the
	 *            destination
	 * @return list of converted destination objects
	 */
	static <S, D> ArrayConversionResult<S, D> convertArray(final S[] sourceArray,
			final InstanceFunction<D> elementInstanceFunction) {
		return new ArrayConversionResult<>(sourceArray, (final S source) -> convertFrom(source, elementInstanceFunction));
	}

}
