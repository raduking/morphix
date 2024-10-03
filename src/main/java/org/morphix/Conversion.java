package org.morphix;

import static org.morphix.ConversionFromIterable.convertIterable;
import static org.morphix.extra.ParameterizedTypeConversions.convertFromToParameterizedType;
import static org.morphix.function.InstanceFunction.to;
import static org.morphix.function.InstanceFunction.toEmpty;
import static org.morphix.reflection.Constructors.newInstance;
import static org.morphix.reflection.Fields.getDeclaredFieldsInHierarchy;
import static org.morphix.reflection.Fields.getIgnoreAccess;
import static org.morphix.reflection.Fields.setIgnoreAccess;
import static org.morphix.reflection.predicates.MemberPredicates.isStatic;
import static org.morphix.reflection.predicates.Predicates.allOf;
import static org.morphix.reflection.predicates.Predicates.not;
import static org.morphix.reflection.predicates.TypePredicates.isA;
import static org.morphix.reflection.predicates.TypePredicates.isArray;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import org.morphix.annotation.Expandable;
import org.morphix.extra.ExpandableFields;
import org.morphix.extra.ParameterizedTypeConversions;
import org.morphix.extra.SimpleConverters;
import org.morphix.function.ConverterWithInstance;
import org.morphix.function.ExtraConvertFunction;
import org.morphix.function.InstanceFunction;
import org.morphix.function.SimpleConverter;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.InstanceCreator;

/**
 * Utility interface for conversion static methods.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Conversion {

	/**
	 * Convenience static conversion method.
	 *
	 * @param source source object
	 * @param instanceFunction destination instance function
	 * @return destination object
	 */
	static <S, D> D convertFrom(final S source, final InstanceFunction<D> instanceFunction) {
		return convertFrom(source, instanceFunction, ExtraConvertFunction.empty(),
				Configuration.defaultConfiguration());
	}

	/**
	 * Convenience static conversion method.
	 *
	 * @param source source object
	 * @param instanceFunction destination instance function
	 * @param extraConvertFunction extra conversion function
	 * @return destination object
	 */
	static <S, D> D convertFrom(final S source, final InstanceFunction<D> instanceFunction, final ExtraConvertFunction<S, D> extraConvertFunction) {
		return convertFrom(source, instanceFunction, extraConvertFunction,
				Configuration.defaultConfiguration());
	}

	/**
	 * Convenience static conversion method which also uses a conversion method
	 * defined in an external class.
	 *
	 * @param source source object
	 * @param instanceFunction destination instance function
	 * @param convertMethod inner objects conversion function
	 * @return destination object
	 */
	static <S, D, S1, D1> D convertFrom(final S source, final InstanceFunction<D> instanceFunction, final SimpleConverter<S1, D1> convertMethod) {
		return convertFrom(source, instanceFunction, SimpleConverters.of(convertMethod));
	}

	/**
	 * Convenience static conversion method which also uses a conversion method
	 * defined in an external class.
	 *
	 * @param source source object
	 * @param instanceFunction destination instance function
	 * @param simpleConverters inner objects conversion functions
	 * @return destination object
	 */
	static <S, D> D convertFrom(final S source, final InstanceFunction<D> instanceFunction, final SimpleConverters simpleConverters) {
		return convertFrom(source, instanceFunction, ExtraConvertFunction.empty(),
				Configuration.of(ExpandableFields.expandAll(), simpleConverters));
	}

	/**
	 * Convenience static conversion method which also uses a conversion method
	 * defined in an external class.
	 * <p>
	 * The expandedFieldNames list has the following conventions:
	 * <ul>
	 * <li><code>null</code> - all fields will be expanded ({@link Expandable}
	 * annotation will be ignored)</li>
	 * <li>empty list - no fields will be expanded (all fields with
	 * {@link Expandable} annotation present will be <code>null</code>)</li>
	 * <li>non empty list - only the fields present in the list will be expanded
	 * all others will be <code>null</code></li>
	 * </ul>
	 *
	 * @param source source object
	 * @param instanceFunction destination instance function
	 * @param expandedFieldNames fields that will be expanded and have the
	 *            {@link Expandable} annotation.
	 * @return destination object
	 */
	static <S, D> D convertFrom(final S source, final InstanceFunction<D> instanceFunction, final List<String> expandedFieldNames) {
		return convertFrom(source, instanceFunction, ExtraConvertFunction.empty(),
				Configuration.of(expandedFieldNames, SimpleConverters.empty()));
	}

	/**
	 * Convenience static conversion method which also uses an extra conversion
	 * method and an instance function for creating instances
	 * <p>
	 * The expandedFieldNames list has the following conventions:
	 * <ul>
	 * <li><code>null</code> - all fields will be expanded ({@link Expandable}
	 * annotation will be ignored)</li>
	 * <li>empty list - no fields will be expanded (all fields with
	 * {@link Expandable} annotation present will be <code>null</code>)</li>
	 * <li>non empty list - only the fields present in the list will be expanded
	 * all others will be <code>null</code></li>
	 * <p>
	 * The extraConvertFunction has the form:
	 * <p>
	 * <code>public void convert(S source, D destination) { ... }</code>
	 *
	 * @param source source object
	 * @param instanceFunction destination instance function
	 * @param extraConvertFunction extra conversion function
	 * @param expandedFieldNames fields that will be expanded and have the
	 *            {@link Expandable} annotation.
	 * @return destination object
	 */
	static <S, D> D convertFrom(
			final S source,
			final InstanceFunction<D> instanceFunction,
			final ExtraConvertFunction<S, D> extraConvertFunction,
			final List<String> expandedFieldNames) {
		return convertFrom(source, instanceFunction, extraConvertFunction,
				Configuration.of(expandedFieldNames, SimpleConverters.empty()));
	}

	/**
	 * Convenience static conversion method which also uses an extra conversion
	 * method and an instance function for creating instances
	 * <p>
	 * The expandedFieldNames list has the following conventions:
	 * <ul>
	 * <li><code>null</code> - all fields will be expanded ({@link Expandable}
	 * annotation will be ignored)</li>
	 * <li>empty list - no fields will be expanded (all fields with
	 * {@link Expandable} annotation present will be <code>null</code>)</li>
	 * <li>non empty list - only the fields present in the list will be expanded
	 * all others will be <code>null</code></li>
	 * <p>
	 * The extraConvertFunction has the form:
	 * <p>
	 * <code>public void convert(S source, D destination) { ... }</code>
	 *
	 * @param source source object
	 * @param instanceFunction destination instance function
	 * @param expandedFieldNames fields that will be expanded and have the
	 *            {@link Expandable} annotation.
	 * @param convertMethod extra conversion function
	 * @return destination object
	 */
	static <S, D, S1, D1> D convertFrom(
			final S source,
			final InstanceFunction<D> instanceFunction,
			final List<String> expandedFieldNames,
			final SimpleConverter<S1, D1> convertMethod) {
		return convertFrom(source, instanceFunction, ExtraConvertFunction.empty(),
				Configuration.of(expandedFieldNames, SimpleConverters.of(convertMethod)));
	}

	/**
	 * Convenience static conversion method.
	 *
	 * @param source source object
	 * @param instanceFunction destination instance function
	 * @param extraConvertFunction extra conversion function
	 * @return destination object
	 */
	static <S, D, S1, D1> D convertFrom(
			final S source,
			final InstanceFunction<D> instanceFunction,
			final ExtraConvertFunction<S, D> extraConvertFunction,
			final SimpleConverter<S1, D1> convertMethod) {
		return convertFrom(source, instanceFunction, extraConvertFunction,
				Configuration.of(ExpandableFields.expandAll(), SimpleConverters.of(convertMethod)));
	}

	/**
	 * Convenience static conversion method with all parameters.
	 * <p>
	 * The expandedFieldNames list has the following conventions:
	 * <ul>
	 * <li><code>null</code> - all fields will be expanded ({@link Expandable}
	 * annotation will be ignored)</li>
	 * <li>empty list - no fields will be expanded (all fields with
	 * {@link Expandable} annotation present will be <code>null</code>)</li>
	 * <li>non empty list - only the fields present in the list will be expanded
	 * all others will be <code>null</code></li>
	 * <p>
	 * The extraConvertFunction has the form:
	 * <p>
	 * <code>public void convert(S source, D destination) { ... }</code>
	 *
	 * @param source source object
	 * @param instanceFunction destination instance function
	 * @param simpleConverters extra conversion function
	 * @param expandedFieldNames fields that will be expanded and have the
	 *            {@link Expandable} annotation.
	 * @return destination object
	 */
	static <S, D> D convertFrom(
			final S source,
			final InstanceFunction<D> instanceFunction,
			final ExtraConvertFunction<S, D> extraConvertFunction,
			final List<String> expandedFieldNames,
			final SimpleConverters simpleConverters) {
		return convertFrom(source, instanceFunction, extraConvertFunction,
				Configuration.of(expandedFieldNames, simpleConverters));
	}

	/**
	 * Convenience static conversion method with {@link Configuration}.
	 *
	 * @param source source object
	 * @param instanceFunction destination instance function
	 * @param configuration configuration
	 * @return destination object
	 */
	static <S, D> D convertFrom(
			final S source,
			final InstanceFunction<D> instanceFunction,
			final Configuration configuration) {
		return convertFrom(source, instanceFunction, ExtraConvertFunction.empty(), configuration);
	}

	/**
	 * Convenience static conversion method with {@link Configuration}.
	 *
	 * @param source source object
	 * @param instanceFunction destination instance function
	 * @param configuration configuration
	 * @return destination object
	 */
	static <S, D> D convertFrom(
			final S source,
			final InstanceFunction<D> instanceFunction,
			final ExtraConvertFunction<S, D> extraConvertFunction,
			final Configuration configuration) {
		Converter<S, D> converter = ConverterBuilder.newConverter(configuration);
		return converter.convert(source, instanceFunction, extraConvertFunction);

	}

	/**
	 * Convenience static conversion method that converts the source to a new
	 * object which has the type of the destination class given as parameter.
	 *
	 * @param source source object
	 * @param destinationClass destination class
	 * @return destination object
	 */
	static <S, D> D convertFrom(final S source, final Class<D> destinationClass) {
		return convertFrom(source, destinationClass, Configuration.defaultConfiguration());
	}

	/**
	 * Convenience static conversion method that converts the source to a new
	 * object which has the type of the destination class given as parameter.
	 *
	 * @param source source object
	 * @param destinationClass destination class
	 * @param convertMethod inner objects conversion function
	 * @return destination object
	 */
	static <S, D, S1, D1> D convertFrom(final S source, final Class<D> destinationClass, final SimpleConverter<S1, D1> convertMethod) {
		return convertFrom(source, destinationClass,
				Configuration.of(ExpandableFields.expandAll(), SimpleConverters.of(convertMethod)));
	}

	/**
	 * Convenience static conversion method that converts the source to a new
	 * object which has the type of the destination class given as parameter.
	 *
	 * @param source source object
	 * @param destinationClass destination class
	 * @param configuration configuration
	 * @return destination object
	 */
	static <S, D> D convertFrom(final S source, final Class<D> destinationClass, final Configuration configuration) {
		return convertFrom(source, destinationClass, ExtraConvertFunction.empty(), configuration);
	}

	/**
	 * Convenience static conversion method that converts the source to a new
	 * object which has the type of the destination class given as parameter.
	 *
	 * @param source source object
	 * @param destinationClass destination class
	 * @param extraConvertFunction extra convert function
	 * @param configuration configuration
	 * @return destination object
	 */
	static <S, D> D convertFrom(final S source, final Class<D> destinationClass,
			final ExtraConvertFunction<S, D> extraConvertFunction,
			final Configuration configuration) {
		return convertFrom(source, () -> newInstance(destinationClass, InstanceCreator.getInstance()), extraConvertFunction, configuration);
	}

	/**
	 * Convenience static conversion method that converts the source to a new
	 * object which has the type of the destination type given as parameter.
	 *
	 * @param source source object
	 * @param type destination type
	 * @return destination object
	 */
	static <S, D> D convertFrom(final S source, final Type type) {
		return convertFrom(source, type, ExtraConvertFunction.empty(), Configuration.defaultConfiguration());
	}

	/**
	 * Convenience static conversion method that converts the source to a new
	 * object which has the type of the destination type given as parameter.
	 *
	 * @param source source object
	 * @param type destination type
	 * @param configuration configuration
	 * @return destination object
	 */
	static <S, D> D convertFrom(final S source, final Type type, final Configuration configuration) {
		return convertFrom(source, type, ExtraConvertFunction.empty(), configuration);
	}

	/**
	 * Convenience static conversion method that converts the source to a new
	 * object which has the type of the destination type given as parameter.
	 *
	 * @param source source object
	 * @param type destination type
	 * @param extraConvertFunction extra convert function
	 * @param configuration configuration
	 * @return destination object
	 */
	@SuppressWarnings("unchecked")
	static <S, D> D convertFrom(final S source, final Type type,
			final ExtraConvertFunction<S, D> extraConvertFunction,
			final Configuration configuration) {
		if (isA(Class.class).test(type)) {
			// the cast to (Class<D>) is necessary to call the right method
			return convertFrom(source, (Class<D>) type, extraConvertFunction, configuration); // NOSONAR
		}
		if (isA(ParameterizedType.class).test(type)) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Configuration newConfiguration = Configuration.copyWith(parameterizedType, configuration);
			return convertFromToParameterizedType(source, parameterizedType, extraConvertFunction, newConfiguration);
		}
		throw new ConverterException("Could not convert to type: " + type);
	}

	/**
	 * Enveloped conversion.
	 *
	 * @param source source object
	 * @param type destination type
	 * @param configuration configuration
	 * @return destination object
	 */
	static <S, D> D convertEnvelopedFrom(final S source, final Type type, final Configuration configuration) {
		if (not(isA(Class.class)).test(type)) {
			if (isIterable().test(type) && allOf(not(isIterable()), not(isArray())).test(source.getClass())) {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				return ParameterizedTypeConversions.convertFromIterable(Collections.singletonList(source),
						parameterizedType, parameterizedType.getActualTypeArguments(), configuration);
			}
			return convertFrom(source, type, configuration);
		}
		D instance = ParameterizedTypeConversions.newTypeInstance(source, type, configuration);
		return convertEnvelopedFrom(source, to(instance), configuration);
	}

	/**
	 * Enveloped conversion.
	 *
	 * @param source source object
	 * @param instanceFunction destination {@link InstanceFunction}
	 * @param configuration configuration
	 * @return destination object
	 */
	static <S, D> D convertEnvelopedFrom(final S source, final InstanceFunction<D> instanceFunction, final Configuration configuration) {
		return convertFrom(new Fields() {
			@SuppressWarnings("unused")
			S converterResultEnvelope = source;
		}, to(new Fields() {
			D converterResultEnvelope = instanceFunction.instance();
		}), configuration).converterResultEnvelope;
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * {@link List} using an instance function for creating instances for each
	 * element.
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the
	 *            destination
	 * @return list of converted destination objects
	 */
	static <S, D> List<D> convertFromIterable(final Iterable<S> sourceIterable, final InstanceFunction<D> elementInstanceFunction) {
		return convertIterable(sourceIterable, elementInstanceFunction).toList();
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * {@link List} using an instance function for creating instances for each
	 * element.
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the
	 *            destination
	 * @param convertMethod external conversion method
	 * @return list of converted destination objects
	 */
	static <S, D, S1, D1> List<D> convertFromIterable(final Iterable<S> sourceIterable, final InstanceFunction<D> elementInstanceFunction,
			final SimpleConverter<S1, D1> convertMethod) {
		return convertIterable(sourceIterable, elementInstanceFunction, convertMethod).toList();
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * {@link List} using an external conversion method for each element.
	 * <p>
	 * The externalConvertFunction has the form:
	 * <p>
	 * <code>public D convert(S source) { ... }</code>
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param externalConvertFunction external conversion function (method)
	 * @return list of converted destination objects
	 */
	static <S, D> List<D> convertFromIterable(final Iterable<S> sourceIterable, final SimpleConverter<S, D> externalConvertFunction) {
		return convertIterable(sourceIterable, externalConvertFunction).toList();
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * {@link List} using an external conversion method for each element and
	 * instance function for creating instances for each element.
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
	static <S, D> List<D> convertFromIterable(final Iterable<S> sourceIterable,
			final ConverterWithInstance<S, D> externalElementConvertFunction, final InstanceFunction<D> elementInstanceFunction) {
		return convertIterable(sourceIterable, externalElementConvertFunction, elementInstanceFunction).toList();
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * {@link List} using an extra conversion method for each element and
	 * instance function for creating instances for each element.
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
	static <S, D> List<D> convertFromIterable(final Iterable<S> sourceIterable, final InstanceFunction<D> elementInstanceFunction,
			final ExtraConvertFunction<S, D> extraConvertFunction) {
		return convertIterable(sourceIterable, elementInstanceFunction, extraConvertFunction).toList();
	}

	/**
	 * Convenience static conversion method for converting {@link Iterable} to
	 * {@link List} using an instance function for creating instances for each
	 * element.
	 *
	 * @param sourceIterable an {@link Iterable} of source objects
	 * @param elementInstanceFunction instance function for each element in the
	 *            destination
	 * @param expandedFieldNames fields that will be expanded and have the
	 *            {@link Expandable} annotation.
	 * @return list of converted destination objects
	 */
	static <S, D> List<D> convertFromIterable(final Iterable<S> sourceIterable, final InstanceFunction<D> elementInstanceFunction,
			final List<String> expandedFieldNames) {
		return convertIterable(sourceIterable, elementInstanceFunction, expandedFieldNames).toList();
	}

	/**
	 * Convenience static clone method. Cloned object must have a default
	 * constructor.
	 *
	 * @param source source object
	 * @return clone of the source object
	 */
	@SuppressWarnings("unchecked")
	static <D> D cloneOf(final D source) {
		return null == source ? null : copyFrom(source, toEmpty(Constructors.newInstance((Class<D>) source.getClass())));
	}

	/**
	 * Convenience static copy fields method, ignoring null fields.
	 *
	 * @param source source object
	 * @param destinationInstanceFunction destination object
	 * @return destination object
	 */
	static <S, D> D copyFrom(final S source, final InstanceFunction<D> destinationInstanceFunction) {
		return copyFrom(source, destinationInstanceFunction, false);
	}

	/**
	 * Convenience static copy fields method.
	 *
	 * @param source source object
	 * @param destinationInstanceFunction destination object
	 * @param overrideAll indicates if it should copy null values
	 * @return destination object
	 */
	static <S, D> D copyFrom(final S source, final InstanceFunction<D> destinationInstanceFunction, final boolean overrideAll) {
		if (null == destinationInstanceFunction) {
			return null;
		}
		D destination = destinationInstanceFunction.instance();
		if (null == destination) {
			return null;
		}
		if (null == source) {
			return destination;
		}
		if (destination.getClass().equals(source.getClass())) {
			getDeclaredFieldsInHierarchy(source.getClass(), not(isStatic()))
					.forEach(field -> {
						Object value = getIgnoreAccess(source, field);
						if (overrideAll || null != value) {
							setIgnoreAccess(destination, field, value);
						}
					});
			return destination;
		}
		return convertFrom(source, to(destination));
	}

	/**
	 * Convenience interface that can be used to set fields on any object.<br>
	 * Usage:<br>
	 * <p>
	 *
	 * <pre>
	 * copyFrom(new Conversion.Fields() {
	 * 	String uuid = "SomeValue";
	 * }, to(objectWith_uuid_Field));
	 * </pre>
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface Fields {
		// empty
	}
}
