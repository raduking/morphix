/*
 * Copyright 2025 the original author or authors.
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
import java.util.Optional;

import org.morphix.convert.annotation.Src;
import org.morphix.reflection.ExtendedField;

/**
 * Simple strategy which finds the field in the source by its name.
 *
 * @author Radu Sebastian LAZIN
 */
public class BasicNameStrategy implements ConversionStrategy {

	/**
	 * Default constructor.
	 */
	public BasicNameStrategy() {
		// empty
	}

	/**
	 * @see ConversionStrategy#find(Object, List, String)
	 */
	@Override
	public <T> ExtendedField find(final T source, final List<ExtendedField> fields, final String sourceFieldName) {
		// TODO: implement for empty fields, assume fields are available for now
		Optional<ExtendedField> sField = findFieldByName(fields, sourceFieldName);
		return sField.orElse(ExtendedField.EMPTY);
	}

	/**
	 * Finds a field by name. Field name will never be null since the converter only searches through existing fields on
	 * destination or {@link Src} annotation parameters which can only be constants which cannot be null.
	 *
	 * @param fields source object fields
	 * @param fieldName field name
	 * @return optional with field information
	 */
	protected static Optional<ExtendedField> findFieldByName(final List<ExtendedField> fields, final String fieldName) {
		for (ExtendedField extendedField : fields) {
			if (Objects.equals(fieldName, extendedField.getName())) {
				return Optional.of(extendedField);
			}
		}
		return Optional.empty();
	}

}
