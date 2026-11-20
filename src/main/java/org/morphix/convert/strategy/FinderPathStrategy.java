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

import java.util.List;

import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.FieldPathParser;

/**
 * Finds the field in the source by path.
 *
 * @author Radu Sebastian LAZIN
 */
public class FinderPathStrategy implements FieldFinderStrategy {

	/**
	 * Default constructor.
	 */
	public FinderPathStrategy() {
		// empty
	}

	/**
	 * @see FieldFinderStrategy#find(Object, List, String)
	 */
	@Override
	public <T> ExtendedField find(final T source, final List<ExtendedField> fields, final String sourceFieldName) {
		return getConverterFieldByPathInHierarchy(source, sourceFieldName);
	}

	/**
	 * Returns the first field and its owning object from comma-separated alternative paths.
	 *
	 * @param <T> object type
	 *
	 * @param obj object on which to search
	 * @param path alternative paths to the wanted field
	 * @return field object pair
	 */
	public static <T> ExtendedField getConverterFieldByPathInHierarchy(final T obj, final String path) {
		return FieldPathParser.resolveFirstField(obj, path);
	}
}
