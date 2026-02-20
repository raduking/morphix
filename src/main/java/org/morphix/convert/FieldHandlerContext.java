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

import java.util.HashMap;
import java.util.Map;

import org.morphix.lang.JavaObjects;

/**
 * Context for field handlers, it can be used to store any information that the handlers need to share between them.
 *
 * @author Radu Sebastian LAZIN
 */
public class FieldHandlerContext {

	/**
	 * Context map, it can store any information that the handlers need to share between them.
	 */
	private final Map<String, Object> context = new HashMap<>();

	/**
	 * Default constructor.
	 */
	public FieldHandlerContext() {
		// empty
	}

	/**
	 * Puts a value in the context map.
	 *
	 * @param key the key to store the value under
	 * @param value the value to store
	 */
	public void put(final String key, final Object value) {
		context.put(key, value);
	}

	/**
	 * Gets a value from the context map.
	 *
	 * @param <T> the return type
	 *
	 * @param key the key to get the value from
	 * @return the value stored under the key, or null if no value is stored under the key
	 */
	public <T> T get(final String key) {
		return JavaObjects.cast(context.get(key));
	}
}
