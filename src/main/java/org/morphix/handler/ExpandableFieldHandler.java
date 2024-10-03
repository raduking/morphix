package org.morphix.handler;

import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.extra.ConverterCollections.isConvertibleIterableType;
import static org.morphix.extra.ConverterCollections.isConvertibleMapType;
import static org.morphix.reflection.predicates.TypePredicates.isArray;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.Configuration;
import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.annotation.Expandable;
import org.morphix.extra.ConverterCollections;
import org.morphix.reflection.ConverterField;

/**
 * Handles fields annotated with {@link Expandable} in the source. It creates
 * empty Iterables, Arrays and Maps in the destination, for any other fields the
 * destination will be <code>null</code>.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ExpandableFieldHandler extends FieldHandler {

	/**
	 * Map describing the instantiators for each supported expandable field
	 * types.
	 */
	private static final Map<Predicate<Type>, Instantiator<?>> INSTANTIATORS_MAP = new HashMap<>();
	static {
		INSTANTIATORS_MAP.put(isConvertibleIterableType(), ConverterCollections::newCollectionInstance);
		INSTANTIATORS_MAP.put(isConvertibleMapType(), ConverterCollections::newMapInstance);
		INSTANTIATORS_MAP.put(isArray(), ConverterCollections::newEmptyArrayInstance);
	}

	public ExpandableFieldHandler(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		// see if we have specific instantiator first
		boolean fieldSet = false;
		for (Map.Entry<Predicate<Type>, Instantiator<?>> entry : INSTANTIATORS_MAP.entrySet()) {
			if (entry.getKey().test(dfo.getType())) {
				dfo.setFieldValue(entry.getValue().instance(dfo));
				fieldSet = true;
				break;
			}
		}
		if (!fieldSet) {
			dfo.setFieldValue(null);
		}
		return CONVERTED;
	}

	@Override
	protected boolean convert(final ConverterField sfo, final ConverterField dfo) {
		if (condition(sfo, dfo)) {
			FieldHandlerResult result = handle(sfo, dfo);
			return result.isHandled();
		}
		return false;
	}

	@Override
	public boolean condition(final ConverterField sfo, final ConverterField dfo) {
		return getConfiguration().getExpandableFields().shouldNotExpandField(sfo);
	}

	/**
	 * Instance function based on a {@link ConverterField} object.
	 *
	 * @param <T> instance type
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@FunctionalInterface
	public interface Instantiator<T> {
		T instance(ConverterField fop);
	}

}
