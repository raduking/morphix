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
import org.morphix.reflection.ExtendedField;

/**
 * Context for field handlers, it can be used to store any information that the handlers need to share between them.
 *
 * @author Radu Sebastian LAZIN
 */
public class FieldHandlerContext {

	/**
	 * The key for the source class in the context map. This is used to store the source class in the context map, so that
	 * it can be reused by the handlers that need it.
	 */
	private static final String SRC_CLASS = "sClass";

	/**
	 * The key for the destination class in the context map. This is used to store the destination class in the context map,
	 * so that it can be reused by the handlers that need it.
	 */
	private static final String DST_CLASS = "dClass";

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

	/**
	 * Returns the source class from the context map, if it is not present, it will be obtained from the source field and
	 * stored in the context map for future use.
	 *
	 * @param sfo the source field
	 * @return the source class
	 */
	public Class<?> getSClass(final ExtendedField sfo) {
		String key = key(SRC_CLASS, sfo);
		Class<?> sClass = get(key);
		if (null == sClass) {
			sClass = sfo.toClass();
			put(key, sClass);
		}
		return sClass;
	}

	/**
	 * Returns the destination class from the context map, if it is not present, it will be obtained from the destination
	 * field and stored in the context map for future use.
	 *
	 * @param dfo the destination field
	 * @return the destination class
	 */
	public Class<?> getDClass(final ExtendedField dfo) {
		String key = key(DST_CLASS, dfo);
		Class<?> dClass = get(key);
		if (null == dClass) {
			dClass = dfo.toClass();
			put(key, dClass);
		}
		return dClass;
	}

	/**
	 * Returns a unique key for the given key and object. This is used to store information in the context map that is
	 * specific to a field, for example, the converted value of a field.
	 *
	 * @param key the key to use as a prefix for the unique key
	 * @param obj the object to use to generate the unique key
	 * @return a unique key for the given key and object
	 */
	public String key(final String key, final Object obj) {
		return key + System.identityHashCode(obj);
	}
}
