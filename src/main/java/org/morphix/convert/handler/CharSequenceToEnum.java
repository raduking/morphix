/*
 * Copyright 2025 the original author or authors.
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
import static org.morphix.reflection.predicates.TypePredicates.isCharSequence;
import static org.morphix.reflection.predicates.TypePredicates.isEnum;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

import org.morphix.convert.FieldHandler;
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

	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return BREAK;
		}
		Class<?> dClass = dfo.toClass();
		// find a method in the dClass (Enum class) that can convert the
		// value from a CharSequence / String value
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

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return isCharSequence();
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return isEnum();
	}

}
