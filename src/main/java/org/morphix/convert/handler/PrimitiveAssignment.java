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

import java.util.Objects;

import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerContext;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.Primitives;

/**
 * Handles primitives and their respective auto boxing counterparts.
 *
 * @author Radu Sebastian LAZIN
 */
public final class PrimitiveAssignment extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public PrimitiveAssignment() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext context) {
		Object sValue = sfo.getFieldValue();
		if (null != sValue) {
			dfo.setFieldValue(sValue);
		}
		return CONVERTED;
	}

	/**
	 * @see FieldHandler#condition(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	public boolean condition(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext ctx) {
		Class<?> dClass = dfo.toClass();
		Class<?> sClass = sfo.toClass();
		if (Objects.equals(sClass, dClass)) {
			return false;
		}
		return isPrimitiveToClass(sClass, dClass)
				|| isPrimitiveToClass(dClass, sClass);
	}

	/**
	 * Returns true if the first given class is the primitive class of the second given class.
	 *
	 * @param clsPrimitive primitive class
	 * @param cls class to check against
	 * @return true if the first given class is the primitive class of the second given class, false otherwise
	 */
	public static boolean isPrimitiveToClass(final Class<?> clsPrimitive, final Class<?> cls) {
		if (clsPrimitive.isPrimitive()) {
			Class<?> clsBoxed = Primitives.getBoxedClass(clsPrimitive);
			return clsBoxed.isAssignableFrom(cls);
		}
		return false;
	}
}
