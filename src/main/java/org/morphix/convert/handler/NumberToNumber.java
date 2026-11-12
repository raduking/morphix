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
import static org.morphix.lang.function.Predicates.cast;
import static org.morphix.reflection.predicates.ClassPredicates.isA;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerContext;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.PrimitiveValues;

/**
 * Handles number conversions for number classes covering type coercion for non-primitive number types.
 *
 * @author Radu Sebastian LAZIN
 */
public class NumberToNumber extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public NumberToNumber() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext ctx) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return CONVERTED;
		}

		int s = ctx.get(key("sTypeIndex", sfo, dfo));
		int d = ctx.get(key("dTypeIndex", sfo, dfo));

		SimpleConverter<Object, Object> converter = InformationHolder.CONVERTERS[s][d];
		if (null == converter) {
			return CONVERTED;
		}

		Object converted = converter.convert(sValue);
		dfo.setFieldValue(converted);

		return CONVERTED;
	}

	/**
	 * @see FieldHandler#sourceTypeConstraint()
	 */
	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return InformationHolder.NUMBER_TYPE_CONSTRAINT;
	}

	/**
	 * @see FieldHandler#destinationTypeConstraint()
	 */
	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return InformationHolder.NUMBER_TYPE_CONSTRAINT;
	}

	/**
	 * @see FieldHandler#condition(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	public boolean condition(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext ctx) {
		Integer s = InformationHolder.TYPE_COERTION_INDEX.get(sfo.toClass());
		if (null == s) {
			return false;
		}
		Integer d = InformationHolder.TYPE_COERTION_INDEX.get(dfo.toClass());
		if (null == d) {
			return false;
		}
		ctx.put(key("sTypeIndex", sfo, dfo), s);
		ctx.put(key("dTypeIndex", sfo, dfo), d);
		return true;
	}

	/**
	 * Holder for static data to avoid unnecessary class loading of the predicates when the handler is not used.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InformationHolder {

		/**
		 * Type constraint for number to number handler.
		 */
		static final Predicate<Type> NUMBER_TYPE_CONSTRAINT = cast(isA(Number.class).or(isA(Character.class)));

		/**
		 * Type coercion index for all primitive boxed classes.
		 */
		private static final Map<Class<?>, Integer> TYPE_COERTION_INDEX = Map.of(
				Byte.class, 0,
				Character.class, 1,
				Short.class, 2,
				Integer.class, 3,
				Long.class, 4,
				Float.class, 5,
				Double.class, 6);

		/**
		 * Array of converters for all primitive boxed classes. The first index is the source type and the second index is the
		 * destination type. The value is a converter from the source type to the destination type.
		 * <p>
		 * The array is sparse and only contains converters for valid coercions.
		 */
		@SuppressWarnings("unchecked")
		private static final SimpleConverter<Object, Object>[][] CONVERTERS = new SimpleConverter[7][7];
		static {
			// Byte -> Short -> Integer -> Float -> Double
			CONVERTERS[0][2] = PrimitiveValues::toShort;
			CONVERTERS[0][3] = PrimitiveValues::toInt;
			CONVERTERS[0][4] = PrimitiveValues::toLong;
			CONVERTERS[0][5] = PrimitiveValues::toFloat;
			CONVERTERS[0][6] = PrimitiveValues::toDouble;
			// Character -> Short -> Integer -> Float -> Double
			CONVERTERS[1][3] = PrimitiveValues::toInt;
			CONVERTERS[1][4] = PrimitiveValues::toLong;
			CONVERTERS[1][5] = PrimitiveValues::toFloat;
			CONVERTERS[1][6] = PrimitiveValues::toDouble;
			// Short -> Integer -> Float -> Double
			CONVERTERS[2][3] = PrimitiveValues::toInt;
			CONVERTERS[2][4] = PrimitiveValues::toLong;
			CONVERTERS[2][5] = PrimitiveValues::toFloat;
			CONVERTERS[2][6] = PrimitiveValues::toDouble;
			// Integer -> Float -> Double
			CONVERTERS[3][4] = PrimitiveValues::toLong;
			CONVERTERS[3][5] = PrimitiveValues::toFloat;
			CONVERTERS[3][6] = PrimitiveValues::toDouble;
			// Long -> Float -> Double
			CONVERTERS[4][5] = PrimitiveValues::toFloat;
			CONVERTERS[4][6] = PrimitiveValues::toDouble;
			// Float -> Double
			CONVERTERS[5][6] = PrimitiveValues::toDouble;
		}
	}
}
