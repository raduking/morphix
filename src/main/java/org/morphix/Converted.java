package org.morphix;

import static org.morphix.Conversion.convertFrom;
import static org.morphix.reflection.Constructors.newInstance;

import java.util.List;

import org.morphix.extra.ExcludedFields;
import org.morphix.extra.ExpandableFields;
import org.morphix.extra.SimpleConverters;
import org.morphix.function.ExtraConvertFunction;
import org.morphix.function.InstanceFunction;
import org.morphix.function.SimpleConverter;
import org.morphix.reflection.InstanceCreator;
import org.morphix.reflection.ParameterizedClass;

/**
 * Converted object from source. Encapsulates all generic converter
 * functionality with a simpler syntax.
 * <p>
 * TODO: implement the same functionality for Iterables and Maps
 *
 * <pre>
 * A src = new A();
 * B dst = convert(src).to(B::new);
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
public class Converted<S> {

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
	 * Simple converters object which encapsulates all given simple converters
	 * when converting objects.
	 */
	private SimpleConverters simpleConverters = SimpleConverters.empty();

	/**
	 * Extra convert function is not needed in configuration since it should
	 * only be called once per converter.
	 */
	private ExtraConvertFunction<?, ?> extraConvertFunction = ExtraConvertFunction.empty();

	/**
	 * Constructor.
	 *
	 * @param source conversion source object
	 */
	public Converted(final S source) {
		this.source = source;
	}

	/**
	 * Convenience static method to build converted objects and to streamline
	 * the converter syntax.
	 *
	 * @param source conversion source object
	 * @return new converted object
	 */
	public static <S> Converted<S> convert(final S source) {
		return new Converted<>(source);
	}

	/**
	 * Destination specifier method.
	 *
	 * @param instanceFunction destination instance function
	 * @return conversion destination object
	 */
	@SuppressWarnings("unchecked")
	public <D> D to(final InstanceFunction<D> instanceFunction) {
		return to(instanceFunction, (ExtraConvertFunction<S, D>) extraConvertFunction);
	}

	/**
	 * Destination specifier method.
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
	 * @param cls destination object class
	 * @return conversion destination object
	 */
	public <D> D to(final Class<D> cls) {
		return to(() -> newInstance(cls, InstanceCreator.getInstance()));
	}

	/**
	 * Destination specifier method.
	 *
	 * @param parameterizedClassObject a new {@link ParameterizedClass} object
	 *            which specifies the actual generic class
	 * @return conversion destination object
	 */
	@SuppressWarnings("unchecked")
	public <D> D to(final ParameterizedClass<D> parameterizedClassObject) {
		Configuration configuration = Configuration.of(excludedFields, expandableFields, simpleConverters, parameterizedClassObject.getClass());
		return convertFrom(source, parameterizedClassObject.getGenericArgumentType(),
				(ExtraConvertFunction<S, D>) extraConvertFunction, configuration);
	}

	/**
	 * Destination specifier method. This method is just a convenience method so
	 * that there is no need to specify types for the
	 * {@link ExtraConvertFunction}.
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
	 * @param instanceFunction destination instance function
	 * @param extraConvertFunction extra convert function
	 * @return conversion destination object
	 */
	public <D> D to(final InstanceFunction<D> instanceFunction, final ExtraConvertFunction<S, D> extraConvertFunction) {
		Configuration configuration = Configuration.of(excludedFields, expandableFields, simpleConverters);
		return convertFrom(source, instanceFunction, extraConvertFunction, configuration);
	}

	/**
	 * Conversion configuration method to configure excluded fields. You can use
	 * the {@link ExcludedFields#excludeAll()} or
	 * {@link ExcludedFields#exclude(String...)} shortcut methods.
	 *
	 * @see ExcludedFields {@link ExcludedFields}
	 *
	 * @param excludedFields fields to be excluded
	 * @return this
	 */
	public Converted<S> with(final ExcludedFields excludedFields) {
		this.excludedFields = excludedFields;
		return this;
	}

	/**
	 * Conversion configuration method to configure expandable fields. You can
	 * use the {@link ExpandableFields#expandAll()} or
	 * {@link ExpandableFields#expandNone()} shortcut methods.
	 *
	 * @see ExpandableFields {@link ExpandableFields}
	 *
	 * @param expandableFields fields to be expanded
	 * @return this
	 */
	public Converted<S> with(final ExpandableFields expandableFields) {
		this.expandableFields = expandableFields;
		return this;
	}

	/**
	 * Conversion configuration method to configure expandable fields
	 *
	 * @see ExpandableFields {@link ExpandableFields}
	 *
	 * @param expandableFieldNames list of fields to be expanded
	 * @return this
	 */
	public Converted<S> with(final List<String> expandableFieldNames) {
		this.expandableFields = ExpandableFields.of(expandableFieldNames);
		return this;
	}

	/**
	 * Conversion configuration method to configure expandable fields.
	 *
	 * @see ExpandableFields {@link ExpandableFields}
	 *
	 * @param expandableFieldNames array of fields to be expanded
	 * @return this
	 */
	public Converted<S> with(final String... expandableFieldNames) {
		return with(ExpandableFields.of(expandableFieldNames));
	}

	/**
	 * Conversion configuration method to configure excluded fields.
	 *
	 * @see ExcludedFields {@link ExcludedFields}
	 *
	 * @param excludedFieldNames array of fields to be excluded
	 * @return this
	 */
	public Converted<S> exclude(final String... excludedFieldNames) {
		return with(ExcludedFields.of(excludedFieldNames));
	}

	/**
	 * Conversion configuration method to add simple converters.
	 *
	 * @see SimpleConverters {@link SimpleConverters}
	 *
	 * @param <T> simple converter input type
	 * @param <R> simple converter output type
	 *
	 * @param convertMethod conversion method for inner types
	 * @return this
	 */
	public <T, R> Converted<S> with(final SimpleConverter<T, R> convertMethod) {
		this.simpleConverters = SimpleConverters.of(convertMethod, simpleConverters);
		return this;
	}

	/**
	 * Conversion configuration method to specify extra convert function which
	 * will be called after the converter made all conversions. You can call
	 * this method multiple times and the functions will be composed.
	 *
	 * @param <D> destination type
	 *
	 * @param extraConvertFunction function for fields that cannot be
	 *            automatically converted, will always be called after the
	 *            converter finished all conversions
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public <D> Converted<S> with(final ExtraConvertFunction<S, D> extraConvertFunction) {
		this.extraConvertFunction = extraConvertFunction
				.compose((ExtraConvertFunction<S, D>) this.extraConvertFunction);
		return this;
	}

}
