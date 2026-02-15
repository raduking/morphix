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
package org.morphix.convert.strategy;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.morphix.convert.ConversionEngine;
import org.morphix.convert.context.ConversionContext;
import org.morphix.convert.context.CyclicReferencesContext;

/**
 * A {@link PropertyConversionStrategy} implementation that supports array properties.
 *
 * @author Radu Sebastian LAZIN
 */
public class PropertyArrayStrategy implements PropertyConversionStrategy {

	/**
	 * Returns {@code true} if the given value is a non-null array, {@code false} otherwise.
	 *
	 * @param obj the value to check
	 * @return {@code true} if the given value is a non-null array, {@code false} otherwise
	 * @see #convert(Object, ConversionEngine, ConversionContext)
	 */
	@Override
	public boolean supports(final Object obj) {
		return obj != null && obj.getClass().isArray();
	}

	/**
	 * Converts the given array to a {@link List} of converted elements. If the given value is not an array, an
	 * {@link IllegalArgumentException} is thrown. If the given value is an array that has already been visited in the
	 * current conversion context (i.e., a cyclic reference), a singleton list containing the string
	 * {@code "CYCLIC_REFERENCE"} is returned.
	 *
	 * @param obj the array to convert
	 * @param engine the conversion engine to use for converting array elements
	 * @param ctx the conversion context to use for tracking visited objects and handling cyclic references
	 * @return a {@link List} of converted elements if the given value is an array, or a singleton list containing
	 * {@code "CYCLIC_REFERENCE"} if the given value is an array that has already been visited in the current conversion
	 * context
	 * @throws IllegalArgumentException if the given value is not an array
	 */
	@Override
	public Object convert(final Object obj, final ConversionEngine engine, final ConversionContext ctx) {
		return ctx.visit(obj,
				() -> convertArray(obj, engine, ctx),
				() -> List.of(CyclicReferencesContext.CYCLIC_REFERENCE));
	}

	/**
	 * Converts the given array to a {@link List} of converted elements using the provided conversion engine and context.
	 *
	 * @param obj the array to convert
	 * @param engine the conversion engine to use for converting array elements
	 * @param ctx the conversion context to use for tracking visited objects and handling cyclic references
	 * @return a {@link List} of converted elements
	 */
	private static List<? extends Object> convertArray(final Object obj, final ConversionEngine engine, final ConversionContext ctx) {
		int len = Array.getLength(obj);
		List<Object> list = new ArrayList<>(len);
		for (int i = 0; i < len; ++i) {
			Object element = Array.get(obj, i);
			list.add(engine.convert(element, ctx));
		}
		return list;
	}
}
