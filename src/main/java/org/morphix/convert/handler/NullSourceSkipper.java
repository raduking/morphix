/*
 * Copyright 2025 the original author or authors.
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
import static org.morphix.convert.FieldHandlerResult.SKIP;

import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Skips <code>null</code>s in source. This handler should always be the first in the handler chain for best
 * performance.
 *
 * @author Radu Sebastian LAZIN
 */
public final class NullSourceSkipper extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public NullSourceSkipper() {
		// empty
	}

	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		if (sfo.getFieldValue() == null) {
			return BREAK;
		}
		return SKIP;
	}

}
