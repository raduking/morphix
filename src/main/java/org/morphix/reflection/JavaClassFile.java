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

/**
 * Defines constants and utilities for working with Java class files.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JavaClassFile {

	/**
	 * The class file extension.
	 */
	String EXTENSION = ".class";

	/**
	 * The magic number that identifies a class file.
	 */
	int CAFEBABE = 0xCAFEBABE;

	/**
	 * Represents a class file version with major and minor components.
	 *
	 * @param major the major version number
	 * @param minor the minor version number
	 *
	 * @author Radu Sebastian LAZIN
	 */
	record Version(int major, int minor) {

		/**
		 * The magic offset used to calculate the major version from the Java version.
		 * <p>
		 * According to the Java class file specification, the major version is calculated as 44 + Java version (e.g., Java 8 =
		 * 52, Java 11 = 55)
		 */
		public static final int MAGIC_OFFSET = 44;

		/**
		 * The default minor version for class files.
		 */
		public static final int DEFAULT_MINOR = 0;

		/**
		 * Creates a Version instance from a given Java version.
		 *
		 * @param javaVersion the Java version
		 * @return the corresponding class file Version
		 */
		public static Version fromJavaVersion(final int javaVersion) {
			int major = MAGIC_OFFSET + javaVersion;
			return new Version(major, DEFAULT_MINOR);
		}
	}
}
