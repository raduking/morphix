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

/**
 * Context for tracking visited objects during conversion to prevent infinite recursion in case of cyclic references.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ConversionContext {

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
	 * Marks the given object as visited. Returns {@code true} if the object was not previously visited, or {@code false} if
	 * it has already been visited.
	 *
	 * @param o the object to mark as visited
	 * @return {@code true} if the object was not previously visited, {@code false} otherwise
	 */
	public boolean enter(final Object o) {
		return visited.put(o, Boolean.TRUE) == null;
	}

	/**
	 * Marks the given object as no longer being visited. This should be called after processing an object to allow for
	 * correct handling of other objects that may reference it.
	 *
	 * @param o the object to mark as no longer being visited
	 */
	public void exit(final Object o) {
		visited.remove(o);
	}
}
