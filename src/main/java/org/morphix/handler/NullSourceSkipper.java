package org.morphix.handler;

import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.SKIP;

import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Skips <code>null</code>s in source. This handler should always be the first
 * in the handler chain for best performance.
 *
 * @author Radu Sebastian LAZIN
 */
public final class NullSourceSkipper extends FieldHandler {

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		if (sfo.getFieldValue() == null) {
			return BREAK;
		}
		return SKIP;
	}

}
