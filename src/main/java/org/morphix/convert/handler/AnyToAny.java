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
import static org.morphix.convert.FieldHandlerResult.SKIP;
import static org.morphix.lang.function.InstanceFunction.to;
import static org.morphix.lang.function.Predicates.allOf;
import static org.morphix.lang.function.Predicates.not;
import static org.morphix.reflection.predicates.TypePredicates.isArray;
import static org.morphix.reflection.predicates.TypePredicates.isCharSequence;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;
import static org.morphix.reflection.predicates.TypePredicates.isMap;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.convert.Configuration;
import org.morphix.convert.Conversions;
import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Handles any object to any conversion. It calls {@link Conversions#convertFrom(Object, Class)} on the source field to
 * convert it to a destination field based on the destination class.
 * <p>
 * This handler will skip {@link Iterable}s, {@link Map}s from both source and destination and {@link CharSequence}s
 * from source.
 *
 * @author Radu Sebastian LAZIN
 */
public final class AnyToAny extends FieldHandler {

	/**
	 * Predicate list for conditions that must be false for both input classes source or destination for this handler to
	 * begin handling the objects.
	 */
	private static final Predicate<Type> HANDLER_CONSTRAINT = allOf(
			not(isIterable()),
			not(isMap()),
			not(isArray()));

	/**
	 * Default constructor.
	 */
	public AnyToAny() {
		// empty
	}

	/**
	 * Constructor for custom configuration.
	 *
	 * @param configuration configuration object
	 */
	public AnyToAny(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return SKIP;
		}
		Object dValue = dfo.getFieldValue();
		dValue = null != dValue
				? convertFrom(sValue, to(dValue), getConfiguration())
				: convertFrom(sValue, dfo.toClass(), getConfiguration());
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return not(isCharSequence())
				.and(HANDLER_CONSTRAINT);
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return HANDLER_CONSTRAINT;
	}

}
