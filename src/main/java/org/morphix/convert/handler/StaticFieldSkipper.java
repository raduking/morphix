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

import static org.morphix.convert.FieldHandlerResult.HANDLED;
import static org.morphix.convert.FieldHandlerResult.SKIPPED;

import java.lang.reflect.Modifier;

import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Skips static fields in source.
 *
 * @author Radu Sebastian LAZIN
 */
public final class StaticFieldSkipper extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public StaticFieldSkipper() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		if (Modifier.isStatic(sfo.getModifiers())) {
			return HANDLED;
		}
		return SKIPPED;
	}
}
