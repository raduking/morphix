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
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A reference to a resource that may potentially leak. It captures the allocation site and provides a mechanism to
 * report leaks.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ResourceLeakReference implements AutoCloseable {

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
	 * A set of class names to ignore when capturing the allocation site. This is used to filter out internal classes
	 * related to the leak detection mechanism itself, such as {@link ResourceLeakReference}, {@link ResourceLeakTracker},
	 * and {@link ResourceLeakDetector}, to avoid cluttering the captured stack trace with irrelevant frames.
	 */
	private static final Set<String> IGNORED_CLASS_NAMES = Set.of(
			ResourceLeakReference.class.getName(),
			ResourceLeakTracker.class.getName(),
			ResourceLeakDetector.class.getName());

	/**
	 * The leak detection level that determines how much information to capture about the allocation site.
	 */
	private final LeakDetectionLevel level;

	/**
	 * The class of the resource that is being tracked for leaks and is used in the log messages when a leak is detected.
	 */
	private final Class<?> referencedClass;

	/**
	 * The stack frames captured at the allocation site of the resource.
	 */
	private final List<StackFrame> allocationSite;

	/**
	 * The reporter used to report leaks for this reference.
	 */
	private final ResourceLeakReporter reporter;

	/**
	 * A flag to indicate whether a leak has already been reported for this reference.
	 */
	private final AtomicBoolean reported = new AtomicBoolean(false);

	/**
	 * A flag to indicate whether the resource has been properly closed. This is used to avoid reporting a leak if the
	 * resource was closed after the leak was detected but before the leak was reported.
	 */
	private volatile boolean closed;

	/**
	 * Creates a new resource leak reference for the given resource class and leak detection level. It captures the
	 * allocation site based on the specified leak detection level that determines how much information to capture about the
	 * allocation site.
	 *
	 * @param level the leak detection level
	 * @param referencedClass the class of the resource being tracked for leaks
	 * @param reporter the reporter used to report leaks for this reference
	 */
	ResourceLeakReference(final LeakDetectionLevel level, final Class<?> referencedClass, final ResourceLeakReporter reporter) {
		this.level = level;
		this.referencedClass = referencedClass;
		this.allocationSite = captureAllocationSite(level);
		this.reporter = reporter;
	}

	/**
	 * Creates a new resource leak reference for the given resource class and leak detection level. It captures the
	 * allocation site based on the specified leak detection level that determines how much information to capture about the
	 * allocation site.
	 *
	 * @param level the leak detection level
	 * @param referencedClass the class of the resource being tracked for leaks
	 * @param reporter the reporter used to report leaks for this reference
	 * @return a new {@link ResourceLeakReference} instance
	 */
	static ResourceLeakReference of(final LeakDetectionLevel level, final Class<?> referencedClass, final ResourceLeakReporter reporter) {
		return new ResourceLeakReference(level, referencedClass, reporter);
	}

	/**
	 * Captures the allocation site of the resource based on the specified leak detection level that determines how much
	 * information to capture about the allocation site.
	 * <ul>
	 * <li>for {@link LeakDetectionLevel#ADVANCED}, it captures a limited number of stack frames (defined by
	 * {@link #ADVANCED_REPORTED_STACK_FRAMES}) to provide context about where the resource was allocated without incurring
	 * too much overhead.</li>
	 * <li>for {@link LeakDetectionLevel#PARANOID}, it captures the entire stack trace to provide the most detailed
	 * information about the allocation site, which can be useful for debugging purposes but may have a significant
	 * performance impact.</li>
	 * <li>for other levels, it does not capture any stack frames to minimize overhead and only report the class name of the
	 * resource.</li>
	 * </ul>
	 *
	 * @param level the leak detection level
	 * @return a list of stack frames representing the allocation site of the resource
	 */
	private static List<StackFrame> captureAllocationSite(final LeakDetectionLevel level) {
		return switch (level) {
			case ADVANCED -> STACK_WALKER.walk(stream -> stream
					.dropWhile(frame -> IGNORED_CLASS_NAMES.contains(frame.getClassName()))
					.limit(ADVANCED_REPORTED_STACK_FRAMES)
					.toList());
			case PARANOID -> STACK_WALKER.walk(stream -> stream
					.dropWhile(frame -> IGNORED_CLASS_NAMES.contains(frame.getClassName()))
					.toList());
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
		if (null != reporter) {
			reporter.reportLeak(this, reason);
		}
	}

	/**
	 * Retrieves the class of the resource that is being tracked for leaks.
	 *
	 * @return the class of the resource being tracked for leaks
	 */
	public Class<?> getReferencedClass() {
		return referencedClass;
	}

	/**
	 * Retrieves the stack frames captured at the allocation site of the resource. The amount of information captured about
	 * the allocation site depends on the leak detection level.
	 *
	 * @return a list of stack frames representing the allocation site of the resource
	 */
	public List<StackFrame> getAllocationSite() {
		return allocationSite;
	}

	/**
	 * Returns the leak detection level for this reference, which determines how much information is captured about the
	 * allocation site and how leaks are reported.
	 *
	 * @return the leak detection level for this reference
	 */
	public LeakDetectionLevel getLevel() {
		return level;
	}

	/**
	 * Constructs the report for a detected leak.
	 *
	 * @param reason the reason for reporting the leak, which will be included in the log message
	 * @return the constructed report for the detected leak
	 */
	String getReport(final String reason) {
		if (null == reporter) {
			return null;
		}
		return reporter.report(this, reason);
	}
}
