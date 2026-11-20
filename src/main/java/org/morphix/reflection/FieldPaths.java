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
package org.morphix.reflection;

import java.util.Map;

/**
 * Resolves field paths using dot notation for properties and bracket notation for array/list indices and map keys.
 * Multiple paths can be separated by commas outside bracket selectors.
 *
 * @author Radu Sebastian LAZIN
 */
public interface FieldPaths {

	/**
	 * Reads a property, treating maps as containers rather than reflective objects.
	 *
	 * @param obj owning object
	 * @param name property name
	 * @return value or null
	 */
	static Object readProperty(final Object obj, final String name) {
		if (null == obj || null == name || name.isEmpty()) {
			return null;
		}
		return obj instanceof Map<?, ?> map ? map.get(name) : Fields.Safe.get(obj, name);
	}

	/**
	 * Reads a string map key.
	 *
	 * @param obj container
	 * @param key map key
	 * @return value or null
	 */
	static Object readMapKey(final Object obj, final String key) {
		if (null != key && obj instanceof Map<?, ?> map) {
			return map.get(key);
		}
		return null;
	}
}
