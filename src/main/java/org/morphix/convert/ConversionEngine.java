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
package org.morphix.convert;

import org.morphix.convert.context.ConversionContext;

/**
 * Interface for a conversion engine that can convert values from one type to another.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ConversionEngine {

	/**
	 * Converts the given value to a different type based on the provided conversion context.
	 *
	 * @param value the value to be converted
	 * @param ctx the conversion context containing information about the conversion
	 * @return the converted value
	 */
	Object convert(Object value, ConversionContext ctx);
}
