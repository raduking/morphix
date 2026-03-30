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
package org.morphix.lang.resource;

import java.util.Objects;
import java.util.function.Consumer;

import org.morphix.lang.Unchecked;
import org.morphix.lang.function.ThrowingFunction;
import org.morphix.lang.leak.ResourceLeakDetector;
import org.morphix.lang.leak.ResourceLeakTracker;

/**
 * An immutable wrapper for managing the life-cycle of {@link AutoCloseable} resources with scope control.
 * <p>
 * This class provides controlled access to resources while ensuring proper cleanup based on the managed flag. It
 * supports both managed (automatically closed) and unmanaged (manually closed) resource patterns.
 * <p>
 * Instances do not change management state; operations that adjust management return new {@link ScopedResource}
 * instances.
 * <p>
 * Ownership is decided when the {@code ScopedResource} is created. Code that receives a {@code ScopedResource} should
 * not try to determine ownership itself and must always call {@link #closeIfManaged()} when the resource is no longer
 * needed.
 *
 * <b>Usage Pattern</b>
 *
 * <pre>
 * ScopedResource&lt;Client&gt; scoped = ...;
 * try {
 *     Resource resource = scoped.unwrap();
 *     // use resource
 * } finally {
 *     scoped.closeIfManaged();
 * }
 * </pre>
 *
 * or:
 *
 * <pre>
 * try (ScopedResource&lt;Client&gt; scoped = ...) {
 *    Resource resource = scoped.unwrap();
 *    // use resource
 * }
 * </pre>
 *
 * The resource will be automatically closed if it is managed by the scoped resource, otherwise it will remain open
 * <p>
 * This pattern ensures that resources created internally are properly closed, while externally provided resources are
 * never accidentally closed.
 *
 * @param <T> the type of the {@link AutoCloseable} resource being wrapped
 *
 * @author Radu Sebastian LAZIN
 */
public class ScopedResource<T extends AutoCloseable> implements AutoCloseable {

	/**
	 * The wrapped AutoCloseable resource.
	 */
	private final T resource;

	/**
	 * Flag indicating whether this wrapper manages the resource's life-cycle.
	 */
	private final boolean managed;

	/**
	 * Leak tracker for detecting resource leaks.
	 */
	private final ResourceLeakTracker leakTracker;

	/**
	 * Constructs a new ScopedResource instance. Use factory methods {@link #managed(AutoCloseable)}, or
	 * {@link #unmanaged(AutoCloseable)} to construct managed or unmanaged instances respectively.
	 *
	 * @param resource the resource to wrap (must not be null)
	 * @param management whether the wrapper should manage the life-cycle of the resource
	 * @throws NullPointerException if resource is null
	 */
	@SuppressWarnings("resource")
	public ScopedResource(final T resource, final Lifecycle management) {
		this.resource = Objects.requireNonNull(resource, "resource cannot be null");
		this.managed = Objects.requireNonNull(management, "management cannot be null").isManaged();
		this.leakTracker = managed ? ResourceLeakDetector.track(resource) : ResourceLeakTracker.DISABLED;
	}

	/**
	 * Constructs a new managed ScopedResource instance.
	 *
	 * @param resource the resource to wrap (must not be null)
	 * @throws NullPointerException if resource is null
	 */
	public ScopedResource(final T resource) {
		this(resource, Lifecycle.MANAGED);
	}

	/**
	 * Returns the underlying resource.
	 *
	 * @return the wrapped resource
	 */
	public T unwrap() {
		return resource;
	}

	/**
	 * Returns the underlying resource. This is an alias for {@link #unwrap()}.
	 *
	 * @return the wrapped resource
	 */
	public T get() {
		return unwrap();
	}

	/**
	 * Checks if this wrapper manages the resource's life-cycle.
	 *
	 * @return true if the resource is managed by this wrapper
	 */
	public boolean isManaged() {
		return managed;
	}

	/**
	 * Checks if this wrapper manages the resource's life-cycle.
	 *
	 * @return true if the resource is not managed by this wrapper
	 */
	public boolean isNotManaged() {
		return !isManaged();
	}

	/**
	 * Closes the resource if it is managed by this wrapper. For unmanaged resources, this method does nothing. For handling
	 * exceptions, consider using {@link #closeIfManaged(Consumer)}.
	 *
	 * @throws Exception if an error occurs while closing the resource
	 */
	public void closeIfManaged() throws Exception {
		closeIfManaged(resource);
	}

	/**
	 * Closes the resource if it is managed by this wrapper, handling any exceptions using the provided exception handler.
	 *
	 * @param exceptionHandler a consumer to handle exceptions that may occur during closing
	 */
	public void closeIfManaged(final Consumer<? super Exception> exceptionHandler) {
		try {
			closeIfManaged();
		} catch (Exception e) {
			exceptionHandler.accept(e);
		}
	}

	/**
	 * Runs the provided close task if the resource is managed by this wrapper. For unmanaged resources, this method does
	 * nothing. This method allows for custom close logic to be executed for managed resources, while ensuring that
	 * unmanaged resources are not affected. For handling exceptions, consider using {@link #closeIfManaged(Consumer)}.
	 * <p>
	 * The use of {@link AutoCloseable} here allows the close task to be used both as a lambda expression, method reference
	 * or a closeable resource providing flexibility in how the close logic is defined and executed.
	 * <p>
	 * Usage example:
	 *
	 * <pre>
	 * scopedResource.closeIfManaged(() -> {
	 * 	// custom close logic for the resource
	 * 	resource.close();
	 * });
	 * </pre>
	 *
	 * @param closeable a task that performs the close operation on the resource
	 * @throws Exception if an error occurs while executing the close task
	 */
	public void closeIfManaged(final AutoCloseable closeable) throws Exception {
		if (isNotManaged()) {
			return;
		}
		try {
			closeable.close();
		} finally {
			leakTracker.close();
		}
	}

	/**
	 * Closes the resource if it is managed by this wrapper. This method is an alias for {@link #closeIfManaged()} to allow
	 * using try-with-resources with managed resources.
	 * <p>
	 * For unmanaged resources, this method does nothing.
	 */
	@Override
	public void close() throws Exception {
		closeIfManaged();
	}

	/**
	 * Returns the leak tracker associated with this scoped resource. This is primarily intended for internal use and
	 * testing to verify that leak tracking is functioning correctly.
	 *
	 * @return the resource leak tracker
	 */
	protected ResourceLeakTracker getLeakTracker() {
		return leakTracker;
	}

	/**
	 * Creates a new ScopedResource instance.
	 *
	 * @param <T> the type of the resource
	 *
	 * @param resource the resource to wrap
	 * @param management whether the wrapper should manage the life-cycle of the resource
	 * @return a new ScopedResource instance
	 */
	public static <T extends AutoCloseable> ScopedResource<T> of(final T resource, final Lifecycle management) {
		return new ScopedResource<>(resource, management);
	}

	/**
	 * Creates a new managed ScopedResource instance.
	 *
	 * @param <T> the type of the resource
	 *
	 * @param resource the resource to wrap
	 * @return a new managed ScopedResource instance
	 */
	public static <T extends AutoCloseable> ScopedResource<T> managed(final T resource) {
		return of(resource, Lifecycle.MANAGED);
	}

	/**
	 * Creates a new managed ScopedResource instance. This is an alias for {@link #managed(AutoCloseable)}.
	 *
	 * @param <T> the type of the resource
	 *
	 * @param resource the resource to wrap
	 * @return a new managed ScopedResource instance
	 */
	public static <T extends AutoCloseable> ScopedResource<T> owned(final T resource) {
		return managed(resource);
	}

	/**
	 * Creates a new unmanaged ScopedResource instance.
	 *
	 * @param <T> the type of the resource
	 *
	 * @param resource the resource to wrap
	 * @return a new unmanaged ScopedResource instance
	 */
	public static <T extends AutoCloseable> ScopedResource<T> unmanaged(final T resource) {
		return of(resource, Lifecycle.UNMANAGED);
	}

	/**
	 * Creates a new unmanaged ScopedResource instance. This is an alias for {@link #unmanaged(AutoCloseable)}.
	 *
	 * @param <T> the type of the resource
	 *
	 * @param resource the resource to wrap
	 * @return a new unmanaged ScopedResource instance
	 */
	public static <T extends AutoCloseable> ScopedResource<T> external(final T resource) {
		return unmanaged(resource);
	}

	/**
	 * Checks the first parameter against the second for the same underlying reference. If the referenced resources are the
	 * same, and they are both managed then we return an unmanaged scoped resource. Only one scoped resource should manage
	 * the same resource.
	 * <p>
	 * This is useful when passing scoped resources around to ensure that only one manager exists for a given resource. It
	 * is not a mandatory check but a safety mechanism to avoid double closing of resources but since the
	 * {@link AutoCloseable#close()} method should be idempotent it is not a critical one.
	 *
	 * @param <T> the type of the {@link AutoCloseable} resources being wrapped
	 *
	 * @param checkedResource the resource to check
	 * @param resource the resource to check against
	 * @return a checked scoped resource
	 */
	@SuppressWarnings("resource")
	public static <T extends AutoCloseable> ScopedResource<T> ensureSingleManager(
			final ScopedResource<T> checkedResource,
			final ScopedResource<T> resource) {
		if (checkedResource.isNotManaged() || resource.isNotManaged()) {
			return checkedResource;
		}
		T rawCheckedResource = checkedResource.unwrap();
		return rawCheckedResource == resource.unwrap()
				? ScopedResource.unmanaged(rawCheckedResource)
				: checkedResource;
	}

	/**
	 * Safely closes the given resource, handling any exceptions using the provided exception handler. If the resource is
	 * null, this method does nothing.
	 *
	 * @param resource the resource to close
	 * @param exceptionHandler a consumer to handle exceptions that may occur during closing
	 */
	public static void safeClose(final AutoCloseable resource, final Consumer<? super Exception> exceptionHandler) {
		if (null == resource) {
			return;
		}
		try {
			resource.close();
		} catch (Exception e) {
			exceptionHandler.accept(e);
		}
	}

	/**
	 * Derives a new ScopedResource from the current one using the provided factory function. The management of the new
	 * resource is determined by the management of the current resource.
	 * <ul>
	 * <li>If the current resource is managed, the new resource will also be managed</li>
	 * <li>If the current resource is unmanaged, the new resource will also be unmanaged</li>
	 * </ul>
	 * This allows for consistent management of related resources and ensures that derived resources are properly handled
	 * based on the management of the original resource.
	 *
	 * @param <R> the type of the new resource being derived
	 *
	 * @param factory a function that takes the current resource and produces a new resource
	 * @return a new ScopedResource instance wrapping the derived resource
	 * @throws Exception if an error occurs while creating the new resource
	 */
	public <R extends AutoCloseable> ScopedResource<R> derive(final ThrowingFunction<? super T, ? extends R> factory)
			throws Exception { // NOSONAR this method is designed to accommodate any throwing function
		final R child;
		try {
			child = factory.apply(resource);
			return isManaged() ? managed(child) : unmanaged(child);
		} catch (Throwable t) {
			return Unchecked.reThrow(t);
		}
	}
}
