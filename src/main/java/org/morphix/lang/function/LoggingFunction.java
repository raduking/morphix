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
package org.morphix.lang.function;

/**
 * Functional interface for logging messages with a specific format and arguments.
 * <p>
 * This interface assumes a logging mechanism where messages can be formatted similarly to SLF4J.
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface LoggingFunction {

	/**
	 * Logs a message with the specified format and arguments.
	 *
	 * @param format the log message format.
	 * @param arguments the arguments to include in the log message.
	 */
	void log(String format, Object... arguments);
}
