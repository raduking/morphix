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
	 * @see ConversionStrategy#find(Object, String)
	 */
	@Override
	public ExtendedField find(final Object source, final String sourceFieldName) {
		Optional<ExtendedField> sField = findFieldByName(source, sourceFieldName);
		return sField.orElse(ExtendedField.EMPTY);
	}

	/**
	 * Finds a field by name. Field name will never be null since the converter only searches through existing fields on
	 * destination or {@link Src} annotation parameters which can only be constants which cannot be null.
	 *
	 * @param <T> object type
	 *
	 * @param obj object on which to find the field
	 * @param fieldName field name
	 * @return optional with field information
	 */
	protected static <T> Optional<ExtendedField> findFieldByName(final T obj, final String fieldName) {
		return ConversionStrategy.findFields(obj,
				converterField -> fieldName.equals(converterField.getName())).findFirst();
	}

}
