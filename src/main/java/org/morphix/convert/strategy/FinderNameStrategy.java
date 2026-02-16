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
import java.util.Objects;

import org.morphix.convert.annotation.Src;
import org.morphix.reflection.ExtendedField;

/**
 * Simple strategy which finds the field in the source by its name.
 *
 * @author Radu Sebastian LAZIN
 */
public class FinderNameStrategy implements FieldFinderStrategy {

	/**
	 * Default constructor.
	 */
	public FinderNameStrategy() {
		// empty
	}

	/**
	 * @see FieldFinderStrategy#find(Object, List, String)
	 */
	@Override
	public <T> ExtendedField find(final T source, final List<ExtendedField> fields, final String sourceFieldName) {
		// TODO: implement for empty fields, assume fields are available for now
		return findFieldByName(fields, sourceFieldName);
	}

	/**
	 * Finds a field by name. Field name will never be null since the converter only searches through existing fields on
	 * destination or {@link Src} annotation parameters which can only be constants which cannot be null.
	 *
	 * @param fields source object fields
	 * @param fieldName field name
	 * @return extended field if found, empty otherwise
	 */
	protected static ExtendedField findFieldByName(final List<ExtendedField> fields, final String fieldName) {
		for (ExtendedField extendedField : fields) {
			if (Objects.equals(fieldName, extendedField.getName())) {
				return extendedField;
			}
		}
		return ExtendedField.EMPTY;
	}
}
