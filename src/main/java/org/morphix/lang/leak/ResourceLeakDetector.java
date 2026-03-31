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

import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;

/**
 * Utility class for tracking resource leaks. This class holds all the tracked references and the {@link Cleaner}. It
 * provides methods for tracking objects and reporting leaks when they are garbage collected or when the JVM shuts down.
 * <p>
 * This is to be used in conjunction with the {@link ResourceLeakTracker} to track resources that need to be closed.
 * <p>
 * <b>Usage:</b>
 *
 * <pre>
 * class MyResource implements AutoCloseable {
 *
 * 	private final ResourceLeakTracker leakTracker = ResourceLeakDetector.track(this);
 *
 * 	&#64;Override
 * 	public void close() {
 * 		leakTracker.close();
 * 	}
 * }
 * </pre>
 *
 * or:
 *
 * <pre>
 * AutoCloseable resource = ...;
 * try (ResourceLeakTracker leakTracker = ResourceLeakDetector.track(resource)) {
 *    // use resource
 *    // leakTracker will automatically report if the resource is not closed properly
 * }
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
public final class ResourceLeakDetector {

	/**
	 * The name of the cleaner thread for easier identification in thread dumps or monitoring tools.
	 */
	public static final String CLEANER_THREAD_NAME = "morphix-leak-detector-cleaner";

	/**
	 * The set that keeps track of all active references. This avoids reporting the same leak multiple times if the same
	 * object is tracked multiple times (e.g. in case of multiple references to the same object).
	 */
	private static final Set<ResourceLeakReference> REFERENCES = ConcurrentHashMap.newKeySet();

	/**
	 * We use a single cleaner for all tracked objects. This allows us to avoid creating a new cleaner for each tracked
	 * object, which would be expensive. The cleaner will run in a separate thread and will report leaks when the tracked
	 * objects are garbage collected.
	 */
	private static final Cleaner CLEANER = Cleaner.create(ResourceLeakDetector::newCleanerThread);

	/**
	 * Static initializer to add the shutdown hook. This ensures that the shutdown hook is added as soon as the class is
	 * loaded, which allows us to report leaks on JVM shutdown even if no objects are tracked.
	 */
	static {
		addShutdownHook();
	}

	/**
	 * Adds a shutdown hook to report any leaks that were not reported by the cleaner. This is useful for cases where the
	 * cleaner thread is not able to run (e.g. because the JVM is shutting down) or for cases where the tracked objects are
	 * not garbage collected (e.g. because they are still referenced by other objects).
	 */
	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(newLeakReporterThread());
	}

	/**
	 * Tracks the given object for resource leaks. If the leak detection level is disabled, this method returns a no-op
	 * tracker.
	 *
	 * @param object the object to track for leaks
	 * @return a leak tracker that can be used to close the tracked object and report leaks
	 */
	public static ResourceLeakTracker track(final Object object) {
		return track(object, null);
	}

	/**
	 * Tracks the given object for resource leaks. If the leak detection level is disabled, this method returns a no-op
	 * tracker.
	 *
	 * @param object the object to track for leaks
	 * @param hint the hint to include in the leak report, which can provide additional context about the leak
	 * @return a leak tracker that can be used to close the tracked object and report leaks
	 */
	public static ResourceLeakTracker track(final Object object, final String hint) {
		return track(object, hint, ResourceLeakLogger.instance());
	}

	/**
	 * Tracks the given object for resource leaks. If the leak detection level is disabled, this method returns a no-op
	 * tracker.
	 * <p>
	 * TODO: implement fluent API for configuring the tracker.
	 *
	 * @param object the object to track for leaks
	 * @param hint the hint to include in the leak report, which can provide additional context about the leak
	 * @param reporter the reporter to use for reporting leaks for this object
	 * @return a leak tracker that can be used to close the tracked object and report leaks
	 */
	@SuppressWarnings("resource")
	public static ResourceLeakTracker track(final Object object, final String hint, final ResourceLeakReporter reporter) {
		Objects.requireNonNull(object, "tracked object cannot be null");
		LeakDetectionLevel level = LeakDetectionLevel.current();
		if (LeakDetectionLevel.DISABLED == level) {
			return ResourceLeakTracker.DISABLED;
		}

		ResourceLeakReference reference = ResourceLeakReference.of(level, object.getClass(), reporter);
		REFERENCES.add(reference);
		Cleanable cleanable = CLEANER.register(object, () -> reportLeak(reference, message(hint, "GC without close()")));

		return new ResourceLeakTracker(reference, cleanable);
	}

	/**
	 * Constructs the message to include in the leak report based on the provided hint and reason. If the hint is not empty,
	 * it is included in the message for additional context about the leak.
	 *
	 * @param hint the hint to include in the message, which can provide additional context about the leak
	 * @param reason the reason for reporting the leak, which can provide additional context about the leak
	 * @return a formatted message to include in the leak report
	 */
	public static String message(final String hint, final String reason) {
		return (JavaObjects.isEmpty(hint) ? "" : hint + " - ") + reason;
	}

	/**
	 * Untracks the given reference. This is called when a tracked object is closed to remove it from the set of active
	 * references and avoid reporting it as a leak.
	 *
	 * @param reference the reference to untrack
	 */
	static void untrack(final ResourceLeakReference reference) {
		REFERENCES.remove(reference);
	}

	/**
	 * Creates a new cleaner virtual thread with the specified runnable. This method is used to create the cleaner thread
	 * for the {@link Cleaner}.
	 *
	 * @param runnable the runnable to run in the cleaner thread
	 * @return a new thread with the specified runnable and name
	 */
	static Thread newCleanerThread(final Runnable runnable) {
		return Thread.ofVirtual().name(CLEANER_THREAD_NAME).unstarted(runnable);
	}

	/**
	 * Creates a new thread that will report leaks when run. This thread will be added as a shutdown hook in the static
	 * initializer.
	 *
	 * @return a new thread that will report leaks on JVM shutdown
	 */
	static Thread newLeakReporterThread() {
		// use platform thread, shutdown hooks must not rely on virtual thread scheduler
		return new Thread(() -> reportLeaks(REFERENCES, "JVM shutdown"));
	}

	/**
	 * Reports a leak for the given reference with the specified message. After a leak is reported for the reference, it is
	 * untracked to avoid reporting it again in the future.
	 *
	 * @param reference the reference for which to report the leak
	 * @param message the message to include in the leak report
	 */
	static void reportLeak(final ResourceLeakReference reference, final String message) {
		reference.reportLeak(message);
		untrack(reference);
	}

	/**
	 * Reports leaks for all the given references with the specified message. After a leak is reported for a reference, it
	 * is untracked to avoid reporting it again in the future.
	 *
	 * @param references the set of references for which to report leaks
	 * @param message the message to include in the leak reports
	 */
	static void reportLeaks(final Set<ResourceLeakReference> references, final String message) {
		Set<ResourceLeakReference> refs = REFERENCES == references ? Set.copyOf(references) : references;
		for (ResourceLeakReference reference : refs) {
			reportLeak(reference, message);
		}
	}

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private ResourceLeakDetector() {
		throw Constructors.unsupportedOperationException();
	}
}
