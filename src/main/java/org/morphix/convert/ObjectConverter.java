/*
 * Copyright 2025 the original author or authors.
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

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import org.morphix.convert.annotation.From;
import org.morphix.convert.annotation.Src;
import org.morphix.convert.function.ConvertFunction;
import org.morphix.convert.function.InstanceConvertFunction;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.convert.strategy.ConversionStrategy;
import org.morphix.lang.function.InstanceFunction;
import org.morphix.reflection.ExtendedField;

/**
 * Converter class that will try to convert an object of type S (source) to an object of type D (destination).
 *
 * @param <S> Source type.
 * @param <D> Destination type.
 *
 * @author Radu Sebastian LAZIN
 */
public class ObjectConverter<S, D> implements
		InstanceFunction<D>,
		InstanceConvertFunction<S, D>,
		SimpleConverter<S, D>,
		ConvertFunction<S, D> {

	/**
	 * Serial version UID, required for {@link Serializable} classes.
	 */
	@Serial
	private static final long serialVersionUID = 6376266502114978408L;

	/**
	 * Converter configuration.
	 */
	private final transient Configuration configuration;

	/**
	 * Default constructor.
	 */
	public ObjectConverter() {
		this(Configuration.defaultConfiguration());
	}

	/**
	 * Constructor with configuration.
	 *
	 * @param configuration converter configuration
	 */
	public ObjectConverter(final Configuration configuration) {
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
	public List<ConversionStrategy> getStrategies() {
		return this.configuration.getStrategies();
	}

	/**
	 * For inherited classes this method must be overridden to create destination instance.
	 *
	 * @return destination object
	 */
	@Override
	public D instance() {
		throw new ObjectConverterException("Method 'instance' not implemented in derived class.");
	}

	/**
	 * For inherited classes override this method for extra conversions. Will always be called after default conversions.
	 *
	 * @param source source object
	 * @param destination destination object
	 */
	@Override
	public void convert(final S source, final D destination) {
		// empty, can be overridden
	}

	/**
	 * Converts the source to destination. This is the main conversion method, all other <code>convert</code> methods rely
	 * on this method.
	 *
	 * @param source source object
	 * @param instanceFunction function which creates a destination instance.
	 * @return destination object
	 */
	@Override
	public final D convert(final S source, final InstanceFunction<D> instanceFunction) {
		Objects.requireNonNull(source, "Converter source cannot be null.");
		Objects.requireNonNull(instanceFunction, "Converter instanceFunction cannot be null.");

		final D destination = instanceFunction.instance();

		mainConvert(source, destination);
		convert(source, destination);

		return destination;
	}

	/**
	 * Converts the source to destination. Calls the instance method, this method should be used when overriding the
	 * {@link #instance()} method.
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
	 * @param extraConvertFunction function for extra conversions, will be called last.
	 * @return destination object
	 */
	public final D convert(final S source, final InstanceFunction<D> instanceFunction, final ConvertFunction<S, D> extraConvertFunction) {
		Objects.requireNonNull(extraConvertFunction, "Converter extraConvertFunction cannot be null.");

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
	private void mainConvert(final S source, final D destination) {
		ConversionStrategy.findFields(Objects.requireNonNull(destination, "Converter destination cannot be null."), ConversionStrategy.noFilter())
				.forEach(dfo -> {
					if (dfo.hasField()) {
						String sourceFieldName = getSourceFieldName(dfo, source);
						// apply field finding strategies
						for (ConversionStrategy strategy : getStrategies()) {
							ExtendedField sfo = strategy.find(source, sourceFieldName);
							if (sfo.hasObject()) {
								convertField(sfo, dfo);
								break;
							}
						}
					}
				});
	}

	/**
	 * Finds the source field name if the destination field is annotated.
	 *
	 * @param <T> source type
	 *
	 * @param dfo destination field object
	 * @param source source object
	 * @return source field name
	 */
	public <T> String getSourceFieldName(final ExtendedField dfo, final T source) {
		Src srcAnnotation = null;
		Method getterMethod = dfo.getGetterMethod();
		if (null != getterMethod) {
			srcAnnotation = getterMethod.getAnnotation(Src.class);
		}
		Field field = dfo.getField();
		if (null == srcAnnotation && null != field) {
			srcAnnotation = field.getAnnotation(Src.class);
		}
		String name = dfo.getName();
		if (null == srcAnnotation) {
			return name;
		}
		for (From from : srcAnnotation.from()) {
			if (Objects.equals(source.getClass(), from.type())) {
				return from.path();
			}
		}
		if (srcAnnotation.name().isEmpty() && srcAnnotation.value().isEmpty()) {
			return name;
		}
		return srcAnnotation.name().isEmpty() ? srcAnnotation.value() : srcAnnotation.name();
	}

	/**
	 * Call all handlers to set the fields. If one handler succeeds there's no need to call other. Changes can still be made
	 * in the extra conversions.
	 *
	 * @param sfo source field object pair
	 * @param dfo destination field object pair
	 */
	private void convertField(final ExtendedField sfo, final ExtendedField dfo) {
		try {
			for (FieldHandler handler : getFieldHandlers()) {
				if (handler.convert(sfo, dfo)) {
					break;
				}
			}
		} catch (Exception e) {
			throw new ObjectConverterException("Error converting fields: "
					+ "\nsrc" + sfo
					+ "\ndst" + dfo, e);
		}
	}

}
