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

import static org.morphix.convert.Conversions.convertFrom;

import java.util.List;

import org.morphix.convert.extras.ExcludedFields;
import org.morphix.convert.extras.ExpandableFields;
import org.morphix.convert.extras.SimpleConverters;
import org.morphix.convert.function.ConvertFunction;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.InstanceFunction;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.GenericClass;
import org.morphix.reflection.GenericType;
import org.morphix.reflection.InstanceCreator;

/**
 * Converted object from source. Encapsulates all converter functionality with a simpler syntax.
 * <p>
 * For the getter methods the converter uses the 'is' convention for both <code>boolean</code> and {@link Boolean}
 * types.
 * <p>
 * TODO: implement the same functionality for Iterables and Maps
 *
 * <pre>
 * A src = new A();
 * B dst = convert(src).to(B::new);
 * </pre>
 *
 * @param <S> source type
 *
 * @author Radu Sebastian LAZIN
 */
public class Converter<S> {

	/**
	 * Source object.
	 */
	private final S source;

	/**
	 * Fields to be excluded.
	 */
	private ExcludedFields excludedFields = ExcludedFields.excludeNone();

	/**
	 * Fields to be expanded.
	 */
	private ExpandableFields expandableFields = ExpandableFields.expandAll();

	/**
	 * Simple converters object which encapsulates all given simple converters when converting objects.
	 */
	private SimpleConverters simpleConverters = SimpleConverters.empty();

	/**
	 * Extra convert function is not needed in configuration since it should only be called once per converter.
	 */
	private ConvertFunction<?, ?> extraConvertFunction = ConvertFunction.empty();

	/**
	 * Constructor.
	 *
	 * @param source conversion source object
	 */
	public Converter(final S source) {
		this.source = source;
	}

	/**
	 * Convenience static method to build converted objects and to streamline the converter syntax.
	 *
	 * @param <S> source type
	 *
	 * @param source conversion source object
	 * @return new converted object
	 */
	public static <S> Converter<S> convert(final S source) {
		return new Converter<>(source);
	}

	/**
	 * Destination specifier method.
	 *
	 * @param <D> destination type
	 *
	 * @param instanceFunction destination instance function
	 * @return conversion destination object
	 */
	public <D> D to(final InstanceFunction<D> instanceFunction) {
		return to(instanceFunction, JavaObjects.cast(extraConvertFunction));
	}

	/**
	 * Destination specifier method.
	 *
	 * @param <D> destination type
	 *
	 * @param instance destination instance object
	 * @return conversion destination object
	 */
	public <D> D to(final D instance) {
		return to(InstanceFunction.to(instance));
	}

	/**
	 * Destination specifier method.
	 *
	 * @param <D> destination type
	 *
	 * @param cls destination object class
	 * @return conversion destination object
	 */
	public <D> D to(final Class<D> cls) {
		return to(() -> Constructors.IgnoreAccess.newInstance(cls, InstanceCreator.getInstance()));
	}

	/**
	 * Destination specifier method.
	 *
	 * @param <D> destination type
	 *
	 * @param genericClass a new {@link GenericClass} object which specifies the actual generic class
	 * @return conversion destination object
	 */
	public <D> D to(final GenericClass<D> genericClass) {
		Configuration configuration = Configuration.of(excludedFields, expandableFields, simpleConverters, genericClass.getType());
		return convertFrom(source, genericClass.getGenericArgumentType(),
				JavaObjects.cast(extraConvertFunction), configuration);
	}

	/**
	 * Destination specifier method.
	 *
	 * @param <D> destination type
	 *
	 * @param genericType a new {@link GenericType} object which specifies the actual generic type
	 * @return conversion destination object
	 */
	public <D> D to(final GenericType genericType) {
		return to(GenericClass.of(genericType));
	}

	/**
	 * Destination specifier method. This method is just a convenience method so that there is no need to specify types for
	 * the {@link ConvertFunction}.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * class A {
	 * }
	 *
	 * class B {
	 * }
	 *
	 * A a = new A();
	 *
	 * // now two equivalent constructs:
	 *
	 * B b1 = convert(a)
	 * 		.with((A s, B d) -> {
	 * 			// ...
	 * 		})
	 * 		.to(B::new);
	 *
	 * // this method can be used without specifying the types
	 * // because the compiler will automatically infer the types
	 *
	 * B b2 = convert(a)
	 * 		.to(B::new, (s, d) -> {
	 * 			// ...
	 * 		});
	 *
	 * </pre>
	 *
	 * @param <D> destination type
	 *
	 * @param instanceFunction destination instance function
	 * @param extraConvertFunction extra convert function
	 * @return conversion destination object
	 */
	public <D> D to(final InstanceFunction<D> instanceFunction, final ConvertFunction<S, D> extraConvertFunction) {
		Configuration configuration = Configuration.of(excludedFields, expandableFields, simpleConverters);
		return convertFrom(source, instanceFunction, extraConvertFunction, configuration);
	}

	/**
	 * Conversion configuration method to configure excluded fields. You can use the {@link ExcludedFields#excludeAll()} or
	 * {@link ExcludedFields#exclude(String...)} shortcut methods.
	 *
	 * @see ExcludedFields
	 *
	 * @param excludedFields fields to be excluded
	 * @return this
	 */
	public Converter<S> with(final ExcludedFields excludedFields) {
		this.excludedFields = excludedFields;
		return this;
	}

	/**
	 * Conversion configuration method to configure expandable fields. You can use the {@link ExpandableFields#expandAll()}
	 * or {@link ExpandableFields#expandNone()} shortcut methods.
	 *
	 * @see ExpandableFields
	 *
	 * @param expandableFields fields to be expanded
	 * @return this
	 */
	public Converter<S> with(final ExpandableFields expandableFields) {
		this.expandableFields = expandableFields;
		return this;
	}

	/**
	 * Conversion configuration method to configure expandable fields.
	 *
	 * @see ExpandableFields
	 *
	 * @param expandableFieldNames list of fields to be expanded
	 * @return this
	 */
	public Converter<S> with(final List<String> expandableFieldNames) {
		this.expandableFields = ExpandableFields.of(expandableFieldNames);
		return this;
	}

	/**
	 * Conversion configuration method to configure expandable fields.
	 *
	 * @see ExpandableFields
	 *
	 * @param expandableFieldNames array of fields to be expanded
	 * @return this
	 */
	public Converter<S> with(final String... expandableFieldNames) {
		return with(ExpandableFields.of(expandableFieldNames));
	}

	/**
	 * Conversion configuration method to configure excluded fields.
	 *
	 * @see ExcludedFields
	 *
	 * @param excludedFieldNames array of fields to be excluded
	 * @return this
	 */
	public Converter<S> exclude(final String... excludedFieldNames) {
		return with(ExcludedFields.of(excludedFieldNames));
	}

	/**
	 * Conversion configuration method to add simple converters.
	 *
	 * @see SimpleConverters
	 *
	 * @param <T> simple converter input type
	 * @param <R> simple converter output type
	 *
	 * @param convertMethod conversion method for inner types
	 * @return this
	 */
	public <T, R> Converter<S> with(final SimpleConverter<T, R> convertMethod) {
		this.simpleConverters = SimpleConverters.of(convertMethod, simpleConverters);
		return this;
	}

	/**
	 * Conversion configuration method to specify extra convert function which will be called after the converter made all
	 * conversions. You can call this method multiple times and the functions will be composed.
	 *
	 * @param <D> destination type
	 *
	 * @param extraConvertFunction function for fields that cannot be automatically converted, will always be called after
	 *     the converter finished all conversions
	 * @return this
	 */
	public <D> Converter<S> with(final ConvertFunction<S, D> extraConvertFunction) {
		this.extraConvertFunction = extraConvertFunction.compose(JavaObjects.cast(this.extraConvertFunction));
		return this;
	}

}
