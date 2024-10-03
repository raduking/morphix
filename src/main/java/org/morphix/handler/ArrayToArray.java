package org.morphix.handler;

import static org.morphix.Conversion.convertEnvelopedFrom;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.predicates.TypePredicates.isArray;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.function.Predicate;

import org.morphix.Configuration;
import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Array to Array conversions.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ArrayToArray extends FieldHandler {

	public ArrayToArray(final Configuration configuration) {
		super(configuration);
	}

	public ArrayToArray() {
		// empty
	}

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object[] sValue = (Object[]) sfo.getFieldValue();
		if (null == sValue) {
			return BREAK;
		}

		Class<?> elementClass = dfo.toClass().getComponentType();
		Object[] dValue = (Object[]) Array.newInstance(elementClass, sValue.length);

		for (int i = 0; i < sValue.length; ++i) {
			dValue[i] = convertEnvelopedFrom(sValue[i], elementClass, getConfiguration());
		}
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return isArray();
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return isArray();
	}

}
