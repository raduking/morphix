package org.morphix.strategy;

import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;
import java.util.Map;

import org.morphix.reflection.ConverterField;

/**
 * Map strategy.
 *
 * @author Radu Sebastian LAZIN
 */
public class FieldNameMapStrategy implements Strategy {

	@Override
	public ConverterField find(final Object source, final String sourceFieldName) {
		@SuppressWarnings("unchecked")
		Map<String, ?> sourceMap = (Map<String, ?>) source;
		return of((Field) null, sourceMap.get(sourceFieldName));
	}

}
