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
import org.morphix.reflection.ExtendedFields;

/**
 * Source field finding strategy.
 *
 * @author Radu Sebastian LAZIN
 */
public interface FieldFinderStrategy {

	/**
	 * Finds the source field object to convert to destination.
	 *
	 * @param <T> source object type
	 *
	 * @param source source object
	 * @param fields source object fields if available, can be empty
	 * @param sourceFieldName source field name
	 * @return the source field object to convert to destination
	 */
	<T> ExtendedField find(T source, List<ExtendedField> fields, String sourceFieldName);

	/**
	 * Finds all the source field objects to convert to destination.
	 *
	 * @param <T> source object type
	 *
	 * @param source source object
	 * @return all the source field objects to convert to destination
	 */
	default <T> List<ExtendedField> findAll(final T source) {
		return ExtendedFields.findAllNonStatic(source);
	}
}
