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
import static org.morphix.lang.function.Predicates.not;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;
import static org.morphix.reflection.predicates.TypePredicates.isMap;

import java.lang.reflect.Type;
import java.util.function.Predicate;

import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Handles direct assignment that is when the source field is directly assignable to the destination field.
 *
 * @author Radu Sebastian LAZIN
 */
public final class DirectAssignment extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public DirectAssignment() {
		// empty
	}

	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null != sValue) {
			dfo.setFieldValue(sValue);
		}
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return not(isMap());
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return not(isIterable());
	}

	@Override
	public boolean condition(final ExtendedField sfo, final ExtendedField dfo) {
		Class<?> dClass = dfo.toClass();
		Class<?> sClass = sfo.toClass();
		return dClass.isAssignableFrom(sClass);
	}

}
