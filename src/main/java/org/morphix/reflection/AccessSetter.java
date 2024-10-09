/*
 * Copyright 2025 the original author or authors.
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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.Objects;

/**
 * Functional interface for setting access for {@link AccessibleObject} or {@link Member}.
 *
 * @param <T> accessible object type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface AccessSetter<T extends AccessibleObject & Member> {

	/**
	 * Sets the accessible state of the object. Returns true if successful false otherwise.
	 *
	 * @param object object for which the access change is requested
	 * @param access accessible flag
	 * @return true if successful false otherwise
	 */
	boolean setAccessible(T object, boolean access);

	/**
	 * Creates an access setter for the override setter. The override setter is the setter for the {@link AccessibleObject}s
	 * "override" field.
	 *
	 * @param <T> accessible object type
	 *
	 * @param overrideSetter override setter
	 * @return access setter
	 */
	public static <T extends AccessibleObject & Member> AccessSetter<T> ofOverride(final MethodHandle overrideSetter) {
		Objects.requireNonNull(overrideSetter, "overrideSetter");
		return (object, value) -> {
			try {
				// this is the correct way to invoke the overrideSetter method so disable Sonar
				overrideSetter.invokeWithArguments(new Object[] { object, value }); // NOSONAR
				return true;
			} catch (Throwable t) {
				return false;
			}
		};
	}

}
