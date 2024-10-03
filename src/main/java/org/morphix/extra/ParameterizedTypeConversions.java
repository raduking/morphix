package org.morphix.extra;

import static org.morphix.Conversion.convertEnvelopedFrom;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.ConversionFromIterable.convertArray;
import static org.morphix.ConversionFromIterable.convertIterable;
import static org.morphix.ConversionFromMap.convertMap;
import static org.morphix.extra.ConverterCollections.isConvertibleIterableClass;
import static org.morphix.extra.ConverterCollections.isConvertibleIterableType;
import static org.morphix.extra.ConverterCollections.isConvertibleMapClass;
import static org.morphix.extra.ConverterCollections.newEmptyArrayInstance;
import static org.morphix.extra.ConverterCollections.newMapInstance;
import static org.morphix.function.InstanceFunction.to;
import static org.morphix.reflection.Constructors.newInstance;
import static org.morphix.reflection.predicates.ClassPredicates.isArray;
import static org.morphix.reflection.predicates.ClassPredicates.isIterable;
import static org.morphix.reflection.predicates.ClassPredicates.isMap;
import static org.morphix.reflection.predicates.TypePredicates.isA;
import static org.morphix.reflection.predicates.TypePredicates.isParameterizedType;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.Configuration;
import org.morphix.function.ExtraConvertFunction;
import org.morphix.reflection.Fields;
import org.morphix.reflection.InstanceCreator;
import org.morphix.reflection.ParameterizedTypeImpl;
import org.morphix.reflection.Reflection;
import org.morphix.reflection.Types;
import org.morphix.reflection.predicates.MemberPredicates;
import org.morphix.reflection.predicates.Predicates;

/**
 * Utility class for conversions from {@link Type}s.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ParameterizedTypeConversions {

	/**
	 * Generic parameterized type conversion functional interface.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@FunctionalInterface
	public interface ParameterizedConvertMethod {
		/**
		 * Converts from a source to a parameterized class given its actual type
		 * arguments.
		 *
		 * @param <S> source type
		 * @param <D> destination type
		 * @param source source object
		 * @param parameterizedClass parameterized class
		 * @param actualTypeArguments actual types
		 * @param configuration converter configuration
		 * @return destination object
		 */
		<S, D> D convertFrom(S source, ParameterizedType parameterizedType, Type[] actualTypeArguments, Configuration configuration);
	}

	/**
	 * Constraint for the destination type for known conversions like lists,
	 * maps, arrays, sets, queues, etc. These conversions are defined by
	 * convention in generic converter.
	 */
	private static final Predicate<Class<?>> KNOWN_CONVERSION_DESTINATION_CONSTRAINT =
			isConvertibleIterableClass()
					.or(isConvertibleMapClass())
					.or(isArray());

	/**
	 * Map that represents the known parameterized type conversion methods for
	 * which we do not want actual conversions for example {@link Iterable}s and {@link Map}s.
	 */
	private static final Map<Predicate<Class<?>>, ParameterizedConvertMethod> KNOWN_CONVERSION_METHODS_MAP = new HashMap<>();
	static {
		KNOWN_CONVERSION_METHODS_MAP.put(isIterable(), ParameterizedTypeConversions::convertFromIterable);
		KNOWN_CONVERSION_METHODS_MAP.put(isMap(), ParameterizedTypeConversions::convertFromMap);
		KNOWN_CONVERSION_METHODS_MAP.put(isArray(), ParameterizedTypeConversions::convertFromArray);
	}

	/**
	 * Private constructor.
	 */
	private ParameterizedTypeConversions() {
		throw new UnsupportedOperationException("This class shouldn't be instantiated.");
	}

	/**
	 * Converts from a source to a {@link ParameterizedType} destination.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 * @param source source object
	 * @param parameterizedType parameterized type
	 * @param configuration configuration
	 * @return destination object
	 */
	public static <S, D> D convertFromToParameterizedType(final S source, final ParameterizedType parameterizedType,
			final Configuration configuration) {
		return convertFromToParameterizedType(source, parameterizedType, ExtraConvertFunction.empty(), configuration);
	}

	/**
	 * Converts from a source to a {@link ParameterizedType} destination. This
	 * method first builds a map which links every generic type name with its
	 * actual type argument, then it iterates through the destination objects'
	 * fields and whenever it finds the generic type it will instantiate the
	 * actual type found in the constructed map, or it will convert it directly
	 * if the type is present in the {@link #KNOWN_CONVERSION_METHODS_MAP} for
	 * Iterables and Maps.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 * @param source source object
	 * @param parameterizedType destination parameterized type
	 * @param extraConvertFunction extra convert function
	 * @param configuration configuration
	 * @return destination object
	 */
	@SuppressWarnings("unchecked")
	public static <S, D> D convertFromToParameterizedType(final S source, final ParameterizedType parameterizedType,
			final ExtraConvertFunction<S, D> extraConvertFunction,
			final Configuration configuration) {
		Class<D> dClass = (Class<D>) parameterizedType.getRawType();
		ParameterizedConvertMethod knownConvertMethod = getParameterizedConvertMethod(source.getClass(), dClass);
		if (null != knownConvertMethod) {
			Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
			return knownConvertMethod.convertFrom(source, parameterizedType, actualTypeArguments, configuration);
		}

		D instance = newDestinationInstance(source, parameterizedType, configuration);
		return convertFrom(source, to(instance), extraConvertFunction, configuration);
	}

	/**
	 * Creates a new destination object with all generic fields instantiated.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 * @param source source object
	 * @param parameterizedType parameterized type of the destination
	 * @param configuration configuration
	 * @return a new destination object with all generic fields instantiated
	 */
	@SuppressWarnings("unchecked")
	private static <D, S> D newDestinationInstance(final S source, final ParameterizedType parameterizedType,
			final Configuration configuration) {
		Class<D> dClass = (Class<D>) parameterizedType.getRawType();

		updateGenericTypesMap(configuration, parameterizedType);
		D destination = newInstance(dClass, InstanceCreator.getInstance());

		for (Field dField : Fields.getDeclaredFields(dClass, Predicates.not(MemberPredicates.isStatic()))) {
			String typeName = dField.getGenericType().getTypeName();
			Type actualFieldType = configuration.getGenericType(typeName);
			if (null == actualFieldType) {
				actualFieldType = dField.getType();
				if (isConvertibleIterableType().test(actualFieldType)) {
					Type genericArgumentType = Reflection.getGenericArgumentType(dField, dClass, 0);
					Type[] actualTypeArguments = new Type[] { configuration.getGenericType(genericArgumentType.getTypeName()) };
					actualFieldType = ParameterizedTypeImpl.make((Class<?>) actualFieldType, actualTypeArguments, null);
				}
			}
			if (null != actualFieldType) {
				Object dValue = newDestinationFieldValue(source, dField, actualFieldType, configuration);
				if (null != dValue) {
					Reflection.setFieldValue(destination, dField, dValue);
				}
			}
		}
		return destination;
	}

	/**
	 * Handles a generic field.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 * @param source source object
	 * @param actualFieldType actual (possibly parameterized) type of the
	 *            destination
	 * @param configuration configuration
	 */
	private static <D, S> D newDestinationFieldValue(final S source, final Field dField, final Type actualFieldType,
			final Configuration configuration) {
		Class<?> sClass = source.getClass();
		Field sField = Fields.getDeclaredFieldInHierarchy(sClass, dField.getName());
		if (null == sField) {
			return null;
		}
		Object sValue = Reflection.getFieldValue(source, sField);
		if (null == sValue) {
			return null;
		}
		return newTypeInstance(sValue, actualFieldType, configuration);
	}

	/**
	 * Returns the parameterized convert method for known conversions like maps,
	 * {@link Iterable}s, arrays, etc.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 * @param sClass source class
	 * @param dClass destination class
	 * @return a parameterized conversion method
	 */
	private static <S, D> ParameterizedConvertMethod getParameterizedConvertMethod(final Class<S> sClass, final Class<D> dClass) {
		if (KNOWN_CONVERSION_DESTINATION_CONSTRAINT.test(dClass)) {
			for (Map.Entry<Predicate<Class<?>>, ParameterizedConvertMethod> sEntry : KNOWN_CONVERSION_METHODS_MAP.entrySet()) {
				if (sEntry.getKey().test(sClass)) {
					return sEntry.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Creates a new instance from the given type. It uses instance creator if
	 * it can't create an instance normally. It also automatically converts
	 * parameterized types in case we have known types for which we do not want
	 * actual conversions as for {@link Iterable}s and {@link Map}s.
	 * <p>
	 * Note:<br>
	 * This method internally calls the
	 * {@link #convertFromToParameterizedType(Object, ParameterizedType, Configuration)}
	 * because it has to convert types for which we have known conversions like
	 * lists, maps, etc. with generic types. The {@link Iterable}s converted through this
	 * method will not have an element type when the normal conversion in that
	 * method takes place at the end.<br>
	 * - for simple arrays it will create empty instances so that the converter
	 * can convert them based on the component type.<br>
	 * - for generic arrays it will convert the arrays directly
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 * @param source source object
	 * @param destinationType type to create an instance for
	 * @param configuration converter configuration object
	 * @return new instance
	 */
	@SuppressWarnings("unchecked")
	public static <D, S> D newTypeInstance(final S source, final Type destinationType, final Configuration configuration) {
		if (isParameterizedType().test(destinationType)) {
			return convertFromToParameterizedType(source, (ParameterizedType) destinationType, configuration);
		}
		if (isA(GenericArrayType.class).test(destinationType)) {
			Type componentType = ((GenericArrayType) destinationType).getGenericComponentType();
			if (isIterable().test(source.getClass())) {
				Class<?> arrayClass = Types.getArrayClass((ParameterizedType) componentType);
				return (D) convertIterable((Iterable<?>) source,
						src -> convertFrom(src, componentType, configuration))
								.toAny(arrayClass);
			}
			// TODO: check if we construct the array with the generic type component
		} else if (isA(Class.class).test(destinationType)) {
			Class<D> dClass = (Class<D>) destinationType;
			if (isArray().test(dClass)) {
				return (D) newEmptyArrayInstance(dClass.getComponentType());
			}
			return newInstance(dClass, InstanceCreator.getInstance());
		}
		return null;
	}

	/**
	 * Updates the generic types map with the generic type name and the actual type associated
	 * for the given parameterized type.
	 *
	 * @param configuration conversion configuration
	 * @param parameterizedType parameterized type
	 * @return a map with the generic type name and the actual type associated
	 */
	private static void updateGenericTypesMap(final Configuration configuration, final ParameterizedType parameterizedType) {
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		Class<?> destinationClass = (Class<?>) parameterizedType.getRawType();

		for (int i = 0; i < actualTypeArguments.length; ++i) {
			Type actualType = actualTypeArguments[i];
			String genericTypeName = destinationClass.getTypeParameters()[i].getTypeName();
			configuration.putGenericType(genericTypeName, actualType);

			if (isA(Class.class).test(actualType)) {
				// only add the mapping for array of generic type if the actualType is a Class
				configuration.putGenericType(genericTypeName + "[]", Types.getArrayClass(actualType));
			}
		}
	}

	/**
	 * Converts from an {@link Iterable} to a destination type.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 * @param source source object
	 * @param destinationClass destination {@link Iterable} class
	 * @param actualTypes array of parameterized types for list
	 * @param configuration configuration
	 * @return destination object
	 */
	public static <S, D> D convertFromIterable(final S source, final ParameterizedType destinationType,
			final Type[] actualTypes, final Configuration configuration) {
		return convertIterable((Iterable<?>) source,
				src -> convertEnvelopedFrom(src, actualTypes[0], configuration))
						.toAny(destinationType);
	}

	/**
	 * Converts from a map to a destination type.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 * @param source source object
	 * @param destinationClass destination map class
	 * @param actualTypes array of parameterized types for map
	 * @param configuration configuration
	 * @return destination object
	 */
	public static <S, D> D convertFromMap(final S source, final ParameterizedType destinationType,
			final Type[] actualTypes, final Configuration configuration) {
		return convertMap((Map<?, ?>) source,
				srcKey -> convertEnvelopedFrom(srcKey, actualTypes[0], configuration),
				srcValue -> convertEnvelopedFrom(srcValue, actualTypes[1], configuration))
						.to(newMapInstance(destinationType));
	}

	/**
	 * Converts from an array to a destination type.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 * @param source source object
	 * @param destinationClass destination class
	 * @param actualTypes array of parameterized types for list
	 * @param configuration configuration
	 * @return destination object
	 */
	public static <S, D> D convertFromArray(final S source, final ParameterizedType destinationType,
			final Type[] actualTypes, final Configuration configuration) {
		return convertArray((Object[]) source,
				src -> convertEnvelopedFrom(src, actualTypes[0], configuration))
						.toAny(destinationType);
	}

}
