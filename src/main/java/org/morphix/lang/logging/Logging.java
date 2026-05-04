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
package org.morphix.lang.logging;

import org.morphix.reflection.Constructors;

/**
 * Utility class for logging-related constants and helper methods. This class is not meant to be instantiated and
 * provides common constants and methods that can be used across the logging framework, such as indentation for stack
 * traces and line separators.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Logging {

	/**
	 * Indentation string used for formatting stack traces in exceptions.
	 */
	public static final String INDENT = "\t";

	/**
	 * Line separator string (e.g., "\n" on Unix/Linux, "\r\n" on Windows).
	 */
	public static final String LINE_SEPARATOR = System.lineSeparator();

	/**
	 * Private constructor.
	 */
	private Logging() {
		throw Constructors.unsupportedOperationException();
	}
}
