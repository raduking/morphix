package org.morphix.handler;

import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.FieldHandlerResult.SKIP;
import static org.morphix.reflection.predicates.Predicates.not;
import static org.morphix.reflection.predicates.TypePredicates.isCharSequence;

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
 * <code>public static SomeClass SomeClass::someStaticMethod(SourceClass sourceClassParam);</code>
 * <p>
 * <p>
 * and will call the method for setting the destination field (of type
 * SomeClass). Will exclude {@link CharSequence} since it is already handled in
 * {@link DirectAssignment}. Will exclude {@link Enum} since it is already
 * handled in {@link CharSequenceToEnum}.
 *
 * @author Radu Sebastian LAZIN
 */
public final class AnyToAnyFromStaticMethod extends FieldHandler {

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object sValue = sfo.getFieldValue();
		Class<?> dClass = dfo.toClass();
		Class<?> sClass = sfo.toClass();
		List<Method> staticConvertMethods = getConverterMethods(dClass, sClass);
		if (null == sValue && !staticConvertMethods.isEmpty()) {
			return BREAK;
		}
		// find a method in the dClass that can convert the value from a
		// source class value
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
		return not(isCharSequence());
	}

}
