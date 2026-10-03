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
package org.morphix.reflection;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility reflection methods for interfaces.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Interfaces {

	/**
	 * Returns all the interfaces implemented directly or indirectly by the given class, in no particular order and without
	 * duplicates. The given type itself is never part of the result, not even when it is an interface.
	 *
	 * @param cls class to get the interfaces of
	 * @return all the interfaces implemented by the given class
	 */
	static Set<Class<?>> getAll(final Class<?> cls) {
		Set<Class<?>> result = new LinkedHashSet<>();
		collectAll(cls, result);
		return result;
	}

	/**
	 * Collects the interfaces implemented by the given type, directly or indirectly, into the given set.
	 *
	 * @param cls type to collect the interfaces of
	 * @param result set to collect the interfaces into
	 */
	private static void collectAll(final Class<?> cls, final Set<Class<?>> result) {
		for (Class<?> iface : cls.getInterfaces()) {
			if (result.add(iface)) {
				collectAll(iface, result);
			}
		}
	}
}
