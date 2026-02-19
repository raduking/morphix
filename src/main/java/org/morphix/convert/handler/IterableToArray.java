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
import static org.morphix.convert.FieldHandlerResult.BREAK;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.convert.IterableConversions.convertIterable;
import static org.morphix.reflection.predicates.TypePredicates.isArray;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Predicate;

import org.morphix.convert.Configuration;
import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Handles {@link Iterable} to java array conversions. This handler requires that the destination is an array.
 *
 * @author Radu Sebastian LAZIN
 */
public final class IterableToArray extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public IterableToArray() {
		// empty
	}

	/**
	 * Constructor for custom configuration.
	 *
	 * @param configuration configuration object
	 */
	public IterableToArray(final Configuration configuration) {
		super(configuration);
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return BREAK;
		}
		Object[] dValue = (Object[]) dfo.getFieldValue();
		if (null != dfo.getFieldValue() && dValue.length > 0) {
			return BREAK;
		}
		Class<?> arrayElementClass = dfo.toClass().getComponentType();
		if (null == arrayElementClass) {
			return BREAK;
		}

		Collection<?> arrayList = convertIterable((Iterable<?>) sValue,
				src -> convertEnvelopedFrom(src, arrayElementClass, getConfiguration()))
						.toList();
		dValue = (Object[]) Array.newInstance(arrayElementClass, arrayList.size());

		dfo.setFieldValue(arrayList.toArray(dValue));

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
		 * Source type constraint for this handler. It checks if the source type is an {@link Iterable}.
		 */
		private static final Predicate<Type> SOURCE_TYPE_CONSTRAINT = isIterable();

		/**
		 * Destination type constraint for this handler. It checks if the destination type is an array.
		 */
		private static final Predicate<Type> DESTINATION_TYPE_CONSTRAINT = isArray();
	}
}
