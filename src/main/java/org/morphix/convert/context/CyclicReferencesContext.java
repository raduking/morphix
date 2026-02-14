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
package org.morphix.convert.context;

import java.util.IdentityHashMap;

/**
 * Context for tracking visited objects during conversion to prevent infinite recursion in case of cyclic references.
 *
 * @author Radu Sebastian LAZIN
 */
public class CyclicReferencesContext implements ConversionContext {

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
	 * Creates a new instance of {@link CyclicReferencesContext}.
	 */
	public CyclicReferencesContext() {
		// empty
	}

	/**
	 * Marks the given object as being visited. This should be called before processing an object to prevent infinite
	 * recursion in case of cyclic references.
	 *
	 * @param obj the object to mark as being visited
	 * @return {@code true} if the object was already marked as being visited, {@code false} otherwise
	 */
	@Override
	public boolean enter(final Object obj) {
		return null != visited.put(obj, Boolean.TRUE);
	}

	/**
	 * Marks the given object as no longer being visited. This should be called after processing an object to allow for
	 * correct handling of other objects that may reference it.
	 *
	 * @param obj the object to mark as no longer being visited
	 */
	@Override
	public void exit(final Object obj) {
		visited.remove(obj);
	}
}
