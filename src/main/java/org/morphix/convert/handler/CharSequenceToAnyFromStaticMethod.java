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
import static org.morphix.convert.FieldHandlerResult.SKIP;
import static org.morphix.lang.function.Predicates.not;
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
 * Handles {@link CharSequence} to any class (SomeClass) that has a method like:
 * <p>
 * <code>public static SomeClass SomeClass::someStaticMethod(CharSequence charSequenceParam);</code>
 * <p>
 * <code>public static SomeClass SomeClass::someStaticMethod(String stringParam);</code>
 * <p>
 * and will call the method for setting the destination field (of type SomeClass). Will exclude {@link CharSequence}
 * since it is already handled in {@link DirectAssignment}. Will exclude {@link Enum} since it is already handled in
 * {@link CharSequenceToEnum}.
 *
 * @author Radu Sebastian LAZIN
 */
public final class CharSequenceToAnyFromStaticMethod extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public CharSequenceToAnyFromStaticMethod() {
		// empty
	}

	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		Class<?> dClass = dfo.toClass();
		List<Method> staticConvertMethods = getConverterMethods(dClass, CharSequence.class);
		if (null == sValue && !staticConvertMethods.isEmpty()) {
			return BREAK;
		}
		// find a method in the dClass that can convert the value from a
		// CharSequence value
		for (Method method : staticConvertMethods) {
			Object dValue = Methods.IgnoreAccess.invoke(method, dClass, sValue);
			if (null != dValue) {
				dfo.setFieldValue(dValue);
				return CONVERTED;
			}
		}
		return SKIP;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return isCharSequence();
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return not(isEnum().or(isCharSequence()));
	}

}
