package org.morphix.lang;

/**
 * Interface for copyable objects. This interface can be implemented by classes that want to provide a way to create a
 * copy of their instances. This is different from the standard Java {@link Cloneable} interface, as it specifies a
 * method that returns a copy of the object, rather than relying on the clone() method and the associated pitfalls.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Copyable {

	/**
	 * Creates and returns a copy of this object. The exact type of the returned object may be the same as the original or a
	 * different type, depending on the implementation.
	 *
	 * @return a copy of this object
	 */
	Copyable copy();
}
