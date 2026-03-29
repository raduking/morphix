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
package org.morphix.lang.leak;

import java.util.logging.Logger;

/**
 * Implementation of {@link ResourceLeakReporter} that logs detected resource leaks using Java's built-in logging
 * framework.
 * <p>
 * The logger is configured to log at the WARNING level, and it provides a simple way to report leaks by logging them,
 * which can be useful for debugging and monitoring purposes.
 *
 * @author Radu Sebastian LAZIN
 */
public class ResourceLeakLogger implements ResourceLeakReporter {

	/**
	 * The name of the logger.
	 */
	public static final String NAME = ResourceLeakLogger.class.getName();

	/**
	 * Logger for reporting resource leaks.
	 */
	private static final Logger LOGGER = Logger.getLogger(NAME);

	/**
	 * Hidden default constructor.
	 */
	protected ResourceLeakLogger() {
		// empty
	}

	/**
	 * Reports a detected resource leak by logging a warning message. The log message includes the class name of the
	 * referenced resource and the provided reason for the leak.
	 * <p>
	 * The log level is set to WARNING to indicate that this is a potential issue that should be investigated.
	 *
	 * @param reference the reference to the resource that is leaking
	 * @param reason the reason for reporting the leak, which can provide additional context about the leak
	 */
	@Override
	public void reportLeak(final ResourceLeakReference reference, final String reason) {
		LOGGER.warning(() -> report(reference, reason));
	}

	/**
	 * Retrieves the singleton instance of {@link ResourceLeakLogger}.
	 *
	 * @return the singleton instance of {@link ResourceLeakLogger}
	 */
	public static ResourceLeakLogger instance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Holder class for the singleton instance of {@link ResourceLeakLogger}.
	 * <p>
	 * This class is loaded on demand when the {@link #instance()} method is called, ensuring that the instance is created
	 * in a thread-safe manner without the need for synchronization.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * The singleton instance of {@link ResourceLeakLogger}.
		 */
		private static final ResourceLeakLogger INSTANCE = new ResourceLeakLogger();
	}
}
