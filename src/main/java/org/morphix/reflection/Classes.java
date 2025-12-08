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

import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.morphix.lang.JavaObjects;

/**
 * Utility reflection methods for classes.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Classes {

	/**
	 * Returns a class based on a class name.
	 *
	 * @param <T> returned type
	 *
	 * @param className the {@linkplain ClassLoader##binary-name binary name}
	 * @return a class based on a class name
	 * @throws ReflectionException if the class cannot be loaded
	 */
	static <T> Class<T> getOne(final String className) {
		try {
			return JavaObjects.cast(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new ReflectionException("Could not load class: " + className, e);
		}
	}

	/**
	 * Returns a class based on a class name.
	 *
	 * @param <T> returned type
	 *
	 * @param className the {@linkplain ClassLoader##binary-name binary name}
	 * @param classLoader the class loader used to load the class
	 * @return a class based on a class name
	 * @throws ReflectionException if the class cannot be loaded
	 */
	static <T> Class<T> getOne(final String className, final ClassLoader classLoader) {
		try {
			return JavaObjects.cast(Class.forName(className, false, classLoader));
		} catch (ClassNotFoundException e) {
			throw new ReflectionException("Could not load class: " + className, e);
		}
	}

	/**
	 * Returns the subclass of the expected parent.
	 *
	 * @param expectedParent expected parent
	 * @param child some child class
	 * @return the subclass of the expected parent
	 * @throws ReflectionException if the expected parent is not found
	 */
	static Class<?> findSubclass(final Class<?> expectedParent, final Class<?> child) {
		Class<?> parent = child.getSuperclass();
		if (Object.class == parent) {
			throw new ReflectionException("The parent of " + child.getCanonicalName() + " is not a " + expectedParent.getCanonicalName());
		}
		if (expectedParent == parent) {
			return child;
		}
		return findSubclass(expectedParent, parent);
	}

	/**
	 * Creates a mutable set of classes.
	 *
	 * @param classes the classes to add in the set
	 * @return a mutable set of classes
	 */
	static Set<Class<?>> mutableSetOf(final Class<?>... classes) {
		Set<Class<?>> set = new HashSet<>();
		Collections.addAll(set, classes);
		return set;
	}

	/**
	 * Interface which groups all methods that return null and don't throw exceptions on expected errors. This functions as
	 * a name space so that the method names inside keep the same name pattern.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface Safe {

		/**
		 * Returns a class based on a class name, if the class is not found it returns {@code null}.
		 *
		 * @param <T> returned type
		 *
		 * @param className the {@linkplain ClassLoader##binary-name binary name}
		 * @return a class based on a class name, null if class is not found
		 */
		static <T> Class<T> getOne(final String className) {
			try {
				return Classes.getOne(className);
			} catch (ReflectionException e) {
				return null;
			}
		}

	}

	/**
	 * Additional discovery utilities (class path scanning, annotation scanning, file-based scanning).
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface Scan {

		/**
		 * The class file extension.
		 */
		String CLASS_FILE_EXTENSION = ".class";

		/**
		 * Finds all classes in a specific package located in a specific classes directory.
		 * <p>
		 * This method resolves the package name to a directory path, verifies the directory exists, and then searches for all
		 * class files within that directory and its sub-directories.
		 *
		 * @param basePackage the base package to scan
		 * @param classesDir the classes directory
		 * @return all classes in a specific package located in a specific classes directory
		 * @throws SecurityException if a security manager exists and access to class loading is restricted
		 */
		static Set<Class<?>> findInPackage(final String basePackage, final Path classesDir) {
			File directory = classesDir.resolve(basePackage.replace('.', '/')).toFile();
			if (!directory.exists() || !directory.isDirectory()) {
				return Collections.emptySet();
			}
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			return findInDirectory(directory, basePackage, cl);
		}

		/**
		 * Recursively finds all classes in a specific directory.
		 * <p>
		 * This method performs a depth-first traversal of the directory structure, treating each sub-directory as a package
		 * component and each `.class` file as a loadable class.
		 *
		 * @param directory the directory
		 * @param packageName the package name
		 * @param classLoader the class loader
		 * @return all classes in a specific directory
		 */
		static Set<Class<?>> findInDirectory(final File directory, final String packageName, final ClassLoader classLoader) {
			File[] files = Objects.requireNonNull(directory.listFiles(), "Directory listing failed for: " + directory);
			Set<Class<?>> classes = new HashSet<>();
			for (File file : files) {
				if (file.isDirectory()) {
					classes.addAll(findInDirectory(file, packageName + "." + file.getName(), classLoader));
				} else if (file.getName().endsWith(CLASS_FILE_EXTENSION)) {
					String className = packageName + '.' + file.getName().substring(0, file.getName().length() - CLASS_FILE_EXTENSION.length());
					classes.add(getOne(className, classLoader));
				}
			}
			return classes;
		}

		/**
		 * Finds all classes annotated with any of the specified annotations in the specified packages located in a specific
		 * classes directory.
		 * <p>
		 * This method scans each package for classes, checks each class for the presence of the specified annotations, and
		 * collects those classes into a result set.
		 *
		 * @param packages the packages to scan
		 * @param classesDir the classes directory
		 * @param annotations the annotations to look for
		 * @return all classes annotated with any of the specified annotations in the specified packages located in a specific
		 * classes directory
		 */
		static Set<Class<?>> findWithAnyAnnotation(final Set<String> packages, final Path classesDir,
				final Set<Class<? extends Annotation>> annotations) {
			if (null == packages || packages.isEmpty()) {
				return Collections.emptySet();
			}
			Set<Class<?>> annotated = new HashSet<>();
			for (String pkg : packages) {
				for (Class<?> cls : findInPackage(pkg, classesDir)) {
					for (Class<? extends Annotation> ann : annotations) {
						if (null != cls.getAnnotation(ann)) {
							annotated.add(cls);
							break;
						}
					}
				}
			}
			return annotated;
		}

		/**
		 * Finds all classes annotated with any of the specified annotations in the specified packages located in a specific
		 * classes directory. This version accepts a logger consumer to log messages during the scanning process and it is
		 * marginally slower than the version without logging because it logs all annotations found on the classes where the
		 * first version just skips to the next class.
		 *
		 * @param packages the packages to scan
		 * @param classesDir the classes directory
		 * @param annotations the annotations to look for
		 * @param loggerConsumer a consumer for logging messages must not be null
		 * @return all classes annotated with any of the specified annotations in the specified packages located in a specific
		 * classes directory
		 */
		static Set<Class<?>> findWithAnyAnnotation(final Set<String> packages, final Path classesDir,
				final Set<Class<? extends Annotation>> annotations, final Consumer<String> loggerConsumer) {
			if (null == packages || packages.isEmpty()) {
				return Collections.emptySet();
			}
			Set<Class<?>> annotated = new HashSet<>();
			for (String pkg : packages) {
				loggerConsumer.accept("Scanning package: " + pkg);
				for (Class<?> cls : findInPackage(pkg, classesDir)) {
					boolean found = false;
					for (Class<? extends Annotation> ann : annotations) {
						if (null == cls.getAnnotation(ann)) {
							continue;
						}
						loggerConsumer.accept("Found annotated class: " + cls.getCanonicalName() + " with annotation: " + ann.getCanonicalName());
						if (!found) {
							annotated.add(cls);
							found = true;
						}
					}
				}
			}
			return annotated;
		}
	}
}
