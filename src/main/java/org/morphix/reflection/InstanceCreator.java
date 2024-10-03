package org.morphix.reflection;

import java.util.stream.Stream;

import org.morphix.reflection.jvm.InstanceCreatorOracleJDK;

/**
 * Tries to create an instance even when a default constructor is not available.
 *
 * @author Radu Sebastian LAZIN
 */
public class InstanceCreator {

	/**
	 * Returns true if this instance creator is usable to create
	 * objects without default constructors.
	 *
	 * @return true if this instance creator is usable, false otherwise
	 */
	public boolean isUsable() {
		return false;
	}

	/**
	 * Creates a new instance of type T even if T doesn't have a constructor.
	 * Throws {@link ReflectionException} if the instance couldn't be created.
	 *
	 * @param cls class to instantiate
	 * @return object of type T
	 */
	public <T> T newInstance(final Class<T> cls) {
		throw new ReflectionException("InstanceCreator is not supported by this JDK.");
	}

	/**
	 * Returns the singleton instance of this class.
	 *
	 * @return the singleton instance of this class
	 */
	public static InstanceCreator getInstance() {
		return InstanceHolder.instance;
	}

	/**
	 * Sets the singleton instance.
	 *
	 * @param instance instance to set
	 */
	public static void setInstance(final InstanceCreator instance) {
		InstanceHolder.instance = instance;
	}

	/**
	 * Instance holder for lazy initialization and thread safety.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static final class InstanceHolder {

		private static final InstanceCreator[] IMPLEMENTATIONS = {
				InstanceCreatorOracleJDK.getInstance()
		};

		private static InstanceCreator instance =
				Stream.of(IMPLEMENTATIONS).filter(InstanceCreator::isUsable).findFirst()
						.orElse(new InstanceCreator());

		private InstanceHolder() {
			// empty
		}
	}

}
