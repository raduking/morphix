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
package org.morphix.invoke;

import java.lang.invoke.MethodType;

/**
 * Represents a unique method signature used for caching and resolving {@link java.lang.invoke.MethodHandle}s.
 * <p>
 * This record encapsulates:
 * </p>
 * <ul>
 * <li>Method name</li>
 * <li>Declared {@link MethodType} (parameters and return type)</li>
 * <li>Exact {@link MethodType} (with receiver for instance methods)</li>
 * <li>Static/instance flag</li>
 * <li>Declaring class</li>
 * </ul>
 * <p>
 * The {@code exactType} differs from {@code type} for instance methods because it includes the implicit receiver (the
 * declaring class) as the first parameter.
 * </p>
 * This signature is used as a unique key for method handle caching.
 *
 * @param name The method name.
 * @param type The declared {@link MethodType} (as per Java reflection signature).
 * @param isStatic Whether the method is static.
 * @param declaringClass The class declaring the method.
 *
 * @author Radu Sebastian LAZIN
 */
public record MethodSignature(
		String name,
		MethodType type,
		boolean isStatic,
		Class<?> declaringClass) {

	/**
	 * Factory method to create a {@link MethodSignature} for the specified method.
	 * <p>
	 * For instance methods, the {@code exactType} will prepend the declaring class as the first parameter to account for
	 * the implicit receiver. For static methods, {@code exactType} is identical to {@code type}.
	 * </p>
	 *
	 * @param cls The declaring class.
	 * @param name The method name.
	 * @param type The declared method type (parameters and return type).
	 * @param isStatic {@code true} if the method is static, {@code false} otherwise.
	 * @return A new {@link MethodSignature} instance representing the method.
	 */
	public static MethodSignature of(final Class<?> cls, final String name, final MethodType type, final boolean isStatic) {
		return new MethodSignature(name, type, isStatic, cls);
	}
}
