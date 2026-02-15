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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.lang.function.Predicates;
import org.morphix.reflection.predicates.MemberPredicates;

/**
 * Utility methods for working with {@link ExtendedField}.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ExtendedFields {

	/**
	 * Returns a list of non static fields filtered by the given filter.
	 *
	 * @param <T> source object type
	 *
	 * @param obj object on which to filter fields
	 * @param filter filter predicate
	 * @return list of filtered fields
	 */
	static <T> List<ExtendedField> findAllNonStatic(final T obj, final Predicate<? super ExtendedField> filter) {
		List<ExtendedField> result = new ArrayList<>();
		// TODO: check duplicate fields (with the same name) in hierarchy (fields/getters)
		Map<String, ExtendedField> nameToFieldMap = new HashMap<>();
		for (Field field : Fields.getAllDeclaredInHierarchy(obj.getClass(), MemberPredicates.isNotStatic())) {
			ExtendedField extendedField = ExtendedField.of(field, obj);
			if (filter.test(extendedField)) {
				nameToFieldMap.computeIfAbsent(field.getName(), key -> {
					result.add(extendedField);
					return extendedField;
				});
			}
		}
		List<Method> getterMethods = Methods.getAllDeclaredInHierarchy(obj.getClass(), MethodType.GETTER.predicate());
		for (Method getterMethod : getterMethods) {
			String fieldName = MethodType.GETTER.getFieldName(getterMethod);
			ExtendedField extendedField = nameToFieldMap.get(fieldName);
			if (null != extendedField) {
				extendedField.setGetterMethod(getterMethod);
			} else {
				ExtendedField getterField = ExtendedField.of(getterMethod, obj);
				if (filter.test(getterField)) {
					nameToFieldMap.computeIfAbsent(fieldName, key -> {
						result.add(getterField);
						return getterField;
					});
				}
			}
		}
		return result;
	}

	/**
	 * Returns a lists of all non static fields.
	 *
	 * @param <T> source object type
	 *
	 * @param obj object on which find fields
	 * @return list of fields
	 */
	static <T> List<ExtendedField> findAllNonStatic(final T obj) {
		return findAllNonStatic(obj, Predicates.acceptAll());
	}
}
