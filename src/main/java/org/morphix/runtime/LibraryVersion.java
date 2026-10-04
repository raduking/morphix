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
import java.util.function.Consumer;

import org.morphix.lang.JavaArrays;
import org.morphix.lang.Messages;
import org.morphix.lang.Nullables;

/**
 * Represents the runtime version of a library, as reported by the {@linkplain Package#getImplementationVersion()
 * implementation version} of an anchor class from that library. This is typically used to guard against optional or
 * provided dependencies (declared with {@code provided}/{@code optional} scope) being resolved to a version older than
 * the one the depending code actually requires at runtime.
 * <p>
 * If the runtime version cannot be determined (e.g. missing manifest information, or the anchor class is not present on
 * the classpath), {@link #value()} returns {@code null} and {@link #isAtLeast(String)}/{@link #verifyAtLeast(String)}
 * treat the check as unenforceable rather than failing.
 *
 * @author Radu Sebastian LAZIN
 */
public class LibraryVersion implements Comparable<LibraryVersion> {

	/**
	 * The number of semantic version components (major, minor, patch) taken into account when comparing versions.
	 */
	private static final int COMPONENTS = 3;

	/**
	 * The label used in error messages when the detected version is older than the minimum required version.
	 */
	private static final String MAXIMUM = "maximum";

	/**
	 * The label used in error messages when the detected version is newer than the maximum required version.
	 */
	private static final String MINIMUM = "minimum";

	/**
	 * The library name, used only for diagnostic messages.
	 */
	private final String name;

	/**
	 * The detected runtime version, or {@code null} if it could not be determined.
	 */
	private final String version;

	/**
	 * The major version component, as per semantic versioning. Zero if the version could not be determined.
	 */
	private final int major;

	/**
	 * The minor version component, as per semantic versioning. Zero if the version could not be determined.
	 */
	private final int minor;

	/**
	 * The patch version component, as per semantic versioning. Zero if the version could not be determined.
	 */
	private final int patch;

	/**
	 * Constructor with the library name and its detected runtime version.
	 *
	 * @param name the library name
	 * @param version the detected runtime version, may be {@code null}
	 */
	protected LibraryVersion(final String name, final String version) {
		this.name = name;
		this.version = version;
		String[] parts = Nullables.apply(version, v -> v.split("\\."), () -> JavaArrays.empty(String.class));
		this.major = parseVersionPart(parts, 0);
		this.minor = parseVersionPart(parts, 1);
		this.patch = parseVersionPart(parts, 2);
	}

	/**
	 * Constructor with the library name and its semantic version components. The detected version is the canonical
	 * {@code <major>.<minor>.<patch>} representation of the given components.
	 *
	 * @param name the library name
	 * @param major the major version component
	 * @param minor the minor version component
	 * @param patch the patch version component
	 */
	protected LibraryVersion(final String name, final int major, final int minor, final int patch) {
		this.name = name;
		this.version = major + "." + minor + "." + patch;
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}

	/**
	 * Constructor with the library name only, for cases where the version cannot be determined.
	 *
	 * @param name the library name
	 */
	protected LibraryVersion(final String name) {
		this(name, null);
	}

	/**
	 * Builds a {@link LibraryVersion} by reading the implementation version from the given anchor class' package.
	 *
	 * @param name the library name
	 * @param anchorClass a class belonging to the library
	 * @return a new {@link LibraryVersion}
	 */
	public static LibraryVersion of(final String name, final Class<?> anchorClass) {
		return Nullables.apply(anchorClass, c -> of(name, c.getPackage()), () -> new LibraryVersion(name));
	}

	/**
	 * Builds a {@link LibraryVersion} by reading the implementation version from the given anchor package.
	 * <p>
	 * Note: anchor packages are typically obtained from an anchor class, e.g. {@code MyClass.class.getPackage()} and can be
	 * {@code null} if the class is loaded from a directory instead of a jar (when running from an IDE).
	 *
	 * @param name the library name
	 * @param anchorPackage a package belonging to the library
	 * @return a new {@link LibraryVersion}
	 */
	public static LibraryVersion of(final String name, final Package anchorPackage) {
		return Nullables.apply(anchorPackage, p -> new LibraryVersion(name, p.getImplementationVersion()), () -> new LibraryVersion(name));
	}

	/**
	 * Builds a {@link LibraryVersion} from the given version string.
	 *
	 * @param name the library name
	 * @param version the version string, may be {@code null} if the version could not be determined
	 * @return a new {@link LibraryVersion}
	 */
	public static LibraryVersion of(final String name, final String version) {
		return new LibraryVersion(name, version);
	}

	/**
	 * Builds a {@link LibraryVersion} from the given semantic version components.
	 *
	 * @param name the library name
	 * @param major the major version component
	 * @param minor the minor version component
	 * @param patch the patch version component
	 * @return a new {@link LibraryVersion}
	 */
	public static LibraryVersion of(final String name, final int major, final int minor, final int patch) {
		return new LibraryVersion(name, major, minor, patch);
	}

	/**
	 * Builds a {@link LibraryVersion} from the given semantic version components.
	 *
	 * @param name the library name
	 * @param major the major version component
	 * @param minor the minor version component
	 * @return a new {@link LibraryVersion}
	 */
	public static LibraryVersion of(final String name, final int major, final int minor) {
		return new LibraryVersion(name, major, minor, 0);
	}

	/**
	 * Builds a {@link LibraryVersion} from the given semantic version components.
	 *
	 * @param name the library name
	 * @param major the major version component
	 * @return a new {@link LibraryVersion}
	 */
	public static LibraryVersion of(final String name, final int major) {
		return new LibraryVersion(name, major, 0, 0);
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
	 * Returns the raw version string of this library version, exactly as it was detected or as it was provided, or
	 * {@code null} if it could not be determined.
	 * <p>
	 * Depending on how this instance was built, this is either:
	 * <ul>
	 * <li>the {@linkplain Package#getImplementationVersion() implementation version} reported by the anchor
	 * {@linkplain #of(String, Class) class} or {@linkplain #of(String, Package) package}, which is {@code null} when it is
	 * unavailable (e.g. the library was not loaded from a jar, as when running from an IDE);</li>
	 * <li>the string given to {@link #of(String, String)}, returned verbatim;</li>
	 * <li>the canonical {@code <major>.<minor>.<patch>} form built by {@link #of(String, int, int, int)} and its overloads,
	 * which is never {@code null}.</li>
	 * </ul>
	 * <p>
	 * This string is <b>not</b> normalized: it may keep a non-numeric suffix (e.g. {@code "5.4.3-SNAPSHOT"}), may declare
	 * fewer than three components (e.g. {@code "5"}) or more than the three semantic ones (e.g. {@code "1.2.3.4"}), and
	 * therefore generally differs from {@link #toString()}. Use it for diagnostics, such as error messages and logging,
	 * where reporting exactly what is deployed matters. To compare versions use {@link #isAtLeast(String)} or
	 * {@link #isAtLeast(LibraryVersion)}, which ignore suffixes and missing components.
	 *
	 * @return the raw version string, or {@code null} if it could not be determined
	 * @see #toString()
	 */
	public String value() {
		return version;
	}

	/**
	 * Returns the major version component, as per semantic versioning.
	 *
	 * @return the major version component, or zero if the version could not be determined
	 */
	public int major() {
		return major;
	}

	/**
	 * Returns the minor version component, as per semantic versioning.
	 *
	 * @return the minor version component, or zero if the version could not be determined
	 */
	public int minor() {
		return minor;
	}

	/**
	 * Returns the patch version component, as per semantic versioning.
	 *
	 * @return the patch version component, or zero if the version could not be determined
	 */
	public int patch() {
		return patch;
	}

	/**
	 * Checks whether the detected runtime version is at least the given minimum version. If the runtime version could not
	 * be determined the check cannot be enforced and this method returns {@code true}.
	 *
	 * @param minimumVersion the minimum required version
	 * @return true if the runtime version is at least the minimum version, or if it could not be determined
	 */
	public boolean isAtLeast(final String minimumVersion) {
		if (null == version) {
			return true;
		}
		String[] parts = minimumVersion.split("\\.");
		return compareTo(parseVersionPart(parts, 0), parseVersionPart(parts, 1), parseVersionPart(parts, 2)) >= 0;
	}

	/**
	 * Checks whether the detected runtime version is at least the given minimum version. If the runtime version could not
	 * be determined the check cannot be enforced and this method returns {@code true}.
	 *
	 * @param minimumVersion the minimum required version
	 * @return true if the runtime version is at least the minimum version, or if it could not be determined
	 */
	public boolean isAtLeast(final LibraryVersion minimumVersion) {
		Objects.requireNonNull(minimumVersion, "minimumVersion must not be null");
		return null == version || compareTo(minimumVersion.major, minimumVersion.minor, minimumVersion.patch) >= 0;
	}

	/**
	 * Verifies that the detected runtime version is at least the given minimum version. If the runtime version could not be
	 * determined the check is skipped since it cannot be reliably enforced.
	 *
	 * @param minimumVersion the minimum required version
	 * @throws IllegalStateException if the detected runtime version is older than the minimum version
	 */
	public void verifyAtLeast(final String minimumVersion) {
		verifyAtLeast(minimumVersion, message -> {
			throw new IllegalStateException(message);
		});
	}

	/**
	 * Verifies that the detected runtime version is at least the given minimum version. If the runtime version could not be
	 * determined the check is skipped since it cannot be reliably enforced.
	 *
	 * @param minimumVersion the minimum required version
	 * @throws IllegalStateException if the detected runtime version is older than the minimum version
	 */
	public void verifyAtLeast(final LibraryVersion minimumVersion) {
		verifyAtLeast(minimumVersion, message -> {
			throw new IllegalStateException(message);
		});
	}

	/**
	 * Verifies that the detected runtime version is at least the given minimum version. If the runtime version could not be
	 * determined the check is skipped since it cannot be reliably enforced.
	 *
	 * @param minimumVersion the minimum required version
	 * @param onError a callback to handle the error message if the detected runtime version is older than the minimum
	 *     version
	 */
	public void verifyAtLeast(final String minimumVersion, final Consumer<String> onError) {
		if (!isAtLeast(minimumVersion)) {
			onError.accept(errorMessage(MINIMUM, minimumVersion));
		}
	}

	/**
	 * Verifies that the detected runtime version is at least the given minimum version. If the runtime version could not be
	 * determined the check is skipped since it cannot be reliably enforced.
	 *
	 * @param minimumVersion the minimum required version
	 * @param onError a callback to handle the error message if the detected runtime version is older than the minimum
	 *     version
	 */
	public void verifyAtLeast(final LibraryVersion minimumVersion, final Consumer<String> onError) {
		if (!isAtLeast(minimumVersion)) {
			onError.accept(errorMessage(MINIMUM, minimumVersionString(minimumVersion)));
		}
	}

	/**
	 * Checks whether the detected runtime version is at most the given maximum version. If the runtime version could not be
	 * determined the check cannot be enforced and this method returns {@code true}.
	 *
	 * @param maximumVersion the maximum required version
	 * @return true if the runtime version is at most the maximum version, or if it could not be determined
	 */
	public boolean isAtMost(final String maximumVersion) {
		if (null == version) {
			return true;
		}
		String[] parts = maximumVersion.split("\\.");
		return compareTo(parseVersionPart(parts, 0), parseVersionPart(parts, 1), parseVersionPart(parts, 2)) <= 0;
	}

	/**
	 * Checks whether the detected runtime version is at most the given maximum version. If the runtime version could not be
	 * determined the check cannot be enforced and this method returns {@code true}.
	 *
	 * @param maximumVersion the maximum required version
	 * @return true if the runtime version is at most the maximum version, or if it could not be determined
	 */
	public boolean isAtMost(final LibraryVersion maximumVersion) {
		Objects.requireNonNull(maximumVersion, "maximumVersion must not be null");
		return null == version || compareTo(maximumVersion.major, maximumVersion.minor, maximumVersion.patch) <= 0;
	}

	/**
	 * Verifies that the detected runtime version is at most the given maximum version. If the runtime version could not be
	 * determined the check is skipped since it cannot be reliably enforced.
	 *
	 * @param maximumVersion the maximum required version
	 * @throws IllegalStateException if the detected runtime version is newer than the maximum version
	 */
	public void verifyAtMost(final String maximumVersion) {
		verifyAtMost(maximumVersion, message -> {
			throw new IllegalStateException(message);
		});
	}

	/**
	 * Verifies that the detected runtime version is at most the given maximum version. If the runtime version could not be
	 * determined the check is skipped since it cannot be reliably enforced.
	 *
	 * @param maximumVersion the maximum required version
	 * @throws IllegalStateException if the detected runtime version is newer than the maximum version
	 */
	public void verifyAtMost(final LibraryVersion maximumVersion) {
		verifyAtMost(maximumVersion, message -> {
			throw new IllegalStateException(message);
		});
	}

	/**
	 * Verifies that the detected runtime version is at most the given maximum version. If the runtime version could not be
	 * determined the check is skipped since it cannot be reliably enforced.
	 *
	 * @param maximumVersion the maximum required version
	 * @param onError a callback to handle the error message if the detected runtime version is newer than the maximum
	 *     version
	 */
	public void verifyAtMost(final String maximumVersion, final Consumer<String> onError) {
		if (!isAtMost(maximumVersion)) {
			onError.accept(errorMessage(MAXIMUM, maximumVersion));
		}
	}

	/**
	 * Verifies that the detected runtime version is at most the given maximum version. If the runtime version could not be
	 * determined the check is skipped since it cannot be reliably enforced.
	 *
	 * @param maximumVersion the maximum required version
	 * @param onError a callback to handle the error message if the detected runtime version is newer than the maximum
	 *     version
	 */
	public void verifyAtMost(final LibraryVersion maximumVersion, final Consumer<String> onError) {
		if (!isAtMost(maximumVersion)) {
			onError.accept(errorMessage(MAXIMUM, minimumVersionString(maximumVersion)));
		}
	}

	/**
	 * Returns the error message for a failed version check against the given maximum version.
	 *
	 * @param label the label to use in the message, e.g. "maximum" or "minimum"
	 * @param actualVersion the maximum required version, as displayed in the message
	 * @return the error message
	 */
	private String errorMessage(final String label, final String actualVersion) {
		return Messages.message("Unsupported {} version: {}, {} required version is {}", name, version, label, actualVersion);
	}

	/**
	 * Returns the detected version of the given minimum version, falling back to its semantic representation if it cannot
	 * be determined. Used only for diagnostics, the check itself always compares the already parsed components.
	 *
	 * @param minimumVersion the minimum required version, must not be null
	 * @return the minimum required version as a string
	 */
	private static String minimumVersionString(final LibraryVersion minimumVersion) {
		return Nullables.apply(minimumVersion, LibraryVersion::value, minimumVersion::toString);
	}

	/**
	 * Compares this library version with another by their detected versions, numerically (see
	 * {@link #compare(String, String)}), and by name if the detected versions compare as equal. Undetermined versions and
	 * names ({@code null}) sort first.
	 * <p>
	 * This ordering is <b>not</b> necessarily consistent with {@link #equals(Object)}: {@link #equals(Object)} compares the
	 * raw detected version strings, while this method compares them numerically, so e.g. {@code "5.5"} and {@code "5.5.0"}
	 * compare as equal here even though they are not {@link #equals(Object)}.
	 *
	 * @param that the other library version
	 * @return a negative number if this library version is older than the other, a positive number if it is newer, or zero
	 * if they represent the same library version
	 */
	@Override
	public int compareTo(final LibraryVersion that) {
		Objects.requireNonNull(that, "that must not be null");
		boolean thisUndetermined = null == this.version;
		boolean thatUndetermined = null == that.version;
		int comparison = thisUndetermined || thatUndetermined
				? Boolean.compare(thatUndetermined, thisUndetermined)
				: compareTo(that.major, that.minor, that.patch);
		if (0 != comparison) {
			return comparison;
		}
		return Objects.compare(this.name, that.name, Comparator.nullsFirst(Comparator.naturalOrder()));
	}

	/**
	 * Compares the already parsed version components of this library version with the given ones, ignoring the library
	 * names and whether the versions could be determined at all.
	 *
	 * @param otherMajor the major version component to compare with
	 * @param otherMinor the minor version component to compare with
	 * @param otherPatch the patch version component to compare with
	 * @return a negative number if this library version is older, a positive number if it is newer, or zero if the
	 * components represent the same version
	 */
	private int compareTo(final int otherMajor, final int otherMinor, final int otherPatch) {
		int comparison = Integer.compare(major, otherMajor);
		if (0 != comparison) {
			return comparison;
		}
		comparison = Integer.compare(minor, otherMinor);
		if (0 != comparison) {
			return comparison;
		}
		return Integer.compare(patch, otherPatch);
	}

	/**
	 * Checks whether the given object is a {@link LibraryVersion} with the same {@linkplain #getName() library name} and
	 * the same {@linkplain #value() raw version string} as this one.
	 * <p>
	 * Equality is representational, not semantic: it holds only for versions built the same way.
	 * <p>
	 * Two instances describing the same semantic version are NOT equal whenever their raw version strings differ:
	 * <ul>
	 * <li>{@code "5.5"} and {@code "5.5.0"} — a missing component;</li>
	 * <li>{@code "5.4.3"} and {@code "5.4.3-SNAPSHOT"} — a non-numeric suffix;</li>
	 * <li>{@code "1.2.3"} and {@code "1.2.3.4"} — components beyond the three semantic ones;</li>
	 * <li>{@code "5"} and {@code "5.0.0"} — as built by {@link #of(String, String)} and {@link #of(String, int)}
	 * respectively.</li>
	 * </ul>
	 * The parsed components are never consulted, so {@link #major()}, {@link #minor()} and {@link #patch()} may all agree
	 * while the two instances are still unequal. Conversely, the library name is compared as well, so the same version of
	 * two different libraries is never equal.
	 * <p>
	 * To compare versions semantically use {@link #compareTo(LibraryVersion)}, which orders them by the parsed components
	 * and the name and is <b>not</b> consistent with this method, or {@link #isAtLeast(String)} and
	 * {@link #isAtLeast(LibraryVersion)} to check them against a minimum version.
	 *
	 * @see #hashCode()
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
	 * Returns a hash code consistent with {@link #equals(Object)}, derived from the library name and the
	 * {@linkplain #value() raw version string}. Versions that are unequal only because they were built from differently
	 * spelled strings still have different hash codes.
	 *
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name, version);
	}

	/**
	 * Returns the normalized semantic version of this library version as {@code <major>.<minor>.<patch>}, always with
	 * exactly three numeric components and never {@code null}.
	 * <p>
	 * In contrast to {@link #value()}, any non-numeric suffix is dropped ({@code "5.4.3-SNAPSHOT"} becomes
	 * {@code "5.4.3"}), missing components are padded with zeros ({@code "5"} becomes {@code "5.0.0"}), and components
	 * beyond the three semantic ones are dropped ({@code "1.2.3.4"} becomes {@code "1.2.3"}). An undetermined version is
	 * rendered as {@code "0.0.0"}.
	 * <p>
	 * Use it for display and whenever a stable, comparable textual form is needed. Do not use it to test equality:
	 * {@link #equals(Object)} compares the raw version strings, so {@code "5.5"} and {@code "5.5.0"} are not equal even
	 * though both render as {@code "5.5.0"}. Use {@link #equals(Object)} to test equality and
	 * {@link #compareTo(LibraryVersion)} to order versions.
	 *
	 * @see Object#toString()
	 * @see #value()
	 */
	@Override
	public String toString() {
		return major + "." + minor + "." + patch;
	}

	/**
	 * Compares two dot separated numeric version strings (e.g. {@code "5.2.1"}), ignoring any non-numeric suffix (e.g.
	 * {@code "-SNAPSHOT"} or {@code "-alpha"}) on each component. Missing trailing components are treated as zero, and any
	 * components beyond the {@value #COMPONENTS} semantic ones (major, minor, patch) are ignored.
	 *
	 * @param version1 the first version to compare
	 * @param version2 the second version to compare
	 * @return a negative number if {@code version1} is older than {@code version2}, a positive number if it is newer, or
	 * zero if they represent the same version
	 */
	public static int compare(final String version1, final String version2) {
		return compare(version1.split("\\."), version2.split("\\."));
	}

	/**
	 * Compares the first {@value #COMPONENTS} components of two dot separated version strings, ignoring any non-numeric
	 * suffix. Missing components are treated as zero.
	 *
	 * @param parts1 the first version to compare, split into components
	 * @param parts2 the second version to compare, split into components
	 * @return a negative number if {@code parts1} is older than {@code parts2}, a positive number if it is newer, or zero
	 * if they represent the same version
	 */
	private static int compare(final String[] parts1, final String[] parts2) {
		for (int i = 0; i < COMPONENTS; ++i) {
			int comparison = Integer.compare(parseVersionPart(parts1, i), parseVersionPart(parts2, i));
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

	/**
	 * Parses the leading digits of a version component at the given index in a dot separated version string, ignoring any
	 * non-numeric suffix. Missing trailing components are treated as zero.
	 *
	 * @param parts the dot separated version string split into components
	 * @param index the index of the component to parse
	 * @return the numeric value of the leading digits, or zero if there are none or if the component is missing
	 */
	private static int parseVersionPart(final String[] parts, final int index) {
		return parts.length > index ? parseVersionPart(parts[index]) : 0;
	}
}
