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
import static org.morphix.convert.FieldHandlerResult.HANDLED;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.convert.extras.ConverterCollections.newCollectionInstance;
import static org.morphix.convert.handler.IterableToIterable.getIterableElementType;
import static org.morphix.reflection.predicates.TypePredicates.isArray;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Predicate;

import org.morphix.convert.Configuration;
import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Array to Iterable conversions.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ArrayToIterable extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public ArrayToIterable() {
		// empty
	}

	/**
	 * Constructor for custom configuration.
	 *
	 * @param configuration configuration object
	 */
	public ArrayToIterable(final Configuration configuration) {
		super(configuration);
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object[] sValue = (Object[]) sfo.getFieldValue();
		if (null == sValue) {
			return HANDLED;
		}
		Type elementType = getIterableElementType(dfo);
		if (null == elementType) {
			return HANDLED;
		}

		Collection<?> dValue = newCollectionInstance(dfo);
		for (Object element : sValue) {
			dValue.add(convertEnvelopedFrom(element, elementType, getConfiguration()));
		}
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
		 * Source type constraint for this handler.
		 */
		private static final Predicate<Type> SOURCE_TYPE_CONSTRAINT = isArray();

		/**
		 * Destination type constraint for this handler.
		 */
		private static final Predicate<Type> DESTINATION_TYPE_CONSTRAINT = isIterable();
	}
}
