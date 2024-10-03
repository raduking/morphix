package org.morphix.strategy;

import java.util.Optional;

import org.morphix.annotation.Src;
import org.morphix.reflection.ConverterField;

/**
 * Simple strategy which finds the field in the source by its name.
 *
 * @author Radu Sebastian LAZIN
 */
public class BasicNameStrategy implements Strategy {

	@Override
	public ConverterField find(final Object source, final String sourceFieldName) {
		Optional<ConverterField> sField = findFieldByName(source, sourceFieldName);
		return sField.orElse(ConverterField.EMPTY);
	}

	/**
	 * Finds a field by name. Field name will never be null since the converter
	 * only searches through existing fields on destination or {@link Src}
	 * annotation parameters which can only be constants which cannot be null.
	 *
	 * @param obj object on which to find the field
	 * @param fieldName field name
	 * @return optional with field information
	 */
	protected static <T> Optional<ConverterField> findFieldByName(final T obj, final String fieldName) {
		return Strategy.findFields(obj,
				converterField -> fieldName.equals(converterField.getName())).findFirst();
	}

}
