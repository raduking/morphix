/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.convert.handler;

import static org.morphix.convert.FieldHandlerResult.BREAK;

import org.morphix.convert.Configuration;
import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.convert.extras.ExcludedFields;
import org.morphix.reflection.ExtendedField;

/**
 * Handles fields that need to be excluded from the source. The fields in the destination will be <code>null</code>.
 *
 * @see ExcludedFields
 *
 * @author Radu Sebastian LAZIN
 */
public final class ExcludedFieldHandler extends FieldHandler {

	/**
	 * Constructor for custom configuration.
	 *
	 * @param configuration configuration object
	 */
	public ExcludedFieldHandler(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		return BREAK;
	}

	@Override
	public boolean condition(final ExtendedField sfo, final ExtendedField dfo) {
		return getConfiguration().getExcludedFields().shouldExcludeField(sfo);
	}

	@Override
	protected boolean convert(final ExtendedField sfo, final ExtendedField dfo) {
		if (condition(sfo, dfo)) {
			FieldHandlerResult result = handle(sfo, dfo);
			return result.isHandled();
		}
		return false;
	}
}
