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
package org.morphix.runtime;

import java.util.Objects;
import java.util.function.Supplier;

import org.morphix.lang.JavaArrays;

/**
 * Utility interface for initializing libraries based on their presence in the descriptors.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Libraries {

	/**
	 * Initializes and returns an instance of the first available library from the provided descriptors. If none of the
	 * libraries are present, it uses the fallback supplier to provide a default instance.
	 *
	 * @param <T> the type of the library instance
	 *
	 * @param fallbackSupplier the supplier to provide a default instance if no libraries are present, must not be null
	 * @param libraryDescriptors the library descriptors to check for presence
	 * @return an instance of the first available library or a default instance from the fallback supplier
	 */
	@SafeVarargs
	static <T> T instance(final Supplier<T> fallbackSupplier, final OptionalLibrary<? extends T>... libraryDescriptors) {
		Objects.requireNonNull(fallbackSupplier, "fallbackSupplier must not be null");
		if (JavaArrays.isNotEmpty(libraryDescriptors)) {
			for (OptionalLibrary<? extends T> libraryDescriptor : libraryDescriptors) {
				if (libraryDescriptor.isPresent()) {
					return libraryDescriptor.getSpecificInstance();
				}
			}
		}
		return fallbackSupplier.get();
	}
}
