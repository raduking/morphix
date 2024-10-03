package org.morphix.handler;

import static org.morphix.FieldHandlerResult.BREAK;

import org.morphix.Configuration;
import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.extra.ExcludedFields;
import org.morphix.reflection.ConverterField;

/**
 * Handles fields that need to be excluded from the source. The fields in the
 * destination will be <code>null</code>.
 *
 * @see ExcludedFields {@link ExcludedFields}
 *
 * @author Radu Sebastian LAZIN
 */
public final class ExcludedFieldHandler extends FieldHandler {

	public ExcludedFieldHandler(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		return BREAK;
	}

	@Override
	public boolean condition(final ConverterField sfo, final ConverterField dfo) {
		return getConfiguration().getExcludedFields().shouldExcludeField(sfo);
	}

	@Override
	protected boolean convert(final ConverterField sfo, final ConverterField dfo) {
		if (condition(sfo, dfo)) {
			FieldHandlerResult result = handle(sfo, dfo);
			return result.isHandled();
		}
		return false;
	}

}
