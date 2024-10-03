package org.morphix.handler;

import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.FieldHandlerResult.SKIP;

import org.morphix.Configuration;
import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.extra.Lambdas;
import org.morphix.function.SimpleConverter;
import org.morphix.reflection.ConverterField;

/**
 * Handles any object which can be converted with the conversion method
 * supplied.
 *
 * @param <S> source type
 * @param <D> destination type
 *
 * @author Radu Sebastian LAZIN
 */
public final class AnyToAnyFromConversionMethod<S, D> extends FieldHandler {

	public AnyToAnyFromConversionMethod(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		@SuppressWarnings("unchecked")
		S sValue = (S) sfo.getFieldValue();
		if (null == sValue) {
			return SKIP;
		}
		SimpleConverter<S, D> converter = getSimpleConverter(sfo.toClass(), dfo.toClass());
		if (null == converter) {
			return SKIP;
		}

		D dValue = converter.convert(sValue);
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	@Override
	public boolean condition(final ConverterField sfo, final ConverterField dfo) {
		return getConfiguration().getSimpleConverters().hasConverters();
	}

	/**
	 * Returns a {@link SimpleConverter} if with the given parameter type, null
	 * otherwise.
	 *
	 * @return a {@link SimpleConverter} if with the given parameter type, null
	 *         otherwise
	 */
	@SuppressWarnings("unchecked")
	private SimpleConverter<S, D> getSimpleConverter(final Class<?> paramType, final Class<?> returnType) {
		for (SimpleConverter<?, ?> converter : getConfiguration().getSimpleConverters()) {
			if (Lambdas.isLambdaWithParams(converter, returnType, paramType)) {
				return (SimpleConverter<S, D>) converter;
			}
		}
		return null;
	}

}
