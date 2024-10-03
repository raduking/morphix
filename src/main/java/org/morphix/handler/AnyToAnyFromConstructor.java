package org.morphix.handler;

import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.FieldHandlerResult.SKIP;

import java.lang.reflect.Constructor;

import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Converts an object based on the other objects' constructor.
 * <p>
 * Example:
 *
 * <pre>
 * public class A {
 * 	// ...
 * }
 *
 * public class B {
 *
 * 	public B(A a) {
 * 		// ...
 * 	}
 *
 * }
 *
 * A a = new A();
 * B b = new B(a);
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 *
 */
public final class AnyToAnyFromConstructor extends FieldHandler {

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object dValue;
		try {
			Constructor<?> constructor = dfo.toClass().getDeclaredConstructor(sfo.toClass());
			dValue = constructor.newInstance(sfo.getFieldValue());
		} catch (Exception e) {
			return SKIP;
		}
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	@Override
	public boolean condition(final ConverterField sfo, final ConverterField dfo) {
		try {
			dfo.toClass().getDeclaredConstructor(sfo.toClass());
		} catch (NoSuchMethodException e) {
			return false;
		}
		return true;
	}

}
