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
import static org.morphix.convert.FieldHandlerResult.SKIP;
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

	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return SKIP;
		}
		Type elementType = IterableToIterable.getIterableElementType(dfo);
		if (null == elementType) {
			return BREAK;
		}
		Iterable<?> sIterable = Collections.singletonList(sValue);
		Iterable<?> dValue = convertIterable(sIterable,
				src -> convertEnvelopedFrom(src, elementType, getConfiguration()))
						.to(newCollectionInstance(dfo));
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return allOf(
				not(isIterable()),
				not(isMap()),
				not(isArray()));
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return allOf(
				isIterable(),
				not(isMap()),
				not(isArray()));
	}

}
