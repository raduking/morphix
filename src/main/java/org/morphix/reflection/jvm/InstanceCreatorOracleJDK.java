package org.morphix.reflection.jvm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.morphix.reflection.Constructors;
import org.morphix.reflection.InstanceCreator;
import org.morphix.reflection.Methods;
import org.morphix.reflection.ReflectionException;

/**
 * Tries to create an instance even when a default constructor is not available.
 * <p>
 * Note: platform dependent (Oracle JDK/JRE). This was only tested on JDK 8.
 *
 * @author Radu Sebastian LAZIN
 */
public class InstanceCreatorOracleJDK extends InstanceCreator {

	static final String REFLECTION_FACTORY_CLASS_NAME = "sun.reflect.ReflectionFactory";

	private static Method newConstructorForSerializationMethod = null;

	private static Constructor<Object> objectConstructor = null;
	private static Object reflectionFactory = null;

	static {
		initializeMethods(REFLECTION_FACTORY_CLASS_NAME);
	}

	/**
	 * Returns true if this instance creator is usable to create objects without
	 * default constructors.
	 *
	 * @return true if this instance creator is usable, false otherwise
	 */
	@Override
	public boolean isUsable() {
		return newConstructorForSerializationMethod != null;
	}

	/**
	 * Creates a new instance of type T even if T doesn't have a constructor.
	 * Returns null if the instance couldn't be created.
	 *
	 * @param cls class to instantiate
	 * @return object of type T
	 */
	@Override
	public <T> T newInstance(final Class<T> cls) {
		Constructor<T> serializationConstructor = newConstructorForSerialization(cls);
		return Constructors.newInstanceIgnoreAccess(serializationConstructor);
	}

	/**
	 * Returns a new constructor for serialization for the given class.
	 *
	 * @param cls class to get the constructor for serialization for
	 * @return a new constructor for serialization for the given class
	 */
	<T> Constructor<T> newConstructorForSerialization(final Class<T> cls) {
		try {
			return Methods.invokeIgnoreAccess(newConstructorForSerializationMethod, reflectionFactory, cls, objectConstructor);
		} catch (Exception e) {
			throw new ReflectionException("Could not create constructor for serialization.", e);
		}
	}

	/**
	 * Initializes the static members of this class.
	 *
	 * @param reflectionFactoryClassName reflection factory class name
	 */
	static void initializeMethods(final String reflectionFactoryClassName) {
		try {
			Class<?> reflectionFactoryClass = Class.forName(reflectionFactoryClassName);
			Method getReflectionFactoryMethod = reflectionFactoryClass.getDeclaredMethod("getReflectionFactory");

			objectConstructor = Object.class.getConstructor();
			reflectionFactory = Methods.invokeIgnoreAccess(getReflectionFactoryMethod, null);
			newConstructorForSerializationMethod =
					reflectionFactoryClass.getDeclaredMethod("newConstructorForSerialization", Class.class, Constructor.class);
		} catch (Exception e) {
			// swallow exception since it just won't create instances if
			// the actual JDK/JRE doesn't support it this way
		}
	}

	/**
	 * Returns the singleton instance of this class.
	 *
	 * @return the singleton instance of this class
	 */
	public static InstanceCreatorOracleJDK getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Private constructor.
	 */
	private InstanceCreatorOracleJDK() {
		// empty
	}

	/**
	 * Instance holder for lazy initialization and thread safety.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static final class InstanceHolder {

		private static final InstanceCreatorOracleJDK INSTANCE = new InstanceCreatorOracleJDK();

		private InstanceHolder() {
			// empty
		}
	}
}
