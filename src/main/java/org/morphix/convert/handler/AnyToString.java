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
import static org.morphix.reflection.predicates.ClassPredicates.isA;
import static org.morphix.reflection.predicates.TypePredicates.isAClassAnd;

import java.lang.reflect.Type;
import java.util.function.Predicate;

import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Handles assignment when the destination is {@link String} and calls {@link #toString()} on the source object's field.
 * It skips {@code null} values.
 *
 * @author Radu Sebastian LAZIN
 */
public final class AnyToString extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public AnyToString() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null != sValue) {
			dfo.setFieldValue(sValue.toString());
		}
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
		private static final Predicate<Type> DESTINATION_TYPE_CONSTRAINT = isAClassAnd(isA(String.class));
	}
}
