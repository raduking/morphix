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
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.morphix.lang.function.BinaryOperators;
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
	 * Finds the source field object pair to convert to destination.
	 *
	 * @param <T> source object type
	 *
	 * @param source source object
	 * @param sourceFieldName source field name
	 * @return the source field object pair to convert to destination
	 */
	<T> ExtendedField find(T source, String sourceFieldName);

	/**
	 * Finds all the source field object pairs to convert to destination.
	 *
	 * @param <T> source object type
	 *
	 * @param source source object
	 * @return all the source field object pairs to convert to destination
	 */
	default <T> List<ExtendedField> findAll(final T source) {
		return findFields(source).toList();
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
	static <T> Stream<ExtendedField> findFields(final T obj, final Predicate<? super ExtendedField> filter) {
		// TODO: check duplicate fields (with the same name) in hierarchy (fields/getters)
		Map<String, ExtendedField> nameToFieldMap =
				Fields.getAllDeclaredInHierarchy(obj.getClass(), MemberPredicates.isNotStatic()).stream()
						.collect(Collectors.toMap(Field::getName, field -> ExtendedField.of(field, obj), BinaryOperators.first()));

		List<Method> getterMethods = Methods.getAllDeclaredInHierarchy(obj.getClass(), MethodType.GETTER.getPredicate());
		for (Method getterMethod : getterMethods) {
			String fieldName = MethodType.GETTER.getFieldName(getterMethod);
			ExtendedField converterField = nameToFieldMap.get(fieldName);
			if (null != converterField) {
				converterField.setGetterMethod(getterMethod);
			} else {
				nameToFieldMap.put(fieldName, ExtendedField.of(getterMethod, obj));
			}
		}
		return nameToFieldMap.values().stream().filter(filter);
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
	static <T> Stream<ExtendedField> findFields(final T obj) {
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
