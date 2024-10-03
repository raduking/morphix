package org.morphix.handler;

import static org.morphix.Conversion.convertEnvelopedFrom;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.extra.ConverterCollections.newCollectionInstance;
import static org.morphix.handler.IterableToIterable.getIterableElementType;
import static org.morphix.reflection.predicates.TypePredicates.isArray;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Predicate;

import org.morphix.Configuration;
import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Array to Iterable conversions.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ArrayToIterable extends FieldHandler {

	public ArrayToIterable(final Configuration configuration) {
		super(configuration);
	}

	public ArrayToIterable() {
		// empty
	}

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object[] sValue = (Object[]) sfo.getFieldValue();
		if (null == sValue) {
			return BREAK;
		}
		Type elementType = getIterableElementType(dfo);
		if (null == elementType) {
			return BREAK;
		}

		Collection<?> dValue = newCollectionInstance(dfo);
		for (Object element : sValue) {
			dValue.add(convertEnvelopedFrom(element, elementType, getConfiguration()));
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
		return isIterable();
	}

}
