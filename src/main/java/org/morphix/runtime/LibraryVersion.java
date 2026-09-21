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

import java.util.Comparator;
import java.util.Objects;

import org.morphix.lang.Messages;
import org.morphix.reflection.Classes;

/**
 * Represents the runtime version of a library, as reported by the {@linkplain Package#getImplementationVersion()
 * implementation version} of an anchor class from that library. This is typically used to guard against optional or
 * provided dependencies (declared with {@code provided}/{@code optional} scope) being resolved to a version older than
 * the one the depending code actually requires at runtime.
 * <p>
 * If the runtime version cannot be determined (e.g. missing manifest information, or the anchor class is not present on
 * the classpath), {@link #getVersion()} returns {@code null} and
 * {@link #isAtLeast(String)}/{@link #verifyAtLeast(String)} treat the check as unenforceable rather than failing.
 *
 * @author Radu Sebastian LAZIN
 */
public class LibraryVersion implements Comparable<LibraryVersion> {

	/**
	 * The library name, used only for diagnostic messages.
	 */
	private final String name;

	/**
	 * The detected runtime version, or {@code null} if it could not be determined.
	 */
	private final String version;

	/**
	 * Constructor with the library name and its detected runtime version.
	 *
	 * @param name the library name
	 * @param version the detected runtime version, may be {@code null}
	 */
	protected LibraryVersion(final String name, final String version) {
		this.name = name;
		this.version = version;
	}

	/**
	 * Builds a {@link LibraryVersion} by reading the implementation version from the given anchor class' package.
	 *
	 * @param name the library name
	 * @param anchorClass a class belonging to the library
	 * @return a new {@link LibraryVersion}
	 */
	public static LibraryVersion of(final String name, final Class<?> anchorClass) {
		if (null == anchorClass) {
			return new LibraryVersion(name, null);
		}
		return of(name, anchorClass.getPackage());
	}

	/**
	 * Builds a {@link LibraryVersion} by reading the implementation version from the given anchor package.
	 *
	 * @param name the library name
	 * @param anchorPackage a package belonging to the library
	 * @return a new {@link LibraryVersion}
	 */
	public static LibraryVersion of(final String name, final Package anchorPackage) {
		if (null == anchorPackage) {
			// can happen if the class is loaded from a directory instead of a jar (when running from an IDE)
			return new LibraryVersion(name, null);
		}
		return new LibraryVersion(name, anchorPackage.getImplementationVersion());
	}

	/**
	 * Builds a {@link LibraryVersion} by reading the implementation version from the package of the class with the given
	 * name. If the class cannot be loaded the resulting version is {@code null}.
	 *
	 * @param name the library name
	 * @param anchorClassName the fully qualified name of a class belonging to the library
	 * @return a new {@link LibraryVersion}
	 */
	public static LibraryVersion of(final String name, final String anchorClassName) {
		Class<?> anchorClass = Classes.Safe.getOne(anchorClassName);
		return of(name, anchorClass);
	}

	/**
	 * Returns the library name.
	 *
	 * @return the library name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the detected runtime version.
	 *
	 * @return the detected runtime version, or {@code null} if it could not be determined
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Checks whether the detected runtime version is at least the given minimum version. If the runtime version could not
	 * be determined the check cannot be enforced and this method returns {@code true}.
	 *
	 * @param minimumVersion the minimum required version
	 * @return true if the runtime version is at least the minimum version, or if it could not be determined
	 */
	public boolean isAtLeast(final String minimumVersion) {
		return null == version || compare(version, minimumVersion) >= 0;
	}

	/**
	 * Verifies that the detected runtime version is at least the given minimum version. If the runtime version could not be
	 * determined the check is skipped since it cannot be reliably enforced.
	 *
	 * @param minimumVersion the minimum required version
	 * @throws IllegalStateException if the detected runtime version is older than the minimum version
	 */
	public void verifyAtLeast(final String minimumVersion) {
		if (!isAtLeast(minimumVersion)) {
			throw new IllegalStateException(
					Messages.message("Unsupported {} version: {}, minimum required version is {}", name, version, minimumVersion));
		}
	}

	/**
	 * Compares this library version with another by their detected versions, numerically (see
	 * {@link #compare(String, String)}), and by name if the detected versions are equal. Undetermined versions and names
	 * ({@code null}) sort first. The comparison is consistent with {@link #equals(Object)}.
	 *
	 * @param that the other library version
	 * @return a negative number if this library version is older than the other, a positive number if it is newer, or zero
	 * if they represent the same library version
	 */
	@Override
	public int compareTo(final LibraryVersion that) {
		int comparison = Objects.compare(this.version, that.version, Comparator.nullsFirst(LibraryVersion::compare));
		if (0 != comparison) {
			return comparison;
		}
		return Objects.compare(this.name, that.name, Comparator.nullsFirst(Comparator.naturalOrder()));
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof LibraryVersion that) {
			return Objects.equals(this.name, that.name)
					&& Objects.equals(this.version, that.version);
		}
		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name, version);
	}

	/**
	 * Compares two dot separated numeric version strings (e.g. {@code "5.2.1"}), ignoring any non-numeric suffix (e.g.
	 * {@code "-SNAPSHOT"} or {@code "-alpha"}) on each component. Missing trailing components are treated as zero.
	 *
	 * @param version1 the first version to compare
	 * @param version2 the second version to compare
	 * @return a negative number if {@code version1} is older than {@code version2}, a positive number if it is newer, or
	 * zero if they represent the same version
	 */
	public static int compare(final String version1, final String version2) {
		String[] parts1 = version1.split("\\.");
		String[] parts2 = version2.split("\\.");
		int length = Math.max(parts1.length, parts2.length);
		for (int i = 0; i < length; ++i) {
			int part1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
			int part2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;
			int comparison = Integer.compare(part1, part2);
			if (0 != comparison) {
				return comparison;
			}
		}
		return 0;
	}

	/**
	 * Parses the leading digits of a version component, ignoring any non-numeric suffix.
	 *
	 * @param part the version component
	 * @return the numeric value of the leading digits, or zero if there are none
	 */
	private static int parseVersionPart(final String part) {
		int end = 0;
		while (end < part.length() && Character.isDigit(part.charAt(end))) {
			++end;
		}
		return end > 0 ? Integer.parseInt(part.substring(0, end)) : 0;
	}
}
