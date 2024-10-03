package org.morphix.handler;

import static org.morphix.Conversion.convertEnvelopedFrom;
import static org.morphix.FieldHandlerResult.CONVERTED;

import java.util.Map;

import org.morphix.Configuration;
import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Any from {@link Map} field handler.
 *
 * @author Radu Sebastian LAZIN
 */
public final class AnyFromMap extends FieldHandler {

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		if (sfo.hasObject()) {
			Object value = convertEnvelopedFrom(sfo.getObject(), dfo.getType(), Configuration.defaultConfiguration());
			dfo.setFieldValue(value);
		}
		return CONVERTED;
	}

}
