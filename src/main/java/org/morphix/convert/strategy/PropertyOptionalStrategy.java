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

import java.util.Optional;

import org.morphix.convert.ConversionEngine;
import org.morphix.convert.context.ConversionContext;

/**
 * A {@link PropertyConversionStrategy} implementation that handles conversion of {@link Optional} properties.
 *
 * @author Radu Sebastian LAZIN
 */
public class PropertyOptionalStrategy implements PropertyConversionStrategy {

	/**
	 * Determines if the given object is an instance of {@link Optional}.
	 *
	 * @param obj the object to check
	 * @return {@code true} if the object is an instance of {@link Optional}, {@code false} otherwise
	 */
	@Override
	public boolean supports(final Object obj) {
		return obj instanceof Optional<?>;
	}

	/**
	 * Converts the given {@link Optional} object by unwrapping it and converting the contained value if present.
	 *
	 * @param obj the object to convert, expected to be an instance of {@link Optional}
	 * @param engine the conversion engine to use for converting the contained value
	 * @param ctx the conversion context to use during conversion
	 * @return the converted value if present, or {@code null} if the {@link Optional} is empty
	 */
	@Override
	public Object convert(final Object obj, final ConversionEngine engine, final ConversionContext ctx) {
		return ((Optional<?>) obj)
				.map(o -> engine.convert(o, ctx))
				.orElse(null);
	}
}
