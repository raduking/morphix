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

import java.util.List;
import java.util.Objects;

import org.morphix.lang.function.InstanceFunction;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Reflection;

/**
 * Descriptor for a library indicating its presence and associated specific class.
 *
 * @param <T> the type of the specific class associated with the library
 *
 * @author Radu Sebastian LAZIN
 */
public class OptionalLibrary<T> {

	/**
	 * Indicates if the library is present and the specific class associated with it.
	 */
	private final boolean present;

	/**
	 * The specific class associated with the library.
	 */
	private final Class<T> specificClass;

	/**
	 * An instance function to retrieve instances of the specific class, if needed.
	 */
	private final InstanceFunction<T> instanceFunction;

	/**
	 * Constructor with presence flag and specific class. When providing the instance function, it will be used to retrieve
	 * instances of the specific class. If the instance function is not provided, a default one will be created using the
	 * constructor of the specific class. This allows for flexibility in how instances of the specific class are created and
	 * also allows for singleton instances if the instance function is implemented to return the same instance.
	 *
	 * @param present indicates if the library is present
	 * @param specificClass the specific class associated with the library
	 * @param instanceFunction an optional instance function to retrieve instances of the specific class, if needed
	 */
	protected OptionalLibrary(final boolean present, final Class<T> specificClass, final InstanceFunction<T> instanceFunction) {
		this.present = present;
		this.specificClass = Objects.requireNonNull(specificClass, "specificClass must not be null");
		this.instanceFunction = instanceFunction;
	}

	/**
	 * Creates a default instance function that uses the constructor of the specific class to create new instances. This is
	 * used when no custom instance function is provided, allowing for a simple way to create instances of the specific
	 * class using its default constructor.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param specificClass specific class to create instances of
	 * @return an instance function that creates new instances of the specific class using its constructor
	 */
	private static <T> InstanceFunction<T> defaultInstanceFunction(final Class<T> specificClass) {
		return () -> Constructors.IgnoreAccess.newInstance(specificClass);
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance indicating the library is present.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param specificClass the specific class associated with the library
	 * @return a new LibraryDescriptor instance with presence set to true
	 */
	public static <T> OptionalLibrary<T> present(final Class<T> specificClass) {
		return present(specificClass, defaultInstanceFunction(specificClass));
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance indicating the library is present.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param specificClass the specific class associated with the library
	 * @param instanceFunction an instance function to retrieve instances of the specific class
	 * @return a new LibraryDescriptor instance with presence set to true
	 */
	public static <T> OptionalLibrary<T> present(final Class<T> specificClass, final InstanceFunction<T> instanceFunction) {
		return new OptionalLibrary<>(true, specificClass, instanceFunction);
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance indicating the library is not present.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param specificClass the specific class associated with the library
	 * @return a new LibraryDescriptor instance with presence set to false
	 */
	public static <T> OptionalLibrary<T> notPresent(final Class<T> specificClass) {
		return notPresent(specificClass, defaultInstanceFunction(specificClass));
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance indicating the library is not present.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param specificClass the specific class associated with the library
	 * @param instanceFunction an instance function to retrieve instances of the specific class
	 * @return a new LibraryDescriptor instance with presence set to false
	 */
	public static <T> OptionalLibrary<T> notPresent(final Class<T> specificClass, final InstanceFunction<T> instanceFunction) {
		return new OptionalLibrary<>(false, specificClass, instanceFunction);
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param libraryClassName the fully qualified class name to check for presence
	 * @param specificClass the specific class associated with the library
	 * @return a new LibraryDescriptor instance
	 */
	public static <T> OptionalLibrary<T> of(final String libraryClassName, final Class<T> specificClass) {
		return of(libraryClassName, specificClass, defaultInstanceFunction(specificClass));
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param libraryClassName the fully qualified class name to check for presence
	 * @param specificClass the specific class associated with the library
	 * @param instanceFunction an instance function to retrieve instances of the specific class
	 * @return a new LibraryDescriptor instance
	 */
	public static <T> OptionalLibrary<T> of(final String libraryClassName, final Class<T> specificClass,
			final InstanceFunction<T> instanceFunction) {
		return of(List.of(libraryClassName), specificClass, instanceFunction);
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance.
	 *
	 * @param <T> the type of the specific class
	 *
	 * @param libraryClassNames a list of fully qualified class names to check for presence
	 * @param specificClass the specific class associated with the library
	 * @param instanceFunction an instance function to retrieve instances of the specific class
	 * @return a new LibraryDescriptor instance
	 */
	public static <T> OptionalLibrary<T> of(final List<String> libraryClassNames, final Class<T> specificClass,
			final InstanceFunction<T> instanceFunction) {
		boolean libraryPresent = libraryClassNames.stream().allMatch(Reflection::isClassPresent);
		return libraryPresent
				? present(specificClass, instanceFunction)
				: notPresent(specificClass, instanceFunction);
	}

	/**
	 * Checks if the library is present.
	 *
	 * @return true if the library is present, false otherwise
	 */
	public boolean isPresent() {
		return present;
	}

	/**
	 * Retrieves the specific class associated with the library.
	 *
	 * @return the specific class
	 */
	public Class<T> getSpecificClass() {
		return specificClass;
	}

	/**
	 * Retrieves an instance of the specific class using the instance function.
	 *
	 * @return an instance of the specific class
	 */
	public T getSpecificInstance() {
		return instanceFunction.get();
	}
}
