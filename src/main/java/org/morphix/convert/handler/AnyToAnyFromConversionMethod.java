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
package org.morphix.convert.handler;

import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.convert.FieldHandlerResult.SKIP;

import org.morphix.convert.Configuration;
import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.Lambdas;

/**
 * Handles any object which can be converted with the conversion method supplied.
 *
 * @param <S> source type
 * @param <D> destination type
 *
 * @author Radu Sebastian LAZIN
 */
public final class AnyToAnyFromConversionMethod<S, D> extends FieldHandler {

	/**
	 * Constructor for custom configuration.
	 *
	 * @param configuration configuration object
	 */
	public AnyToAnyFromConversionMethod(final Configuration configuration) {
		super(configuration);
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		S sValue = JavaObjects.cast(sfo.getFieldValue());
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

	/**
	 * @see FieldHandler#condition(ExtendedField, ExtendedField)
	 */
	@Override
	public boolean condition(final ExtendedField sfo, final ExtendedField dfo) {
		return getConfiguration().getSimpleConverters().hasConverters();
	}

	/**
	 * Returns a {@link SimpleConverter} if with the given parameter type, null otherwise.
	 *
	 * @return a {@link SimpleConverter} if with the given parameter type, null otherwise
	 */
	private SimpleConverter<S, D> getSimpleConverter(final Class<?> paramType, final Class<?> returnType) {
		for (SimpleConverter<?, ?> converter : getConfiguration().getSimpleConverters()) {
			if (Lambdas.isLambdaWithParams(converter, returnType, paramType)) {
				return JavaObjects.cast(converter);
			}
		}
		return null;
	}
}
