package org.morphix.handler;

import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.predicates.ClassPredicates.isA;
import static org.morphix.reflection.predicates.TypePredicates.isAClassAnd;

import java.lang.reflect.Type;
import java.util.function.Predicate;

import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Handles assignment when the destination is <code>char[]</code> and calls
 * {@link #toString()} and then {@link String#toCharArray()} on the source
 * object's field. It skips {@code null} values.
 *
 * @author Radu Sebastian LAZIN
 */
public final class AnyToCharArray extends FieldHandler {

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null != sValue) {
			dfo.setFieldValue(sValue.toString().toCharArray());
		}
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return isAClassAnd(isA(char[].class));
	}

}
