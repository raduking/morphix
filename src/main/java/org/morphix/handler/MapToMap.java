package org.morphix.handler;

import static org.morphix.Conversion.convertEnvelopedFrom;
import static org.morphix.ConversionFromMap.convertMap;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.extra.ConverterCollections.isConvertibleMapType;
import static org.morphix.extra.ConverterCollections.newMapInstance;
import static org.morphix.reflection.predicates.TypePredicates.isMap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.Configuration;
import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Handles {@link Map} to {@link Map} conversions. This handler requires that
 * the destination is a {@link Map} and it has a getter method.
 * <p>
 * The getter method is used to find the element type within the map so that it
 * can instantiate each element of the destination list.
 * <p>
 * In java the return type of a method must keep it's actual generic arguments
 * at runtime (see {@link Method#getGenericReturnType()}).
 *
 * @author Radu Sebastian LAZIN
 */
public final class MapToMap extends FieldHandler {

	public MapToMap(final Configuration configuration) {
		super(configuration);
	}

	public MapToMap() {
		// empty
	}

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return BREAK;
		}
		Type keyType = getKeyType(dfo);
		if (null == keyType) {
			return BREAK;
		}

		Type valueType = getValueType(dfo);

		Map<?, ?> dValue = convertMap((Map<?, ?>) sValue,
				srcKey -> convertEnvelopedFrom(srcKey, keyType, getConfiguration()),
				srcValue -> convertEnvelopedFrom(srcValue, valueType, getConfiguration()))
						.to(newMapInstance(dfo));
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return isMap();
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return isConvertibleMapType();
	}

	/**
	 * Returns the value class from the destination Map field.
	 * <p>
	 * Example: for <code>Map&lt;String, Integer&gt;</code> the method will
	 * return <code>Class&lt;Integer&gt;</code>
	 *
	 * @param fop field object pair
	 * @return the value type from the destination map field
	 */
	private static <T extends Type> T getValueType(final ConverterField fop) {
		return fop.getGenericReturnType(1);
	}

	/**
	 * Returns the key class from the destination Map field.
	 * <p>
	 * Example: for <code>Map&lt;String, Integer&gt;</code> the method will
	 * return <code>Class&lt;String&gt;</code>
	 *
	 * @param fop field object pair
	 * @return the key type from the destination map field
	 */
	private static <T extends Type> T getKeyType(final ConverterField fop) {
		return fop.getGenericReturnType(0);
	}

}
