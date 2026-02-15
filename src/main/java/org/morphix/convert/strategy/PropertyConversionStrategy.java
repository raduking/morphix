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

import org.morphix.convert.ConversionEngine;
import org.morphix.convert.context.ConversionContext;

/**
 * Interface for property conversion strategies. Implementations of this interface are responsible for converting
 * property values during the conversion process.
 *
 * @author Radu Sebastian LAZIN
 */
public interface PropertyConversionStrategy {

	/**
	 * Determines if this strategy supports converting the given value.
	 *
	 * @param value the value to check for support
	 * @return {@code true} if this strategy supports converting the value, {@code false} otherwise
	 */
	boolean supports(Object value);

	/**
	 * Converts the given value using this strategy.
	 *
	 * @param value the value to convert
	 * @param engine the conversion engine to use for any nested conversions
	 * @param ctx the conversion context containing information about the conversion process
	 * @return the converted value
	 */
	Object convert(Object value, ConversionEngine engine, ConversionContext ctx);
}
