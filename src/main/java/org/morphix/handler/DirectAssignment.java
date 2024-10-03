package org.morphix.handler;

import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.predicates.Predicates.not;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;
import static org.morphix.reflection.predicates.TypePredicates.isMap;

import java.lang.reflect.Type;
import java.util.function.Predicate;

import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Handles direct assignment that is when the source field is directly
 * assignable to the destination field.
 *
 * @author Radu Sebastian LAZIN
 */
public final class DirectAssignment extends FieldHandler {

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null != sValue) {
			dfo.setFieldValue(sValue);
		}
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return not(isMap());
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return not(isIterable());
	}

	@Override
	public boolean condition(final ConverterField sfo, final ConverterField dfo) {
		Class<?> dClass = dfo.toClass();
		Class<?> sClass = sfo.toClass();
		return dClass.isAssignableFrom(sClass);
	}

}
