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

import java.lang.ref.Cleaner.Cleanable;

/**
 * A tracker for resource leaks that implements {@link AutoCloseable}. It holds a reference to a
 * {@link ResourceLeakReference} and a {@link Cleanable}. When closed, it will close the reference and clean up the
 * associated resources.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ResourceLeakTracker implements AutoCloseable {

	/**
	 * A disabled tracker that does nothing when closed. This can be used when leak detection is turned off to avoid
	 * unnecessary overhead.
	 */
	public static final ResourceLeakTracker DISABLED = new ResourceLeakTracker(null, null, true);

	/**
	 * The reference to the {@link ResourceLeakReference} and the {@link Cleanable} so that we can properly close the
	 * reference and clean up the resources when the tracker is closed. This allows for proper resource management and
	 * ensures that leaks are reported when resources are not properly released and that leaks are detected when they occur.
	 */
	private final ResourceLeakReference reference;

	/**
	 * The {@link Cleanable} is used to register a cleanup action that will be executed when the tracked object is garbage
	 * collected. This allows us to detect leaks even if the object is not explicitly closed, as the cleanup action will
	 * report the leak when it is executed.
	 */
	private final Cleanable cleanable;

	/**
	 * A flag to indicate whether the tracker has been properly closed.
	 */
	private volatile boolean closed;

	/**
	 * Creates a new ResourceLeakTracker with the given reference and cleanable. This constructor is package-private to
	 * restrict access to the ResourceLeakDetector class, which is responsible for creating and managing trackers.
	 *
	 * @param reference the ResourceLeakReference to track
	 * @param cleanable the Cleanable to register for cleanup actions
	 * @param closed whether the tracker is already closed (used for the DISABLED instance)
	 */
	ResourceLeakTracker(final ResourceLeakReference reference, final Cleanable cleanable, final boolean closed) {
		this.reference = reference;
		this.cleanable = cleanable;
		this.closed = closed;
	}

	/**
	 * Creates a new ResourceLeakTracker with the given reference and cleanable. This constructor is package-private to
	 * restrict access to the ResourceLeakDetector class, which is responsible for creating and managing trackers.
	 *
	 * @param reference the ResourceLeakReference to track
	 * @param cleanable the Cleanable to register for cleanup actions
	 */
	ResourceLeakTracker(final ResourceLeakReference reference, final Cleanable cleanable) {
		this(reference, cleanable, false);
	}

	/**
	 * Closes the tracker by closing the reference and cleaning up the associated resources. If the reference is null, this
	 * method does nothing. This allows us to safely close trackers that are disabled or that have already been closed.
	 *
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close() {
		if (closed) {
			return;
		}
		this.closed = true;
		if (null == reference) {
			return;
		}
		reference.close();
		cleanable.clean();
	}

	/**
	 * Returns whether the tracker has been properly closed.
	 *
	 * @return true if the tracker is closed, false otherwise
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * Returns the {@link ResourceLeakReference} associated with this tracker. This can be used for testing purposes to
	 * verify that the reference is properly created and managed by the tracker.
	 *
	 * @return the reference associated with this tracker
	 */
	ResourceLeakReference getReference() {
		return reference;
	}
}
