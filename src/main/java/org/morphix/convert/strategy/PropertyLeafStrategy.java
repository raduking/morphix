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

import java.util.UUID;

import org.morphix.convert.ConversionEngine;
import org.morphix.convert.context.ConversionContext;

/**
 * A {@link PropertyConversionStrategy} that handles leaf properties, i.e. properties that are not complex objects or
 * collections. It supports null values, CharSequence, Number, Boolean, Enum and UUID types.
 *
 * @author Radu Sebastian LAZIN
 */
public class PropertyLeafStrategy implements PropertyConversionStrategy {

	/**
	 * Checks if the given type is supported by this strategy. Supported types are:
	 * <ul>
	 * <li>{@link CharSequence}</li>
	 * <li>{@link Number}</li>
	 * <li>{@link Boolean}</li>
	 * <li>{@link Enum}</li>
	 * <li>{@link UUID}</li>
	 * </ul>
	 *
	 * @param type the type to check
	 * @return {@code true} if the type is supported, {@code false} otherwise
	 */
	@Override
	public boolean supportsType(final Class<?> type) {
		return CharSequence.class.isAssignableFrom(type) ||
				Number.class.isAssignableFrom(type) ||
				Boolean.class.isAssignableFrom(type) ||
				Enum.class.isAssignableFrom(type) ||
				UUID.class.isAssignableFrom(type);
	}

	/**
	 * Converts the given value to a String representation. Supported types are:
	 * <ul>
	 * <li>{@code null} is converted to {@code null}</li>
	 * <li>{@link CharSequence} is converted to its String representation</li>
	 * <li>{@link Number} is converted to its String representation</li>
	 * <li>{@link Boolean} is converted to its String representation</li>
	 * <li>{@link Enum} is converted to its name</li>
	 * <li>{@link UUID} is converted to its String representation</li>
	 * </ul>
	 *
	 * @param v the value to convert
	 * @param engine the conversion engine to use for nested conversions (not used in this strategy)
	 * @param ctx the conversion context (not used in this strategy)
	 * @return the converted value as a String, or {@code null} if the input value is {@code null}
	 * @throws IllegalStateException if the value type is not supported
	 */
	@Override
	public Object convert(final Object v, final ConversionEngine engine, final ConversionContext ctx) {
		return switch (v) {
			case null -> null;
			case CharSequence cs -> cs.toString();
			case Number n -> n.toString();
			case Boolean b -> b.toString();
			case Enum<?> e -> e.name();
			case UUID u -> u.toString();
			default -> throw new IllegalStateException("Unsupported property leaf type: " + v.getClass());
		};
	}
}
