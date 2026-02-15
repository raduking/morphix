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
 * A {@link PropertyConversionStrategy} that converts any object to a map of its properties. The keys of the map are the
 * property names, and the values are the converted property values. If a cyclic reference is detected, the value will
 * be a map containing a single entry with the key {@link CyclicReferencesContext#CYCLIC_REFERENCE} and the value being
 * the simple name of the class of the cyclic reference.
 *
 * @author Radu Sebastian LAZIN
 */
public class PropertyBeanStrategy implements PropertyConversionStrategy {

	/**
	 * Returns {@code true} for any object, indicating that this strategy supports converting any object to a map of its
	 * properties.
	 *
	 * @param obj the object to check for support
	 * @return {@code true} if this strategy supports converting the given object, {@code false} otherwise.
	 */
	@Override
	public boolean supports(final Object obj) {
		return true;
	}

	/**
	 * Converts the given object to a map of its properties. The keys of the map are the property names, and the values are
	 * the converted property values. If a cyclic reference is detected, the value will be a map containing a single entry
	 * with the key {@link CyclicReferencesContext#CYCLIC_REFERENCE} and the value being the simple name of the class of the
	 * cyclic reference.
	 *
	 * @param v the object to convert
	 * @param engine the conversion engine to use for converting property values
	 * @param ctx the conversion context to use for tracking cyclic references
	 * @return a map of property names to converted property values, or a map indicating a cyclic reference if one is
	 * detected.
	 */
	@Override
	public Object convert(final Object v, final ConversionEngine engine, final ConversionContext ctx) {
		return ctx.visit(v,
				() -> MapConversions.convertToMap(v, k -> k, val -> engine.convert(val, ctx)),
				() -> Map.of(CyclicReferencesContext.CYCLIC_REFERENCE, v.getClass().getSimpleName()));
	}
}
