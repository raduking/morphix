package org.morphix.strategy;

import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.morphix.reflection.ConverterField;
import org.morphix.reflection.Fields;

/**
 * Finds the field in the source by path.
 *
 * @author Radu Sebastian LAZIN
 */
public class PathStrategy implements Strategy {

	@Override
	public ConverterField find(final Object source, final String sourceFieldName) {
		return getConverterFieldByPathInHierarchy(source, sourceFieldName);
	}

	/**
	 * Returns the field and the object it corresponds to given its path.
	 *
	 * @param obj object on which to search
	 * @param path path to the wanted field (object)
	 * @return field object pair
	 */
	public static <T> ConverterField getConverterFieldByPathInHierarchy(final T obj, final String path) {
		String[] fieldNames = path.split("\\.");

		Field resultField = null;
		Object resultObject = obj;
		Class<?> currentClass = resultObject.getClass();

		for (String fieldName : fieldNames) {
			if (null != resultField && null != resultObject) {
				resultObject = Fields.getIgnoreAccess(resultObject, resultField);
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
