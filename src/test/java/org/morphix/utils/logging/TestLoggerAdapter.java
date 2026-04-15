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
package org.morphix.utils.logging;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.morphix.lang.Messages;
import org.morphix.lang.function.LoggerAdapter;

/**
 * Test implementation of {@link LoggerAdapter} that captures log messages in memory for verification in tests.
 *
 * @author Radu Sebastian LAZIN
 */
public final class TestLoggerAdapter implements LoggerAdapter {

	/**
	 * Map that stores log messages categorized by their logging level. Each logging level maps to a list of messages logged
	 * at that level.
	 */
	private final Map<LoggingLevel, List<String>> debugMessages = new EnumMap<>(LoggingLevel.class);

	/**
	 * @see LoggerAdapter#log(LoggingLevel, String, Object...)
	 */
	@Override
	public void log(final LoggingLevel level, final String message, final Object... args) {
		debugMessages.computeIfAbsent(level, k -> new ArrayList<>()).add(Messages.message(message, args));
	}

	/**
	 * Returns the map of logged messages categorized by their logging level. Each logging level maps to a list of messages
	 * logged at that level.
	 *
	 * @return the map of logged messages categorized by logging level
	 */
	public Map<LoggingLevel, List<String>> getMessages() {
		return debugMessages;
	}

	/**
	 * Returns the list of messages logged at the specified logging level. If no messages were logged at that level, an empty
	 * list is returned.
	 *
	 * @param level the logging level for which to retrieve messages
	 * @return the list of messages logged at the specified logging level, or an empty list if none were logged
	 */
	public List<String> getMessages(final LoggingLevel level) {
		return debugMessages.getOrDefault(level, List.of());
	}
}
