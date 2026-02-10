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
package org.morphix.convert;

/**
 * Result from the field handler.
 *
 * @author Radu Sebastian LAZIN
 */
public enum FieldHandlerResult {

	/**
	 * Tells the converter that the handler converted the value successfully.
	 */
	CONVERTED(true),

	/**
	 * Tells the converter to move to the next handler in the chain.
	 */
	SKIP(false),

	/**
	 * Tells the converter to break the chain and consider the value handled but not converted.
	 */
	BREAK(true);

	/**
	 * handled flag
	 */
	private final boolean handled;

	/**
	 * Constructor with handled parameter.
	 *
	 * @param handled handled flag
	 */
	FieldHandlerResult(final boolean handled) {
		this.handled = handled;
	}

	/**
	 * Returns true if the field is handled, false otherwise.
	 *
	 * @return true if the field is handled, false otherwise
	 */
	public boolean isHandled() {
		return handled;
	}

}
