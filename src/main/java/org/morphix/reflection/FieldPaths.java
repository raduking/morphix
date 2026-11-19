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

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import org.morphix.lang.JavaObjects;

/**
 * Resolves field paths using dot notation for properties and bracket notation for array/list indices and map keys.
 * Multiple paths can be separated by commas outside bracket selectors.
 *
 * @author Radu Sebastian LAZIN
 */
public interface FieldPaths {

	/**
	 * Resolves a single path to its value.
	 *
	 * @param <U> value type
	 *
	 * @param root root object
	 * @param path field path
	 * @return value or null if the path cannot be resolved
	 */
	static <U> U resolveValue(final Object root, final String path) {
		return JavaObjects.cast(resolve(root, path, false, false));
	}

	/**
	 * Resolves the first non-null value in a comma-separated list of paths. Empty alternatives are ignored.
	 *
	 * @param <U> value type
	 *
	 * @param root root object
	 * @param paths alternative field paths
	 * @return first non-null value or null
	 */
	static <U> U resolveValues(final Object root, final String paths) {
		return JavaObjects.cast(resolve(root, paths, true, false));
	}

	/**
	 * Finds the first non-static reflective field and its owning object in a comma-separated list of paths. The final field
	 * is not read; a field with a null value can therefore be found. Paths ending at a container element rather than a
	 * reflective field are not matches.
	 *
	 * @param root root object
	 * @param paths alternative field paths
	 * @return field and its owning object, or {@link ExtendedField#EMPTY}
	 */
	static ExtendedField resolveField(final Object root, final String paths) {
		ExtendedField field = JavaObjects.cast(resolve(root, paths, true, true));
		return null == field ? ExtendedField.EMPTY : field;
	}

	private static Object resolve(final Object root, final String paths, final boolean alternatives, final boolean findField) {
		if (null == root || null == paths || paths.isEmpty()) {
			return null;
		}
		return new FieldPathParser(paths, alternatives, findField).resolve(root);
	}

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
	 * Reads a zero-based array or list index.
	 *
	 * @param obj container
	 * @param index element index
	 * @return element or null
	 */
	static Object readIndex(final Object obj, final int index) {
		if (null == obj || index < 0) {
			return null;
		}
		if (obj instanceof List<?> list) {
			return index < list.size() ? list.get(index) : null;
		}
		if (obj.getClass().isArray()) {
			return index < Array.getLength(obj) ? Array.get(obj, index) : null;
		}
		return null;
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
