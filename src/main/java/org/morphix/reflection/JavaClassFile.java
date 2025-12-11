package org.morphix.reflection;

/**
 * Represents a class file version with major and minor components.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JavaClassFile {

	/**
	 * The class file extension.
	 */
	static final String EXTENSION = ".class";

	/**
	 * The magic number that identifies a class file.
	 */
	static final int CAFEBABE = 0xCAFEBABE;

	/**
	 * Represents a class file version with major and minor components.
	 *
	 * @param major the major version number
	 * @param minor the minor version number
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public record Version(int major, int minor) {

		/**
		 * The magic offset used to calculate the major version from the Java version.
		 */
		public static final int MAGIC_OFFSET = 44;

		/**
		 * Creates a Version instance from a given Java version.
		 *
		 * @param javaVersion the Java version
		 * @return the corresponding class file Version
		 */
		public static Version fromJavaVersion(final int javaVersion) {
			int major = MAGIC_OFFSET + javaVersion;
			int minor = 0;
			return new Version(major, minor);
		}
	}
}
