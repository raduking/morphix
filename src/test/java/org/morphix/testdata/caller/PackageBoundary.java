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
package org.morphix.testdata.caller;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.morphix.testdata.callerprefix.PrefixBoundary;

/**
 * Supplier whose package name is a prefix of {@link PrefixBoundary}'s package.
 *
 * @author Radu Sebastian LAZIN
 */
public final class PackageBoundary implements Supplier<String> {

	private static final PackageBoundary SUPPLIER = new PackageBoundary();

	private PackageBoundary() {
		// singleton
	}

	@Override
	public String get() {
		return "value";
	}

	/**
	 * Calls through a package that only shares this package name as a prefix.
	 *
	 * @param nameFunction function to build the name from the class name and method name
	 * @return caller method name
	 */
	public static Optional<String> invokeThroughPrefix(final BiFunction<String, String, String> nameFunction) {
		return PrefixBoundary.invoke(SUPPLIER, nameFunction);
	}
}
