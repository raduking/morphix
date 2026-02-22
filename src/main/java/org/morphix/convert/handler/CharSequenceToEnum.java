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
import static org.morphix.convert.FieldHandlerResult.HANDLED;
import static org.morphix.reflection.predicates.TypePredicates.isCharSequence;
import static org.morphix.reflection.predicates.TypePredicates.isEnum;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerContext;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.Methods;

/**
 * Handles any object to {@link Enum} fields via public static final method. It will call the default 'valueOf' method
 * last when another method is present with the same signature.
 *
 * @author Radu Sebastian LAZIN
 */
public final class CharSequenceToEnum extends FieldHandler {

	/**
	 * {@link Enum#valueOf(Class, String)} method name constant.
	 */
	private static final String VALUE_OF_METHOD_NAME = "valueOf";

	/**
	 * Default constructor.
	 */
	public CharSequenceToEnum() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext ctx) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return HANDLED;
		}
		Class<?> dClass = dfo.toClass();
		// find a method in the dClass (Enum class) that can convert the value from a CharSequence / String value
		List<Method> methods = getConverterMethods(dClass, CharSequence.class);
		Method valueOfMethod = null;
		for (Method method : methods) {
			if (VALUE_OF_METHOD_NAME.equals(method.getName())) {
				valueOfMethod = method;
			} else {
				Object dValue = Methods.IgnoreAccess.invoke(method, dClass, sValue);
				if (null != dValue) {
					dfo.setFieldValue(dValue);
					return CONVERTED;
				}
			}
		}
		// invoke the 'valueOf' method last
		Object dValue = Methods.IgnoreAccess.invoke(valueOfMethod, dClass, sValue);
		// dValue will never be null because 'valueOf' will fail for wrong values
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
		 * Source type constraint for this handler. It checks if the source type is a class and it is a {@link CharSequence}.
		 */
		private static final Predicate<Type> SOURCE_TYPE_CONSTRAINT = isCharSequence();

		/**
		 * Destination type constraint for this handler. It checks if the destination type is a class and it is an {@link Enum}.
		 */
		private static final Predicate<Type> DESTINATION_TYPE_CONSTRAINT = isEnum();
	}
}
