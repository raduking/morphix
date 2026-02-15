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

import java.util.Map;

import org.morphix.convert.ConversionEngine;
import org.morphix.convert.MapConversions;
import org.morphix.convert.context.ConversionContext;
import org.morphix.convert.context.CyclicReferencesContext;

/**
 * A {@link PropertyConversionStrategy} for converting {@link Map} objects to a map of string keys and converted values.
 *
 * @author Radu Sebastian LAZIN
 */
public class PropertyMapStrategy implements PropertyConversionStrategy {

	/**
	 * Determines if the given type is supported by this conversion strategy. It returns true if the type is a {@link Map}
	 * or a subclass of {@link Map}.
	 *
	 * @param type the class type to check for support
	 * @return true if the type is a {@link Map} or a subclass of {@link Map}, false otherwise
	 */
	@Override
	public boolean supportsType(final Class<?> type) {
		return Map.class.isAssignableFrom(type);
	}

	/**
	 * Converts the given {@link Map} object to a map of string keys and converted values. If the object has already been
	 * visited in the current conversion context, it returns a map with a single entry indicating a cyclic reference.
	 *
	 * @param obj the object to convert
	 * @param engine the conversion engine to use for converting values
	 * @param ctx the conversion context to track visited objects and handle cyclic references
	 * @return a map of string keys and converted values, or a map indicating a cyclic reference if the object has already
	 * been visited
	 */
	@Override
	public Object convert(final Object obj, final ConversionEngine engine, final ConversionContext ctx) {
		return ctx.visit(obj,
				() -> MapConversions.convertMap((Map<?, ?>) obj, String::valueOf, val -> engine.convert(val, ctx)).toMap(),
				() -> Map.of(CyclicReferencesContext.CYCLIC_REFERENCE, obj.getClass().getSimpleName()));
	}
}
