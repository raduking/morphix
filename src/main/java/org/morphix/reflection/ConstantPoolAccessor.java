package org.morphix.reflection;

import java.lang.reflect.Member;
import java.util.stream.Stream;

import org.morphix.reflection.jvm.ConstantPoolAccessorOracleJDK;

/**
 * Accesses the java constant pool.
 *
 * @author Radu Sebastian LAZIN
 */
public class ConstantPoolAccessor {

	protected static final int DEFAULT_SIZE = 0;

	/**
	 * Returns true if this constant pool accessor is usable to access the java
	 * constant pool.
	 *
	 * @return true if this constant pool accessor is usable, false otherwise
	 */
	public boolean isUsable() {
		return false;
	}

	/**
	 * Returns the singleton instance of this class.
	 *
	 * @return the singleton instance of this class
	 */
	public static ConstantPoolAccessor getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Returns the constant pool object for the given class.
	 *
	 * @param cls class for which the constant pool is requested
	 * @return the constant pool object for the given class
	 */
	protected Object getConstantPool(final Class<?> cls) {
		return null;
	}

	/**
	 * Returns the size of the constant pool.
	 *
	 * @param constantPool constant pool for which the size is returned
	 * @return the size of the constant pool
	 */
	protected int getSize(final Object constantPool) {
		return DEFAULT_SIZE;
	}

	/**
	 * Returns the constant member at the given index from the constant pool.
	 *
	 * @param constantPool constant pool to get the constant member from
	 * @param index index in the constant pool
	 * @return the constant member at the given index from the constant pool
	 */
	protected Member getMethodAt(final Object constantPool, final int index) {
		return null;
	}

	/**
	 * Instance holder for lazy initialization and thread safety.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static final class InstanceHolder {

		private static final ConstantPoolAccessor[] IMPLEMENTATIONS = {
				ConstantPoolAccessorOracleJDK.getInstance()
		};

		private static final ConstantPoolAccessor INSTANCE =
				Stream.of(IMPLEMENTATIONS).filter(ConstantPoolAccessor::isUsable).findFirst()
						.orElse(new ConstantPoolAccessor());

		private InstanceHolder() {
			// empty
		}
	}

}
