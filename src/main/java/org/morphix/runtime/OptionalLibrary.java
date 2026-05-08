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
 * Descriptor for a library indicating its presence and associated facade class. This class is used to check for the
 * presence of a library in the classpath and to provide access to a facade class that can be used to interact with the
 * library if it is present. It also allows for an instance function to retrieve instances of the facade class,
 * providing flexibility in how the facade class is instantiated (e.g., singleton, new instance per call, etc.).
 *
 * @param <T> the type of the facade class associated with the library
 *
 * @author Radu Sebastian LAZIN
 */
public class OptionalLibrary<T> {

	/**
	 * Indicates if the library is present and the facade class associated with it.
	 */
	private final boolean present;

	/**
	 * The facade class associated with the library.
	 */
	private final Class<T> facadeClass;

	/**
	 * An instance function to retrieve instances of the facade class, if needed.
	 */
	private final InstanceFunction<T> instanceFunction;

	/**
	 * Constructor with presence flag and facade class. When providing the instance function, it will be used to retrieve
	 * instances of the facade class. If the instance function is not provided, a default one will be created using the
	 * constructor of the facade class. This allows for flexibility in how instances of the facade class are created and
	 * also allows for singleton instances if the instance function is implemented to return the same instance.
	 *
	 * @param present indicates if the library is present
	 * @param facadeClass the facade class associated with the library
	 * @param instanceFunction an optional instance function to retrieve instances of the facade class, if needed
	 */
	protected OptionalLibrary(final boolean present, final Class<T> facadeClass, final InstanceFunction<T> instanceFunction) {
		this.present = present;
		this.facadeClass = Objects.requireNonNull(facadeClass, "facadeClass must not be null");
		this.instanceFunction = instanceFunction;
	}

	/**
	 * Creates a default instance function that uses the constructor of the facade class to create new instances. This is
	 * used when no custom instance function is provided, allowing for a simple way to create instances of the facade class
	 * using its default constructor.
	 *
	 * @param <T> the type of the facade class
	 *
	 * @param facadeClass facade class to create instances of
	 * @return an instance function that creates new instances of the facade class using its constructor
	 */
	private static <T> InstanceFunction<T> defaultInstanceFunction(final Class<T> facadeClass) {
		return () -> Constructors.IgnoreAccess.newInstance(facadeClass);
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance indicating the library is present.
	 *
	 * @param <T> the type of the facade class
	 *
	 * @param facadeClass the facade class associated with the library
	 * @return a new LibraryDescriptor instance with presence set to true
	 */
	public static <T> OptionalLibrary<T> present(final Class<T> facadeClass) {
		return present(facadeClass, defaultInstanceFunction(facadeClass));
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance indicating the library is present.
	 *
	 * @param <T> the type of the facade class
	 *
	 * @param facadeClass the facade class associated with the library
	 * @param instanceFunction an instance function to retrieve instances of the facade class
	 * @return a new LibraryDescriptor instance with presence set to true
	 */
	public static <T> OptionalLibrary<T> present(final Class<T> facadeClass, final InstanceFunction<T> instanceFunction) {
		return new OptionalLibrary<>(true, facadeClass, instanceFunction);
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance indicating the library is not present.
	 *
	 * @param <T> the type of the facade class
	 *
	 * @param facadeClass the facade class associated with the library
	 * @return a new LibraryDescriptor instance with presence set to false
	 */
	public static <T> OptionalLibrary<T> notPresent(final Class<T> facadeClass) {
		return notPresent(facadeClass, defaultInstanceFunction(facadeClass));
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance indicating the library is not present.
	 *
	 * @param <T> the type of the facade class
	 *
	 * @param facadeClass the facade class associated with the library
	 * @param instanceFunction an instance function to retrieve instances of the facade class
	 * @return a new LibraryDescriptor instance with presence set to false
	 */
	public static <T> OptionalLibrary<T> notPresent(final Class<T> facadeClass, final InstanceFunction<T> instanceFunction) {
		return new OptionalLibrary<>(false, facadeClass, instanceFunction);
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance.
	 *
	 * @param <T> the type of the facade class
	 *
	 * @param libraryClassName the fully qualified class name to check for presence
	 * @param facadeClass the facade class associated with the library
	 * @return a new LibraryDescriptor instance
	 */
	public static <T> OptionalLibrary<T> of(final String libraryClassName, final Class<T> facadeClass) {
		return of(libraryClassName, facadeClass, defaultInstanceFunction(facadeClass));
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance.
	 *
	 * @param <T> the type of the facade class
	 *
	 * @param libraryClassName the fully qualified class name to check for presence
	 * @param facadeClass the facade class associated with the library
	 * @param instanceFunction an instance function to retrieve instances of the facade class
	 * @return a new LibraryDescriptor instance
	 */
	public static <T> OptionalLibrary<T> of(final String libraryClassName, final Class<T> facadeClass,
			final InstanceFunction<T> instanceFunction) {
		return of(List.of(libraryClassName), facadeClass, instanceFunction);
	}

	/**
	 * Factory method to create a {@link OptionalLibrary} instance.
	 *
	 * @param <T> the type of the facade class
	 *
	 * @param libraryClassNames a list of fully qualified class names to check for presence
	 * @param facadeClass the facade class associated with the library
	 * @param instanceFunction an instance function to retrieve instances of the facade class
	 * @return a new LibraryDescriptor instance
	 */
	public static <T> OptionalLibrary<T> of(final List<String> libraryClassNames, final Class<T> facadeClass,
			final InstanceFunction<T> instanceFunction) {
		boolean libraryPresent = libraryClassNames.stream().allMatch(Reflection::isClassPresent);
		return libraryPresent
				? present(facadeClass, instanceFunction)
				: notPresent(facadeClass, instanceFunction);
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
	 * Retrieves the facade class associated with the library.
	 *
	 * @return the facade class
	 */
	public Class<T> getFacadeClass() {
		return facadeClass;
	}

	/**
	 * Retrieves the facade class associated with the library.
	 * <p>
	 * Alias for {@link #getFacadeClass()}.
	 *
	 * @return the facade class
	 */
	public Class<T> getSpecificClass() {
		return getFacadeClass();
	}

	/**
	 * Retrieves an instance of the facade class using the instance function.
	 *
	 * @return an instance of the facade class
	 */
	public T getFacadeInstance() {
		return instanceFunction.get();
	}

	/**
	 * Retrieves an instance of the facade class using the instance function.
	 * <p>
	 * Alias for {@link #getFacadeInstance()}.
	 *
	 * @return an instance of the facade class
	 */
	public T getSpecificInstance() {
		return getFacadeInstance();
	}
}
