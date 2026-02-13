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
package org.morphix.convert.extras;

import java.util.IdentityHashMap;
import java.util.function.Supplier;

/**
 * Context for tracking visited objects during conversion to prevent infinite recursion in case of cyclic references.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ConversionContext {

	/**
	 * Key used to indicate a cyclic reference in the conversion result. This can be used by converters to return a
	 * placeholder value when a cyclic reference is detected.
	 */
	public static final String CYCLIC_REFERENCE = "_cyclic_ref";

	/**
	 * {@link IdentityHashMap} is used to track visited objects based on their identity (reference) rather than equality,
	 * which is crucial for correctly handling cyclic references.
	 */
	private final IdentityHashMap<Object, Object> visited = new IdentityHashMap<>();

	/**
	 * Creates a new instance of {@link ConversionContext}.
	 */
	public ConversionContext() {
		// empty
	}

	/**
	 * Marks the given object as being visited. This should be called before processing an object to prevent infinite
	 * recursion in case of cyclic references.
	 *
	 * @param obj the object to mark as being visited
	 * @return {@code true} if the object was already marked as being visited, {@code false} otherwise
	 */
	public boolean enter(final Object obj) {
		return null != visited.put(obj, Boolean.TRUE);
	}

	/**
	 * Marks the given object as no longer being visited. This should be called after processing an object to allow for
	 * correct handling of other objects that may reference it.
	 *
	 * @param obj the object to mark as no longer being visited
	 */
	public void exit(final Object obj) {
		visited.remove(obj);
	}

	/**
	 * Executes the given supplier function for the provided object, while tracking the object in the context to prevent
	 * infinite recursion in case of cyclic references. If the object is already being tracked, the
	 * {@code cyclicReferenceSupplier} will be executed instead.
	 *
	 * @param <T> the type of the result produced by the supplier functions
	 *
	 * @param obj the object to process
	 * @param resultSupplier the supplier function to execute if the object is not already being tracked
	 * @param cyclicReferenceSupplier the supplier to execute if the object is already being tracked (cyclic reference)
	 * @return the result produced by either the {@code resultSupplier} or the {@code cyclicReferenceSupplier}, depending on
	 * whether a cyclic reference was detected
	 */
	public <T> T onObject(final Object obj, final Supplier<T> resultSupplier, final Supplier<T> cyclicReferenceSupplier) {
		if (enter(obj)) {
			return cyclicReferenceSupplier.get();
		}
		try {
			return resultSupplier.get();
		} finally {
			exit(obj);
		}
	}
}
