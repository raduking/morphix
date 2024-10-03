package org.morphix.reflection.jvm;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.morphix.reflection.ConstantPoolAccessor;
import org.morphix.reflection.Methods;

/**
 * Java Constant pool accessor (used in converter for Lambda parameter types).
 * Platform dependent (other JDKs might have different ways of accessing the
 * constant pool). This was only tested on JDK 8.
 *
 * @author Radu Sebastian LAZIN
 */
public class ConstantPoolAccessorOracleJDK extends ConstantPoolAccessor {

	static final String CONSTANT_POOL_CLASS_NAME = "jdk.internal.reflect.ConstantPool";
	static final String SHARED_SECRETS_CLASS_NAME = "jdk.internal.access.SharedSecrets";

	private static Method getConstantPoolMethod;
	private static Method getSizeMethod;
	private static Method getMethodAtMethod;

	static {
		initializeMethods(getConstantPoolClassName());
	}

	/**
	 * Returns true if this constant pool accessor is usable.
	 *
	 * @return true if this constant pool accessor is usable
	 */
	@Override
	public boolean isUsable() {
		return null != getMethodAtMethod;
	}

	/**
	 * Initializes all the static variables based on a given constant pool class
	 * name.
	 *
	 * @param constantPoolClassName see {@link #getConstantPoolClassName()}
	 */
	static void initializeMethods(final String constantPoolClassName) {
		try {
			Class<?> internalConstantPoolClass = Class.forName(constantPoolClassName);

			Class<?> sharedSecretsClass = Class.forName(SHARED_SECRETS_CLASS_NAME);
			Method javaLangAccessGetter = sharedSecretsClass.getMethod("getJavaLangAccess");

			try (MemberAccessorOracleJDK<Method> methodAccessor = new MemberAccessorOracleJDK<>(javaLangAccessGetter)) {
				Object javaLangAccess = javaLangAccessGetter.invoke(null);
				getConstantPoolMethod = javaLangAccess.getClass().getMethod("getConstantPool", Class.class);
			}

			getSizeMethod = internalConstantPoolClass.getDeclaredMethod("getSize");
			getMethodAtMethod = internalConstantPoolClass.getDeclaredMethod("getMethodAt", int.class);
		} catch (Throwable e) {
			// swallow exception since it just won't create instances if
			// the actual JDK/JRE doesn't support it this way
		}
	}

	/**
	 * Returns the constant pool class name.
	 *
	 * @return the constant pool class name
	 */
	public static String getConstantPoolClassName() {
		return CONSTANT_POOL_CLASS_NAME;
	}

	/**
	 * Returns the singleton instance of this class.
	 *
	 * @return the singleton instance of this class
	 */
	public static ConstantPoolAccessorOracleJDK getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Returns the constant pool object for the given class.
	 *
	 * @param cls class for which the constant pool is requested
	 * @return the constant pool object for the given class
	 */
	@Override
	protected Object getConstantPool(final Class<?> cls) {
		try {
			return Methods.invokeIgnoreAccess(getConstantPoolMethod, cls);
		} catch (Exception e) {
			return super.getConstantPool(cls);
		}
	}

	/**
	 * Returns the size of the constant pool.
	 *
	 * @param constantPool constant pool for which the size is returned
	 * @return the size of the constant pool
	 */
	@Override
	protected int getSize(final Object constantPool) {
		try {
			return Methods.invokeIgnoreAccess(getSizeMethod, constantPool);
		} catch (Exception e) {
			return super.getSize(constantPool);
		}
	}

	/**
	 * Returns the constant member at the given index from the constant pool.
	 *
	 * @param constantPool constant pool to get the constant member from
	 * @param index index in the constant pool
	 * @return the constant member at the given index from the constant pool
	 */
	@Override
	protected Member getMethodAt(final Object constantPool, final int index) {
		try {
			return Methods.invokeIgnoreAccess(getMethodAtMethod, constantPool, index);
		} catch (Exception e) {
			return super.getMethodAt(constantPool, index);
		}
	}

	/**
	 * Instance holder for lazy initialization and thread safety.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		private static final ConstantPoolAccessorOracleJDK INSTANCE = new ConstantPoolAccessorOracleJDK();

		private InstanceHolder() {
			// empty
		}
	}

}

