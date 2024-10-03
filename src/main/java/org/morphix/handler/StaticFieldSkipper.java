package org.morphix.handler;

import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.SKIP;

import java.lang.reflect.Modifier;

import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Skips static fields in source.
 *
 * @author Radu Sebastian LAZIN
 */
public final class StaticFieldSkipper extends FieldHandler {

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		if (Modifier.isStatic(sfo.getModifiers())) {
			return BREAK;
		}
		return SKIP;
	}

}
