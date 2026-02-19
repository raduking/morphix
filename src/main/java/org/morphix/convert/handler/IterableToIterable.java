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
import static org.morphix.convert.IterableConversions.convertIterable;
import static org.morphix.convert.extras.ConverterCollections.isConvertibleIterableType;
import static org.morphix.convert.extras.ConverterCollections.newCollectionInstance;
import static org.morphix.lang.function.Predicates.allOf;
import static org.morphix.lang.function.Predicates.not;
import static org.morphix.reflection.predicates.TypePredicates.isA;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;
import static org.morphix.reflection.predicates.TypePredicates.isMap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.function.Predicate;

import org.morphix.convert.Configuration;
import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Handles {@link Iterable} to {@link Iterable} conversions. This handler requires that the destination is a
 * {@link Iterable} and it has a getter method.
 * <p>
 * The getter method is used to find the element type within the list so that it can instantiate each element of the
 * destination list.
 * <p>
 * In java the return type of a method must keep it's actual generic arguments at runtime (see
 * {@link Method#getGenericReturnType()}).
 *
 * @author Radu Sebastian LAZIN
 */
public final class IterableToIterable extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public IterableToIterable() {
		// empty
	}

	/**
	 * Constructor for custom configuration.
	 *
	 * @param configuration configuration object
	 */
	public IterableToIterable(final Configuration configuration) {
		super(configuration);
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return HANDLED;
		}
		Type elementType = getIterableElementType(dfo);
		if (null == elementType) {
			return HANDLED;
		}
		if (isA(TypeVariable.class).test(elementType)) {
			Type type = getConfiguration().getGenericType(elementType.getTypeName());
			if (null != type) {
				elementType = type;
			}
		}
		Type resolvedElementType = elementType;

		Iterable<?> dValue = convertIterable((Iterable<?>) sValue,
				src -> convertEnvelopedFrom(src, resolvedElementType, getConfiguration()))
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
	 * Returns the element class from the destination {@link Iterable} field. It uses a trick where the JRE retains the
	 * generic type information for method return types.
	 * <p>
	 * Example: for <code>List&lt;String&gt;</code> the method will return <code>Class&lt;String&gt;</code>
	 *
	 * @param fop field object pair
	 * @return the element class from the destination iterable field
	 */
	public static Type getIterableElementType(final ExtendedField fop) {
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
		 * Source type constraint for iterable to iterable handler.
		 */
		private static final Predicate<Type> SOURCE_TYPE_CONSTRAINT = allOf(
				isIterable(),
				not(isMap()));

		/**
		 * Destination type constraint for iterable to iterable handler.
		 */
		private static final Predicate<Type> DESTINATION_TYPE_CONSTRAINT = allOf(
				isConvertibleIterableType(),
				not(isMap()));
	}
}
