package org.morphix.handler;

import static org.morphix.Conversion.convertEnvelopedFrom;
import static org.morphix.ConversionFromIterable.convertIterable;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.FieldHandlerResult.SKIP;
import static org.morphix.extra.ConverterCollections.newCollectionInstance;
import static org.morphix.reflection.predicates.Predicates.allOf;
import static org.morphix.reflection.predicates.Predicates.not;
import static org.morphix.reflection.predicates.TypePredicates.isArray;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;
import static org.morphix.reflection.predicates.TypePredicates.isMap;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.Conversion;
import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Handles any object to any conversion. It calls
 * {@link Conversion#convertFrom(Object, Class)} on the source field to convert
 * it to a destination field based on the destination class.
 * <p>
 * This handler will skip {@link Iterable}s, {@link Map}s from both source and
 * destination and {@link CharSequence}s from source.
 *
 * @author Radu Sebastian LAZIN
 */
public final class AnyToIterable extends FieldHandler {

	public AnyToIterable() {
		// empty
	}

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return SKIP;
		}
		Type elementType = IterableToIterable.getIterableElementType(dfo);
		if (null == elementType) {
			return BREAK;
		}
		Iterable<?> sIterable = Collections.singletonList(sValue);
		Iterable<?> dValue = convertIterable(sIterable,
				src -> convertEnvelopedFrom(src, elementType, getConfiguration()))
						.to(newCollectionInstance(dfo));
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return allOf(
				not(isIterable()),
				not(isMap()),
				not(isArray()));
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return allOf(
				isIterable(),
				not(isMap()),
				not(isArray()));
	}

}
