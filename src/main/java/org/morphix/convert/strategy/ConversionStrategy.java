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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.lang.function.Predicates;
import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.Fields;
import org.morphix.reflection.MethodType;
import org.morphix.reflection.Methods;
import org.morphix.reflection.predicates.MemberPredicates;

/**
 * Source field finding strategy.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ConversionStrategy {

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
		return findFields(source);
	}

	/**
	 * Returns a stream of fields filtered by the given filter.<br>
	 * All static fields are filtered out by default.
	 *
	 * @param <T> source object type
	 *
	 * @param obj object on which to filter fields
	 * @param filter filter predicate
	 * @return stream of filtered fields
	 */
	static <T> List<ExtendedField> findFields(final T obj, final Predicate<? super ExtendedField> filter) {
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

		List<Method> getterMethods = Methods.getAllDeclaredInHierarchy(obj.getClass(), MethodType.GETTER.getPredicate());
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
	 * Returns a stream of fields.<br>
	 * All static fields are filtered out by default.
	 *
	 * @param <T> source object type
	 *
	 * @param obj object on which find fields
	 * @return stream of fields
	 */
	static <T> List<ExtendedField> findFields(final T obj) {
		return findFields(obj, noFilter());
	}

	/**
	 * Returns no filter predicate.
	 *
	 * @return no filter predicate
	 */
	static Predicate<ExtendedField> noFilter() {
		return Predicates.alwaysTrue();
	}

}
