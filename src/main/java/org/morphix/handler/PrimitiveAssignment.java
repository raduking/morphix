package org.morphix.handler;

import static org.morphix.FieldHandlerResult.CONVERTED;

import java.util.Objects;

import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;
import org.morphix.reflection.Primitives;

/**
 * Handles primitives and their respective auto boxing counterparts.
 *
 * @author Radu Sebastian LAZIN
 */
public final class PrimitiveAssignment extends FieldHandler {

	public static boolean isPrimitiveToClass(final Class<?> clsPrimitive, final Class<?> cls) {
		if (clsPrimitive.isPrimitive()) {
			Class<?> clsBoxed = Primitives.getBoxedClass(clsPrimitive);
			return clsBoxed.isAssignableFrom(cls);
		}
		return false;
	}

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null != sValue) {
			dfo.setFieldValue(sValue);
		}
		return CONVERTED;
	}

	@Override
	public boolean condition(final ConverterField sfo, final ConverterField dfo) {
		Class<?> dClass = dfo.toClass();
		Class<?> sClass = sfo.toClass();
		if (Objects.equals(sClass, dClass)) {
			return false;
		}
		return isPrimitiveToClass(sClass, dClass)
				|| isPrimitiveToClass(dClass, sClass);
	}

}
