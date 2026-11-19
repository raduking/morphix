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
import static org.morphix.reflection.JavaModifier.STATIC;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.morphix.lang.collections.Lists;
import org.morphix.reflection.Classes;
import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.FieldPaths;
import org.morphix.reflection.FieldPaths.Step;
import org.morphix.reflection.FieldPaths.StepKind;
import org.morphix.reflection.Fields;

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
	 * Returns the field and the object it corresponds to given its path.
	 *
	 * @param <T> object type
	 *
	 * @param obj object on which to search
	 * @param path path to the wanted field (object)
	 * @return field object pair
	 */
	public static <T> ExtendedField getConverterFieldByPathInHierarchy(final T obj, final String path) {
		List<Step> steps = FieldPaths.parse(path);
		if (Lists.isEmpty(steps)) {
			return ExtendedField.EMPTY;
		}
		Field resultField = null;
		Object resultObject = obj;
		Class<?> currentClass = resultObject.getClass();
		for (Step step : steps) {
			if (StepKind.PROPERTY == step.kind()) {
				if (null != resultField && null != resultObject) {
					resultObject = Fields.IgnoreAccess.get(resultObject, resultField);
				}
				if (null == resultObject) {
					return ExtendedField.EMPTY;
				}
				currentClass = Classes.getFrom(resultObject);
				resultField = Fields.getOneDeclaredInHierarchy(currentClass, step.selector());
				if (null != resultField && STATIC.isPresentOn(resultField)) {
					return ExtendedField.EMPTY;
				}
				if (null == resultField && resultObject instanceof Map<?, ?> map) {
					if (!map.containsKey(step.selector())) {
						return ExtendedField.EMPTY;
					}
					resultObject = map.get(step.selector());
					continue;
				}
				if (null == resultField) {
					return ExtendedField.EMPTY;
				}
			} else {
				if (null != resultField && null != resultObject) {
					resultObject = Fields.IgnoreAccess.get(resultObject, resultField);
				}
				resultObject = FieldPaths.navigate(resultObject, step);
				resultField = null;
				if (null == resultObject) {
					return ExtendedField.EMPTY;
				}
			}
		}
		if (null == resultObject || null == resultField) {
			return ExtendedField.EMPTY;
		}
		return of(resultField, resultObject);
	}
}
