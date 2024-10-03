package org.morphix.handler;

import static org.morphix.Conversion.convertEnvelopedFrom;
import static org.morphix.ConversionFromIterable.convertIterable;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.predicates.TypePredicates.isArray;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Predicate;

import org.morphix.Configuration;
import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Handles {@link Iterable} to java array conversions. This handler requires
 * that the destination is an array.
 *
 * @author Radu Sebastian LAZIN
 */
public final class IterableToArray extends FieldHandler {

	public IterableToArray(final Configuration configuration) {
		super(configuration);
	}

	public IterableToArray() {
		// empty
	}

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return BREAK;
		}
		Object[] dValue = (Object[]) dfo.getFieldValue();
		if (null != dfo.getFieldValue() && dValue.length > 0) {
			return BREAK;
		}
		Class<?> arrayElementClass = dfo.toClass().getComponentType();
		if (null == arrayElementClass) {
			return BREAK;
		}

		Collection<?> arrayList = convertIterable((Iterable<?>) sValue,
				src -> convertEnvelopedFrom(src, arrayElementClass, getConfiguration()))
						.toList();
		dValue = (Object[]) Array.newInstance(arrayElementClass, arrayList.size());

		dfo.setFieldValue(arrayList.toArray(dValue));

		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return isIterable();
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return isArray();
	}

}
