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
import org.morphix.reflection.predicates.MethodPredicates;

/**
 * Utility methods for working with {@link ExtendedField}.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ExtendedFields {

	/**
	 * Returns a list of non-static fields filtered by the given filter.
	 * <p>
	 * Declared fields come first, in the order given by {@link Class#getDeclaredFields()}, followed by the entries that
	 * exist only as getters. The order of the result is <strong>not</strong> specified by the JDK and can differ between
	 * JVMs and platforms, so look entries up by name instead of by index, and never assert on the position of an entry.
	 * <p>
	 * A field and a getter are two views of the same entry when they share a name. Only one entry is created per name, and
	 * the getter attached to it is the one declared in the most derived class, since the getters are walked from the most
	 * to the least derived declaration. Note that a field rejected by the filter does not prevent a getter-only entry from
	 * being added for the same name, because the filter is applied again to that entry, which has no field at all.
	 *
	 * @param <T> source object type
	 *
	 * @param obj object on which to filter fields
	 * @param filter filter predicate, applied to each entry, field-backed or getter-only
	 * @return list of filtered fields
	 */
	static <T> List<ExtendedField> findAllNonStatic(final T obj, final Predicate<? super ExtendedField> filter) {
		List<ExtendedField> result = new ArrayList<>();
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
		List<Method> getterMethods = Methods.getAllDeclaredInHierarchy(obj.getClass(), getterMethodPredicate());
		for (Method getterMethod : getterMethods) {
			String fieldName = MethodType.GETTER.getFieldName(getterMethod);
			ExtendedField extendedField = nameToFieldMap.get(fieldName);
			if (null != extendedField) {
				// getters are returned from the most to the least derived declaration, so only the first one found is kept
				if (null == extendedField.getGetterMethod()) {
					extendedField.setGetterMethod(getterMethod);
				}
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
	 * Returns a lists of all non-static fields.
	 *
	 * @param <T> source object type
	 *
	 * @param obj object on which find fields
	 * @return list of fields, in an order that is not specified by the JDK, see
	 * {@link #findAllNonStatic(Object, java.util.function.Predicate)}
	 */
	static <T> List<ExtendedField> findAllNonStatic(final T obj) {
		return findAllNonStatic(obj, Predicates.acceptAll());
	}

	/**
	 * Returns the predicate for the getter methods considered when looking for fields: instance, non synthetic getters
	 * only. Synthetic methods, such as the bridge methods generated for covariant return types or for implementations of
	 * generic methods, would otherwise shadow the real getter declaration, making the result depend on the unspecified
	 * order in which {@link Class#getDeclaredMethods()} returns them. Static getters are excluded as well, for the same
	 * reason the static fields are.
	 *
	 * @return predicate selecting the getter methods to be considered
	 */
	private static Predicate<Method> getterMethodPredicate() {
		return Predicates.allOf(
				MethodType.GETTER.predicate(),
				MemberPredicates.isNotStatic(),
				MethodPredicates.isNotSynthetic());
	}
}
