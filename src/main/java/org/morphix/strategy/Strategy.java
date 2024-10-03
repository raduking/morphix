package org.morphix.strategy;

import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.morphix.reflection.ConverterField;
import org.morphix.reflection.Fields;
import org.morphix.reflection.MethodType;
import org.morphix.reflection.Methods;

/**
 * Source field finding strategy.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Strategy {

	/**
	 * Finds the source field object pair to convert to destination.
	 *
	 * @param source source object
	 * @param sourceFieldName source field name
	 * @return the source field object pair to convert to destination
	 */
	<T> ConverterField find(T source, String sourceFieldName);

	/**
	 * Finds all the source field object pairs to convert to destination.
	 *
	 * @param source source object
	 * @return all the source field object pairs to convert to destination
	 */
	default <T> List<ConverterField> findAll(final T source) {
		return findFields(source, noFilter()).toList();
	}

	/**
	 * Returns a stream of fields filtered by the given filter.<br>
	 * All static fields are filtered out by default.
	 *
	 * @param obj object on which to filter fields
	 * @return stream of filtered fields
	 */
	static <T> Stream<ConverterField> findFields(final T obj, final Predicate<? super ConverterField> filter) {
		// TODO: check fields with same name in hierarchy (fields/getters)
		Map<String, ConverterField> nameToFieldMap = Fields.getDeclaredFieldsInHierarchy(obj).stream()
				.collect(toMap(Field::getName, field -> ConverterField.of(field, obj), (field1, field2) -> field1));

		List<Method> getterMethods = Methods.getDeclaredMethodsInHierarchy(obj.getClass(), MethodType.GETTER.getPredicate());
		for (Method getterMethod : getterMethods) {
			String fieldName = MethodType.GETTER.getFieldName(getterMethod);
			ConverterField converterField = nameToFieldMap.get(fieldName);
			if (null != converterField) {
				converterField.setGetterMethod(getterMethod);
			} else {
				nameToFieldMap.put(fieldName, ConverterField.of(getterMethod, obj));
			}
		}
		return nameToFieldMap.values().stream().filter(noStaticFields().and(filter));
	}

	/**
	 * Returns no filter predicate.
	 *
	 * @return no filter predicate
	 */
	static Predicate<ConverterField> noFilter() {
		return field -> true;
	}

	/**
	 * Returns a predicate for filtering out static fields.
	 *
	 * @return a predicate for filtering out static fields
	 */
	static Predicate<ConverterField> noStaticFields() {
		return field -> !Modifier.isStatic(field.getModifiers());
	}

}
