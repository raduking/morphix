package org.morphix;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.List;

import org.morphix.function.ConverterWithInstance;
import org.morphix.function.ExtraConvertFunction;
import org.morphix.function.InstanceFunction;
import org.morphix.function.SimpleConverter;
import org.morphix.reflection.ConverterField;
import org.morphix.strategy.Strategy;

/**
 * Converter class that will try to convert an object of type S (source) to an
 * object of type D (destination).
 *
 * @param <S> Source type.
 * @param <D> Destination type.
 *
 * @author Radu Sebastian LAZIN
 */
public class Converter<S, D> implements
		InstanceFunction<D>,
		SimpleConverter<S, D>,
		ConverterWithInstance<S, D>,
		ExtraConvertFunction<S, D> {

	@Serial
	private static final long serialVersionUID = 6297704893123887599L;

	/**
	 * Converter configuration.
	 */
	private final transient Configuration configuration;

	/**
	 * Default constructor.
	 */
	public Converter() {
		this(Configuration.defaultConfiguration());
	}

	/**
	 * Constructor with configuration.
	 *
	 * @param configuration converter configuration
	 */
	public Converter(final Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Return all the field handlers.
	 *
	 * @return a list of field handlers
	 */
	public List<FieldHandler> getFieldHandlers() {
		return this.configuration.getFieldHandlers();
	}

	/**
	 * Returns a list of strategies with which to find the fields in the source.
	 *
	 * @return a list of strategies with which to find the fields in the source
	 */
	public List<Strategy> getStrategies() {
		return this.configuration.getStrategies();
	}

	/**
	 * For inherited classes this method must be overridden to create
	 * destination instance.
	 *
	 * @return destination object
	 */
	@Override
	public D instance() {
		throw new ConverterException("Method 'instance' not implemented in derived class.");
	}

	/**
	 * For inherited classes override this method for extra conversions. Will
	 * always be called after default conversions.
	 *
	 * @param source source object
	 * @param destination destination object
	 */
	@Override
	public void convert(final S source, final D destination) {
		// empty, can be overridden
	}

	/**
	 * Converts the source to destination. This is the main conversion method,
	 * all other <code>convert</convert> methods rely on this method.
	 *
	 * @param source source object
	 * @param instanceFunction function which creates a destination instance.
	 * @return destination object
	 */
	@Override
	public final D convert(final S source, final InstanceFunction<D> instanceFunction) {
		requireNonNull(source, "Converter source cannot be null.");
		requireNonNull(instanceFunction, "Converter instanceFunction cannot be null.");

		final D destination = instanceFunction.instance();

		genericConvert(source, destination);
		convert(source, destination);

		return destination;
	}

	/**
	 * Converts the source to destination. Calls the instance method, this
	 * method should be used when overriding the {@link #instance()} method.
	 *
	 * @param source source object
	 * @return destination object
	 */
	@Override
	public final D convert(final S source) {
		return convert(source, this::instance);
	}

	/**
	 * Converts the source to destination.
	 *
	 * @param source source object to convert
	 * @param instanceFunction function which creates a destination instance.
	 * @param extraConvertFunction function for extra conversions, will be
	 *            called last.
	 * @return destination object
	 */
	public final D convert(final S source, final InstanceFunction<D> instanceFunction, final ExtraConvertFunction<S, D> extraConvertFunction) {
		requireNonNull(extraConvertFunction, "Converter extraConvertFunction cannot be null.");

		final D destination = convert(source, instanceFunction);
		extraConvertFunction.convert(source, destination);

		return destination;
	}

	/**
	 * Main method to convert the fields.
	 *
	 * @param source source object
	 * @param destination destination object
	 */
	public final void genericConvert(final S source, final D destination) {
		Strategy.findFields(requireNonNull(destination, "Converter destination cannot be null."), Strategy.noFilter())
				.forEach(dfo -> {
					if (dfo.hasField()) {
						String sourceFieldName = dfo.sourceFieldName(source);
						// apply field finding strategies
						for (Strategy strategy : getStrategies()) {
							ConverterField sfo = strategy.find(source, sourceFieldName);
							if (sfo.hasObject()) {
								convertField(sfo, dfo);
								break;
							}
						}
					}
				});
	}

	/**
	 * Call all handlers to set the fields. If one handler succeeds there's no
	 * need to call other. Changes can still be made in the extra conversions.
	 *
	 * @param sfo source field object pair
	 * @param dfo destination field object pair
	 */
	private void convertField(final ConverterField sfo, final ConverterField dfo) {
		try {
			for (FieldHandler handler : getFieldHandlers()) {
				if (handler.convert(sfo, dfo)) {
					break;
				}
			}
		} catch (Exception e) {
			throw new ConverterException("Error converting fields: "
					+ "\nsrc" + sfo
					+ "\ndst" + dfo, e);
		}
	}

}
