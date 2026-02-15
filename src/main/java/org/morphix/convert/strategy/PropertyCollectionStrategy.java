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

import java.util.Collection;
import java.util.List;

import org.morphix.convert.ConversionEngine;
import org.morphix.convert.context.ConversionContext;
import org.morphix.convert.context.CyclicReferencesContext;

/**
 * A {@link PropertyConversionStrategy} for {@link Collection}s. It converts each element of the collection using the
 * provided {@link ConversionEngine}.
 *
 * @author Radu Sebastian LAZIN
 */
public class PropertyCollectionStrategy implements PropertyConversionStrategy {

	/**
	 * Returns {@code true} if the provided object is an instance of {@link Collection}, {@code false} otherwise.
	 *
	 * @param obj the object to check
	 * @return {@code true} if the provided object is an instance of {@link Collection}, {@code false} otherwise
	 */
	@Override
	public boolean supports(final Object obj) {
		return obj instanceof Collection<?>;
	}

	/**
	 * Converts the provided collection by converting each element using the provided {@link ConversionEngine}. If a cyclic
	 * reference is detected, it returns a list containing a single element:
	 * {@link CyclicReferencesContext#CYCLIC_REFERENCE}.
	 *
	 * @param obj the collection to convert
	 * @param engine the conversion engine to use for converting each element
	 * @param ctx the conversion context to use for detecting cyclic references
	 * @return a list containing the converted elements of the collection, or a list with a single element indicating a
	 * cyclic reference if one is detected
	 */
	@Override
	public Object convert(final Object obj, final ConversionEngine engine, final ConversionContext ctx) {
		return ctx.visit(obj,
				() -> ((Collection<?>) obj).stream().map(o -> engine.convert(o, ctx)).toList(),
				() -> List.of(CyclicReferencesContext.CYCLIC_REFERENCE));
	}
}
