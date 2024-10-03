package org.morphix.handler;

import static org.morphix.Conversion.convertFrom;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.FieldHandlerResult.SKIP;
import static org.morphix.function.InstanceFunction.to;
import static org.morphix.reflection.predicates.Predicates.allOf;
import static org.morphix.reflection.predicates.Predicates.not;
import static org.morphix.reflection.predicates.TypePredicates.isArray;
import static org.morphix.reflection.predicates.TypePredicates.isCharSequence;
import static org.morphix.reflection.predicates.TypePredicates.isIterable;
import static org.morphix.reflection.predicates.TypePredicates.isMap;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.Configuration;
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
public final class AnyToAny extends FieldHandler {

	/**
	 * Predicate list for conditions that must be false for both input classes
	 * source or destination for this handler to begin handling the objects.
	 */
	private static final Predicate<Type> HANDLER_CONSTRAINT = allOf(
			not(isIterable()),
			not(isMap()),
			not(isArray()));

	public AnyToAny() {
		// empty
	}

	public AnyToAny(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return SKIP;
		}
		Object dValue = dfo.getFieldValue();
		dValue = null != dValue
				? convertFrom(sValue, to(dValue), getConfiguration())
				: convertFrom(sValue, dfo.toClass(), getConfiguration());
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return not(isCharSequence())
				.and(HANDLER_CONSTRAINT);
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return HANDLER_CONSTRAINT;
	}

}
