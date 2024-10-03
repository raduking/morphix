package org.morphix.handler;

import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.predicates.TypePredicates.isCharSequence;
import static org.morphix.reflection.predicates.TypePredicates.isEnum;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;
import org.morphix.reflection.Methods;

/**
 * Handles any object to {@link Enum} fields via public static final method. It
 * will call the default 'valueOf' method last when another method is present
 * with the same signature.
 *
 * @author Radu Sebastian LAZIN
 */
public final class CharSequenceToEnum extends FieldHandler {

	private static final String VALUE_OF_METHOD_NAME = "valueOf";

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return BREAK;
		}
		Class<?> dClass = dfo.toClass();
		// find a method in the dClass (Enum class) that can convert the
		// value from a CharSequence / String value
		List<Method> methods = getConverterMethods(dClass, CharSequence.class);
		Method valueOfMethod = null;
		for (Method method : methods) {
			if (VALUE_OF_METHOD_NAME.equals(method.getName())) {
				valueOfMethod = method;
			} else {
				Object dValue = Methods.invokeIgnoreAccess(method, dClass, sValue);
				if (null != dValue) {
					dfo.setFieldValue(dValue);
					return CONVERTED;
				}
			}
		}
		// invoke the 'valueOf' method last
		Object dValue = Methods.invokeIgnoreAccess(valueOfMethod, dClass, sValue);
		// dValue will never be null because 'valueOf' will fail for wrong values
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return isCharSequence();
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return isEnum();
	}

}
