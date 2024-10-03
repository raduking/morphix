package org.morphix.reflection;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

/**
 * Functional interface for setting access for {@link AccessibleObject} or {@link Member}.
 *
 * @param <T> accessible object type
 *
 * @author Radu Sebastian LAZIN
 */
@FunctionalInterface
public interface AccessSetter<T extends AccessibleObject & Member> {

	/**
	 * Sets the accessible state of the object. Returns true if successful false otherwise.
	 *
	 * @param object object for which the access change is requested
	 * @param access accessible flag
	 * @return true if successful false otherwise
	 */
	boolean setAccessible(T object, boolean access);

	/**
	 * Creates an access setter for the override setter.
	 * The override setter is the setter for the {@link AccessibleObject}s "override" field.
	 *
	 * @param <T> accessible object type
	 *
	 * @param overrideSetter override setter
	 * @return access setter
	 */
	public static <T extends AccessibleObject & Member> AccessSetter<T> ofOverride(final MethodHandle overrideSetter) {
		requireNonNull(overrideSetter, "overrideSetter must not be null");
		return (object, value) -> {
			try {
				// this is the correct way to invoke the overrideSetter method so disable Sonar
				overrideSetter.invokeWithArguments(new Object[] { object, value }); // NOSONAR
				return true;
			} catch (Throwable t) {
				return false;
			}
		};
	}

}
