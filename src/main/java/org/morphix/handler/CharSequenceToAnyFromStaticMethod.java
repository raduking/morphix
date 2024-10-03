package org.morphix.handler;

import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.FieldHandlerResult.SKIP;
import static org.morphix.reflection.predicates.Predicates.not;
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
 * Handles {@link CharSequence} to any class (SomeClass) that has a method like:
 * <p>
 * <p>
 * <code>public static SomeClass SomeClass::someStaticMethod(CharSequence charSequenceParam);</code>
 * <p>
 * <code>public static SomeClass SomeClass::someStaticMethod(String stringParam);</code>
 * <p>
 * <p>
 * and will call the method for setting the destination field (of type
 * SomeClass). Will exclude {@link CharSequence} since it is already handled in
 * {@link DirectAssignment}. Will exclude {@link Enum} since it is already
 * handled in {@link CharSequenceToEnum}.
 *
 * @author Radu Sebastian LAZIN
 */
public final class CharSequenceToAnyFromStaticMethod extends FieldHandler {

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object sValue = sfo.getFieldValue();
		Class<?> dClass = dfo.toClass();
		List<Method> staticConvertMethods = getConverterMethods(dClass, CharSequence.class);
		if (null == sValue && !staticConvertMethods.isEmpty()) {
			return BREAK;
		}
		// find a method in the dClass that can convert the value from a
		// CharSequence value
		for (Method method : staticConvertMethods) {
			Object dValue = Methods.invokeIgnoreAccess(method, dClass, sValue);
			if (null != dValue) {
				dfo.setFieldValue(dValue);
				return CONVERTED;
			}
		}
		return SKIP;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return isCharSequence();
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return not(isEnum().or(isCharSequence()));
	}

}
