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

import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.morphix.convert.function.SimpleConverter;
import org.morphix.lang.Case;
import org.morphix.reflection.ExtendedField;

/**
 * Finder strategy that finds fields in a map if the source is a {@link Map}.
 *
 * @author Radu Sebastian LAZIN
 */
public class FinderMapKeyStrategy implements FieldFinderStrategy {

	/**
	 * List of key converters to apply to the source field name when searching for a value in the map. The converters are
	 * applied in order, and the search stops as soon as a value is found for a converted key. The converters include:
	 * <ul>
	 * <li>{@link Case#KEBAB}</li>
	 * </ul>
	 */
	private static final List<SimpleConverter<String, String>> KEY_CONVERTERS = List.of(
			Case.KEBAB::convert);

	/**
	 * Default constructor.
	 */
	public FinderMapKeyStrategy() {
		// empty
	}

	/**
	 * @see FieldFinderStrategy#find(Object, List, String)
	 */
	@Override
	public <T> ExtendedField find(final T source, final List<ExtendedField> fields, final String sourceFieldName) {
		if (source instanceof Map<?, ?> sourceMap) {
			Object value = sourceMap.get(sourceFieldName);
			if (null == value) {
				value = findValueByKeyConverters(sourceMap, sourceFieldName);
			}
			return of((Field) null, value);
		}
		return ExtendedField.EMPTY;
	}

	/**
	 * Finds a value in the given map by applying the key converters to the source field name.
	 *
	 * @param sourceMap the map to search for the value
	 * @param sourceFieldName the source field name to convert and search for
	 * @return the value found in the map, or {@code null} if no value is found
	 */
	private static Object findValueByKeyConverters(final Map<?, ?> sourceMap, final String sourceFieldName) {
		for (SimpleConverter<String, String> keyConverter : KEY_CONVERTERS) {
			String convertedKey = keyConverter.convert(sourceFieldName);
			Object value = sourceMap.get(convertedKey);
			if (null != value) {
				return value;
			}
		}
		return null;
	}
}
