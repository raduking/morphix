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

import static org.morphix.convert.FieldHandlerResult.BREAK;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.predicates.ClassPredicates.isClass;
import static org.morphix.reflection.predicates.TypePredicates.isAClassAnd;
import static org.morphix.reflection.predicates.TypePredicates.isParameterizedType;
import static org.morphix.reflection.predicates.TypePredicates.rawType;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Predicate;

import org.morphix.convert.Conversions;
import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Handles any to {@link Optional} fields conversion.
 *
 * @author Radu Sebastian LAZIN
 */
public final class AnyToOptional extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public AnyToOptional() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			dfo.setFieldValue(Optional.empty());
			return BREAK;
		}
		Type optionalType = OptionalToAny.getOptionalType(dfo);
		if (null == optionalType) {
			dfo.setFieldValue(Optional.empty());
			return BREAK;
		}

		Object dValue = Conversions.convertFrom(sValue, optionalType, getConfiguration());
		dfo.setFieldValue(Optional.of(dValue));
		return CONVERTED;
	}

	/**
	 * @see FieldHandler#destinationTypeConstraint()
	 */
	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return PredicateHolder.DESTINATION_TYPE_CONSTRAINT;
	}

	/**
	 * Holder for predicates to avoid unnecessary class loading of the predicates when the handler is not used.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class PredicateHolder {

		/**
		 * Destination type constraint for this handler.
		 */
		private static final Predicate<Type> DESTINATION_TYPE_CONSTRAINT = isAClassAnd(isClass(Optional.class))
				.or(isParameterizedType().and(rawType(isAClassAnd(isClass(Optional.class)))));
	}
}
