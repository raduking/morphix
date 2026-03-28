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

import java.lang.StackWalker.StackFrame;

/**
 * Interface for reporting resource leaks. Implementations of this interface can be used to customize how leaks are
 * reported, for example by logging them, sending them to a monitoring system, or displaying them in a user interface.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ResourceLeakReporter {

	/**
	 * Reports a detected resource leak. The implementation of this method should handle the reporting of the leak, which
	 * may include logging the leak, sending it to a monitoring system, or displaying it in a user interface. The provided
	 * reason can be used to give more context about why the leak is being reported.
	 *
	 * @param reference the reference to the resource that is leaking
	 * @param reason the reason for reporting the leak, which can provide additional context about the leak
	 */
	void reportLeak(ResourceLeakReference reference, String reason);

	/**
	 * Constructs the report for a detected leak, including the class name of the referenced resource, the provided reason,
	 * and optionally the stack trace of the allocation site based on the leak detection level.
	 * <p>
	 * For {@link LeakDetectionLevel#ADVANCED} and {@link LeakDetectionLevel#PARANOID}, the stack trace is included in the
	 * log message to provide more context about where the resource was allocated.
	 * <p>
	 * This method can be overridden by implementations to customize the format of the report, but the default
	 * implementation provides a standard format that includes the class name, reason, and optionally the stack trace for
	 * advanced leak detection levels.
	 *
	 * @param reference the reference to the resource that is leaking
	 * @param reason the reason for reporting the leak, which will be included in the log message
	 * @return the constructed log message for the detected leak
	 */
	default String report(final ResourceLeakReference reference, final String reason) {
		StringBuilder sb = new StringBuilder();
		sb.append("LEAK DETECTED: ")
				.append(reference.getReferencedClass().getName())
				.append(" was not closed (")
				.append(reason)
				.append(")");

		if (reference.getLevel().ordinal() >= LeakDetectionLevel.ADVANCED.ordinal()) {
			String eol = System.lineSeparator();
			sb.append(eol);
			for (StackFrame frame : reference.getAllocationSite()) {
				sb.append("  at ").append(frame).append(eol);
			}
		}
		return sb.toString();
	}
}
