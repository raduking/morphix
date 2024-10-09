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

import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.Fields;

/**
 * Finds the field in the source by path.
 *
 * @author Radu Sebastian LAZIN
 */
public class PathStrategy implements ConversionStrategy {

	/**
	 * Default constructor.
	 */
	public PathStrategy() {
		// empty
	}

	/**
	 * @see ConversionStrategy#find(Object, String)
	 */
	@Override
	public ExtendedField find(final Object source, final String sourceFieldName) {
		return getConverterFieldByPathInHierarchy(source, sourceFieldName);
	}

	/**
	 * Returns the field and the object it corresponds to given its path.
	 *
	 * @param <T> object type
	 *
	 * @param obj object on which to search
	 * @param path path to the wanted field (object)
	 * @return field object pair
	 */
	public static <T> ExtendedField getConverterFieldByPathInHierarchy(final T obj, final String path) {
		String[] fieldNames = path.split("\\.");

		Field resultField = null;
		Object resultObject = obj;
		Class<?> currentClass = resultObject.getClass();

		for (String fieldName : fieldNames) {
			if (null != resultField && null != resultObject) {
				resultObject = Fields.IgnoreAccess.get(resultObject, resultField);
			}
			resultField = Fields.getDeclaredFieldInHierarchy(currentClass, fieldName);
			if (null != resultField && Modifier.isStatic(resultField.getModifiers())) {
				resultField = null;
			}
			if (null != resultField) {
				currentClass = resultField.getType();
			} else {
				resultObject = null;
			}
			if (resultField == null) {
				break;
			}
		}
		return of(resultField, resultObject);
	}

}
