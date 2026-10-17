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

import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A class loader that hides selected classes and loads selected classes from its parent class path.
 * <p>
 * Locally loaded classes have a distinct static state and use this class loader to resolve classes, allowing for
 * example tests to simulate an optional dependency being absent. A local class name also matches its nested classes.
 * <p>
 * Use {@link #builder()} to set the parent class loader, the classes to hide, and the classes to load locally.
 *
 * @author Radu Sebastian LAZIN
 */
public class ClassHidingClassLoader extends ClassLoader {

	/**
	 * Class names hidden by this class loader.
	 */
	private final Set<String> hiddenClassNames;

	/**
	 * Root class names loaded by this class loader.
	 */
	private final Set<String> localClassNames;

	/**
	 * Creates a class loader that hides and locally loads the specified classes.
	 *
	 * @param parent parent class loader from which local class byte-code is read
	 * @param hiddenClassNames exact binary names of classes to hide
	 * @param localClassNames root binary names of classes to load locally, including their nested classes
	 */
	protected ClassHidingClassLoader(final ClassLoader parent, final Collection<String> hiddenClassNames, final Collection<String> localClassNames) {
		super(parent);
		this.hiddenClassNames = Set.copyOf(Objects.requireNonNull(hiddenClassNames, "hiddenClassNames collection must not be null"));
		this.localClassNames = Set.copyOf(Objects.requireNonNull(localClassNames, "localClassNames collection must not be null"));
	}

	/**
	 * Creates a builder for a class loader.
	 *
	 * @return a new builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Loads the class with the given binary name, hiding configured classes and locally defining configured classes.
	 *
	 * @param name binary name of the class
	 * @param resolve flag indicating if the class should be resolved
	 * @return the loaded class
	 * @throws ClassNotFoundException if the class cannot be found or is hidden
	 */
	@Override
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		if (hiddenClassNames.contains(name)) {
			throw new ClassNotFoundException(name);
		}
		synchronized (getClassLoadingLock(name)) {
			Class<?> type = findLoadedClass(name);
			if (null == type) {
				type = loadLocally(name) ? findClass(name) : getParent().loadClass(name);
			}
			if (resolve) {
				resolveClass(type);
			}
			return type;
		}
	}

	/**
	 * Defines the class with the given binary name from the byte-code found in the parent class path.
	 *
	 * @param name binary name of the class
	 * @return the defined class
	 * @throws ClassNotFoundException if the class byte-code cannot be found or read
	 */
	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		String resource = name.replace('.', '/') + ".class";
		try (InputStream inputStream = getParent().getResourceAsStream(resource)) {
			if (null == inputStream) {
				throw new ClassNotFoundException(name);
			}
			byte[] bytes = inputStream.readAllBytes();
			return defineClass(name, bytes, 0, bytes.length, protectionDomain(name));
		} catch (IOException e) {
			throw new ClassNotFoundException(name, e);
		}
	}

	/**
	 * Returns the parent class's protection domain, when available, so a locally defined class is not considered to be
	 * generated at runtime.
	 *
	 * @param name binary name of the class
	 * @return the protection domain for the locally defined class
	 */
	private ProtectionDomain protectionDomain(final String name) {
		try {
			return Class.forName(name, false, getParent()).getProtectionDomain();
		} catch (ClassNotFoundException e) {
			return getClass().getProtectionDomain();
		}
	}

	/**
	 * Checks whether the class should be loaded by this class loader.
	 *
	 * @param name binary name of the class
	 * @return {@code true} if the class is configured for local loading, including as a nested class
	 */
	private boolean loadLocally(final String name) {
		return localClassNames.stream().anyMatch(className -> name.equals(className) || name.startsWith(className + "$"));
	}

	/**
	 * Builds a {@link ClassHidingClassLoader} by naming the classes to hide and the classes to load locally.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Builder {

		/**
		 * Parent class loader from which local class byte-code is read. This is the class loader that loaded
		 * {@link ClassHidingClassLoader} when {@link #parent(ClassLoader)} is not called.
		 */
		private ClassLoader parent = defaultParent();

		/**
		 * Exact binary names of classes to hide.
		 */
		private final Set<String> hiddenClassNames = new HashSet<>();

		/**
		 * Root binary names of classes to load locally.
		 */
		private final Set<String> localClassNames = new HashSet<>();

		/**
		 * Private constructor.
		 */
		private Builder() {
			// hide constructor
		}

		/**
		 * Returns the class loader that loaded {@link ClassHidingClassLoader}, or the system class loader when this class was
		 * loaded by the bootstrap class loader.
		 *
		 * @return the default parent class loader
		 */
		private static ClassLoader defaultParent() {
			ClassLoader classLoader = ClassHidingClassLoader.class.getClassLoader();
			return null != classLoader ? classLoader : ClassLoader.getSystemClassLoader();
		}

		/**
		 * Sets the parent class loader from which local class byte-code is read. The default is the class loader that loaded
		 * {@link ClassHidingClassLoader}.
		 *
		 * @param parent parent class loader
		 * @return this builder
		 */
		public Builder parent(final ClassLoader parent) {
			this.parent = Objects.requireNonNull(parent, "parent must not be null");
			return this;
		}

		/**
		 * Hides the class with the given binary name.
		 *
		 * @param className exact binary name of the class to hide
		 * @return this builder
		 */
		public Builder hide(final String className) {
			hiddenClassNames.add(Objects.requireNonNull(className, "className must not be null"));
			return this;
		}

		/**
		 * Hides the given class.
		 *
		 * @param type the class to hide
		 * @return this builder
		 */
		public Builder hide(final Class<?> type) {
			return hide(Objects.requireNonNull(type, "type must not be null").getName());
		}

		/**
		 * Loads the class with the given binary name in this class loader, including its nested classes.
		 *
		 * @param className root binary name of the class to load locally
		 * @return this builder
		 */
		public Builder loadLocally(final String className) {
			localClassNames.add(Objects.requireNonNull(className, "className must not be null"));
			return this;
		}

		/**
		 * Loads the given class in this class loader, including its nested classes.
		 *
		 * @param type the class to load locally
		 * @return this builder
		 */
		public Builder loadLocally(final Class<?> type) {
			return loadLocally(Objects.requireNonNull(type, "type must not be null").getName());
		}

		/**
		 * Builds the class loader. The parent is the class loader that loaded {@link ClassHidingClassLoader} when
		 * {@link #parent(ClassLoader)} was not called.
		 *
		 * @return a class loader that hides and locally loads the configured classes
		 */
		public ClassHidingClassLoader build() {
			return new ClassHidingClassLoader(parent, hiddenClassNames, localClassNames);
		}
	}
}
