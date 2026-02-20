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
import org.morphix.convert.FieldHandlerResult;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.reflection.ExtendedField;

/**
 * Handles number conversions for number classes covering type coercion for non-primitive number types.
 *
 * @author Radu Sebastian LAZIN
 */
public class NumberToNumber extends FieldHandler {

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
	 * destination type. The value is a converter from the source type to the destination type. The array is sparse and only
	 * contains converters for valid coercions.
	 */
	@SuppressWarnings("unchecked")
	private static final SimpleConverter<Object, Object>[][] CONVERTERS = new SimpleConverter[7][7];
	static {
		// Byte -> Short -> Integer -> Float -> Double
		CONVERTERS[0][2] = n -> ((Byte) n).shortValue();
		CONVERTERS[0][3] = n -> ((Byte) n).intValue();
		CONVERTERS[0][4] = n -> ((Byte) n).longValue();
		CONVERTERS[0][5] = n -> ((Byte) n).floatValue();
		CONVERTERS[0][6] = n -> ((Byte) n).doubleValue();
		// Character -> Short -> Integer -> Float -> Double
		CONVERTERS[1][3] = o -> (int) ((Character) o).charValue();
		CONVERTERS[1][4] = o -> (long) ((Character) o).charValue();
		CONVERTERS[1][5] = o -> (float) ((Character) o).charValue();
		CONVERTERS[1][6] = o -> (double) ((Character) o).charValue();
		// Short -> Integer -> Float -> Double
		CONVERTERS[2][3] = o -> ((Short) o).intValue();
		CONVERTERS[2][4] = o -> ((Short) o).longValue();
		CONVERTERS[2][5] = o -> ((Short) o).floatValue();
		CONVERTERS[2][6] = o -> ((Short) o).doubleValue();
		// Integer -> Float -> Double
		CONVERTERS[3][4] = o -> ((Integer) o).longValue();
		CONVERTERS[3][5] = o -> ((Integer) o).floatValue();
		CONVERTERS[3][6] = o -> ((Integer) o).doubleValue();
		// Long -> Float -> Double
		CONVERTERS[4][5] = o -> ((Long) o).floatValue();
		CONVERTERS[4][6] = o -> ((Long) o).doubleValue();
		// Float -> Double
		CONVERTERS[5][6] = o -> ((Float) o).doubleValue();
	}

	/**
	 * Default constructor.
	 */
	public NumberToNumber() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return CONVERTED;
		}

		int s = TYPE_COERTION_INDEX.get(sfo.toClass());
		int d = TYPE_COERTION_INDEX.get(dfo.toClass());

		SimpleConverter<Object, Object> converter = CONVERTERS[s][d];
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
		return PredicateHolder.NUMBER_TYPE_CONSTRAINT;
	}

	/**
	 * @see FieldHandler#destinationTypeConstraint()
	 */
	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return PredicateHolder.NUMBER_TYPE_CONSTRAINT;
	}

	/**
	 * @see FieldHandler#condition(ExtendedField, ExtendedField)
	 */
	@Override
	public boolean condition(final ExtendedField sfo, final ExtendedField dfo) {
		Integer s = TYPE_COERTION_INDEX.get(sfo.toClass());
		Integer d = TYPE_COERTION_INDEX.get(dfo.toClass());
		return null != s && null != d;
	}

	/**
	 * Holder for predicates to avoid unnecessary class loading of the predicates when the handler is not used.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class PredicateHolder {

		/**
		 * Type constraint for number to number handler.
		 */
		static final Predicate<Type> NUMBER_TYPE_CONSTRAINT = cast(isA(Number.class).or(isA(Character.class)));
	}
}
