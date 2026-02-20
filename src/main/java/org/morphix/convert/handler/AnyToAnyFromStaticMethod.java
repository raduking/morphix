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
import static org.morphix.convert.FieldHandlerResult.SKIPPED;
import static org.morphix.lang.function.Predicates.not;
import static org.morphix.reflection.predicates.TypePredicates.isCharSequence;

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
 * Handles {@link CharSequence} to any class (SomeClass) that has a method like:
 * <p>
 * <code>public static SomeClass SomeClass::someStaticMethod(SourceClass sourceClassParam);</code>
 * <p>
 * and will call the method for setting the destination field (of type SomeClass). Will exclude {@link CharSequence}
 * since it is already handled in {@link DirectAssignment}. Will exclude {@link Enum} since it is already handled in
 * {@link CharSequenceToEnum}.
 *
 * @author Radu Sebastian LAZIN
 */
public final class AnyToAnyFromStaticMethod extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public AnyToAnyFromStaticMethod() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext ctx) {
		Object sValue = sfo.getFieldValue();
		Class<?> dClass = dfo.toClass();
		Class<?> sClass = sfo.toClass();
		List<Method> staticConvertMethods = getConverterMethods(dClass, sClass);
		if (null == sValue && !staticConvertMethods.isEmpty()) {
			return HANDLED;
		}
		// find a method in the dClass that can convert the value from a source class value
		for (Method method : staticConvertMethods) {
			Object dValue = Methods.IgnoreAccess.invoke(method, dClass, sValue);
			if (null != dValue) {
				dfo.setFieldValue(dValue);
				return CONVERTED;
			}
		}
		return SKIPPED;
	}

	/**
	 * @see FieldHandler#sourceTypeConstraint()
	 */
	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return PredicateHolder.SOURCE_TYPE_CONSTRAINT;
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
		private static final Predicate<Type> SOURCE_TYPE_CONSTRAINT = not(isCharSequence());
	}
}
