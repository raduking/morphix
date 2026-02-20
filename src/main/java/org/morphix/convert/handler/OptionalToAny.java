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

import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.lang.function.InstanceFunction.to;
import static org.morphix.reflection.predicates.ClassPredicates.isClass;
import static org.morphix.reflection.predicates.TypePredicates.isAClassAnd;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Predicate;

import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerContext;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Handles {@link Optional} fields conversion. This handler assumes that the source field is an {@link Optional} and it
 * will convert the value of the optional to the destination field type. If the optional is empty or null, it will set
 * the destination field to null.
 *
 * @author Radu Sebastian LAZIN
 */
public final class OptionalToAny extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public OptionalToAny() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext ctx) {
		Optional<?> optional = (Optional<?>) sfo.getFieldValue();
		if (null == optional || optional.isEmpty()) { // NOSONAR we can't control how the caller sets the field value
			return CONVERTED;
		}
		Object sValue = optional.get();
		Object dValue = dfo.getFieldValue();

		dValue = null != dValue
				? convertFrom(sValue, to(dValue), getConfiguration())
				: convertFrom(sValue, ctx.getDClass(dfo), getConfiguration());
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	/**
	 * @see FieldHandler#sourceTypeConstraint()
	 */
	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return PredicateHolder.SOURCE_TYPE_CONSTRAINT;
	}

	/**
	 * Returns the type of the value within the optional. This method assumes that the source field is an optional and it
	 * has a getter method. It uses a trick where the JRE retains the generic type information for method return types.
	 *
	 * @param fop source field
	 * @return type of the value within the optional, null if the type cannot be determined
	 */
	public static Type getOptionalType(final ExtendedField fop) {
		// TODO: add exception message to show the getter method need
		return fop.getGenericReturnType(0);
	}

	/**
	 * Holder for predicates to avoid unnecessary class loading of the predicates when the handler is not used.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class PredicateHolder {

		/**
		 * Source type constraint for this handler. It checks if the source type is a class and it is an {@link Optional}.
		 */
		private static final Predicate<Type> SOURCE_TYPE_CONSTRAINT = isAClassAnd(isClass(Optional.class));
	}
}
