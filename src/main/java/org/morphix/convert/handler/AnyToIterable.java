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

import static org.morphix.convert.Conversions.convertEnvelopedFrom;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.convert.FieldHandlerResult.HANDLED;
import static org.morphix.convert.FieldHandlerResult.SKIPPED;
import static org.morphix.convert.IterableConversions.convertIterable;
import static org.morphix.convert.extras.ConverterCollections.newCollectionInstance;
import static org.morphix.lang.function.Predicates.allOf;
import static org.morphix.lang.function.Predicates.not;
import static org.morphix.reflection.predicates.TypePredicates.isArray;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;
import static org.morphix.reflection.predicates.TypePredicates.isMap;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.convert.Conversions;
import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerContext;
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
public final class AnyToIterable extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public AnyToIterable() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext ctx) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return SKIPPED;
		}
		Type elementType = IterableToIterable.getIterableElementType(dfo);
		if (null == elementType) {
			return HANDLED;
		}
		Iterable<?> sIterable = Collections.singletonList(sValue);
		Iterable<?> dValue = convertIterable(sIterable,
				src -> convertEnvelopedFrom(src, elementType, getConfiguration()))
						.to(newCollectionInstance(dfo));
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
		 * Source type constraint for iterable to iterable handler.
		 */
		private static final Predicate<Type> SOURCE_TYPE_CONSTRAINT = allOf(
				not(isIterable()),
				not(isMap()),
				not(isArray()));

		/**
		 * Destination type constraint for iterable to iterable handler.
		 */
		private static final Predicate<Type> DESTINATION_TYPE_CONSTRAINT = allOf(
				isIterable(),
				not(isMap()),
				not(isArray()));
	}
}
