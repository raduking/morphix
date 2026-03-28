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

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A reference to a resource that may potentially leak. It captures the allocation site and provides a mechanism to
 * report leaks.
 *
 * @author Radu Sebastian LAZIN
 */
final class ResourceLeakReference implements AutoCloseable {

	/**
	 * Logger for reporting resource leaks. The logger is configured to log at the WARNING level, and it includes the class
	 * name of the referenced resource in the log messages.
	 */
	private static final Logger LOGGER = Logger.getLogger(ResourceLeakReference.class.getName());

	/**
	 * StackWalker instance used to capture the allocation site of the resource. It is configured with the
	 * {@link Option#RETAIN_CLASS_REFERENCE} option to allow capturing class references in the stack frames.
	 */
	private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

	/**
	 * Number of stack frames to capture for the allocation site when the leak detection level is
	 * {@link LeakDetectionLevel#ADVANCED}.
	 */
	private static final int ADVANCED_REPORTED_STACK_FRAMES = 10;

	/**
	 * The leak detection level that determines how much information to capture about the allocation site.
	 */
	private final LeakDetectionLevel level;

	/**
	 * The class of the resource that is being tracked for leaks and is used in the log messages when a leak is detected.
	 */
	private final Class<?> referencedClass;

	/**
	 * The stack frames captured at the allocation site of the resource. The number of frames captured depends on the leak
	 * detection level. For {@link LeakDetectionLevel#ADVANCED}, it captures a limited number of frames, while for
	 * {@link LeakDetectionLevel#PARANOID}, it captures the entire stack trace.
	 */
	private final List<StackFrame> allocationSite;

	/**
	 * A flag to indicate whether a leak has already been reported for this reference. This is used to ensure that we only
	 * report a leak once, even if the same resource is tracked multiple times or if the leak is detected multiple times
	 * (e.g. due to multiple references to the same object).
	 */
	private final AtomicBoolean reported = new AtomicBoolean(false);

	/**
	 * A flag to indicate whether the resource has been properly closed. This is used to avoid reporting a leak if the
	 * resource was closed after the leak was detected but before the leak was reported.
	 */
	private volatile boolean closed;

	/**
	 * Creates a new resource leak reference for the given resource class and leak detection level. It captures the
	 * allocation site based on the specified leak detection level.
	 *
	 * @param level the leak detection level that determines how much information to capture about the allocation site
	 * @param referencedClass the class of the resource being tracked for leaks
	 */
	ResourceLeakReference(final LeakDetectionLevel level, final Class<?> referencedClass) {
		this.level = level;
		this.referencedClass = referencedClass;
		this.allocationSite = captureAllocationSite(level);
	}

	/**
	 * Captures the allocation site of the resource based on the specified leak detection level. For
	 * {@link LeakDetectionLevel#ADVANCED}, it captures a limited number of stack frames, while for
	 * {@link LeakDetectionLevel#PARANOID}, it captures the entire stack trace.
	 *
	 * @param level the leak detection level that determines how much information to capture about the allocation site
	 * @return a list of stack frames representing the allocation site of the resource
	 */
	private static List<StackFrame> captureAllocationSite(final LeakDetectionLevel level) {
		return switch (level) {
			case ADVANCED -> STACK_WALKER.walk(stackFrame -> stackFrame.limit(ADVANCED_REPORTED_STACK_FRAMES).toList());
			case PARANOID -> STACK_WALKER.walk(Stream::toList);
			default -> Collections.emptyList();
		};
	}

	/**
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close() {
		closed = true;
	}

	/**
	 * Checks if the resource has been properly closed.
	 *
	 * @return {@code true} if the resource has been closed, {@code false} otherwise
	 */
	boolean isClosed() {
		return closed;
	}

	/**
	 * Checks if a leak has already been reported for this reference.
	 *
	 * @return {@code true} if a leak has already been reported, {@code false} otherwise
	 */
	boolean isReported() {
		return reported.get();
	}

	/**
	 * Reports a leak for this reference if it has not been reported yet and if the resource has not been closed. The
	 * provided reason is included in the log message to give more context about the leak.
	 *
	 * @param reason the reason for reporting the leak, which will be included in the log message
	 */
	void reportLeak(final String reason) {
		if (closed || !reported.compareAndSet(false, true)) {
			return;
		}

		LOGGER.warning(() -> message(reason));
	}

	/**
	 * Constructs the log message for a detected leak, including the class name of the referenced resource, the provided
	 * reason, and optionally the stack trace of the allocation site based on the leak detection level. For
	 * {@link LeakDetectionLevel#ADVANCED} and {@link LeakDetectionLevel#PARANOID}, the stack trace is included in the log
	 * message to provide more context about where the resource was allocated.
	 *
	 * @param reason the reason for reporting the leak, which will be included in the log message
	 * @return the constructed log message for the detected leak
	 */
	protected String message(final String reason) {
		StringBuilder sb = new StringBuilder();
		sb.append("LEAK DETECTED: ")
				.append(referencedClass.getName())
				.append(" was not closed (")
				.append(reason)
				.append(")");

		if (level.ordinal() >= LeakDetectionLevel.ADVANCED.ordinal()) {
			String eol = System.lineSeparator();
			sb.append(eol);
			for (StackFrame frame : allocationSite) {
				sb.append("  at ").append(frame).append(eol);
			}
		}
		return sb.toString();
	}
}
