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
package org.morphix.reflection.jvm;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.morphix.reflection.ConstantPoolAccessor;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Methods;

/**
 * Class for accessing the Java Constant pool. Platform dependent (other JDKs might have different ways of accessing the
 * constant pool).
 * <p>
 * Note: This was only tested on JDK 8, 11, 17, 21.
 *
 * @author Radu Sebastian LAZIN
 */
public class ConstantPoolAccessorOracleJDK extends ConstantPoolAccessor { // NOSONAR

	/**
	 * Class names used in this class.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class ClassName {

		/**
		 * ConstantPool class name.
		 */
		static final String CONSTANT_POOL = "jdk.internal.reflect.ConstantPool";

		/**
		 * SharedSecrets class name.
		 */
		static final String SHARED_SECRETS = "jdk.internal.access.SharedSecrets";

		/**
		 * Hide constructor.
		 */
		private ClassName() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * getConstantPool method.
	 */
	private static Method getConstantPoolMethod;

	/**
	 * getSize method.
	 */
	private static Method getSizeMethod;

	/**
	 * getMethodAd method.
	 */
	private static Method getMethodAtMethod;

	static {
		initializeMethods(getConstantPoolClassName());
	}

	/**
	 * Default constructor.
	 */
	ConstantPoolAccessorOracleJDK() {
		// empty
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
	 * Initializes all the static variables based on a given constant pool class name.
	 *
	 * @param constantPoolClassName see {@link #getConstantPoolClassName()}
	 */
	static void initializeMethods(final String constantPoolClassName) {
		try {
			Class<?> internalConstantPoolClass = Class.forName(constantPoolClassName);

			Class<?> sharedSecretsClass = Class.forName(ClassName.SHARED_SECRETS);
			Method javaLangAccessGetter = sharedSecretsClass.getMethod("getJavaLangAccess");

			try (MemberAccessorOracleJDK<Method> methodAccessor = new MemberAccessorOracleJDK<>(javaLangAccessGetter)) {
				Object javaLangAccess = javaLangAccessGetter.invoke(null);
				getConstantPoolMethod = javaLangAccess.getClass().getMethod("getConstantPool", Class.class);
			}

			getSizeMethod = internalConstantPoolClass.getDeclaredMethod("getSize");
			getMethodAtMethod = internalConstantPoolClass.getDeclaredMethod("getMethodAt", int.class);
		} catch (Throwable e) { // NOSONAR
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
		return ClassName.CONSTANT_POOL;
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
			return Methods.IgnoreAccess.invoke(getConstantPoolMethod, cls);
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
			return Methods.IgnoreAccess.invoke(getSizeMethod, constantPool);
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
	protected Member getMemberAt(final Object constantPool, final int index) {
		try {
			return Methods.IgnoreAccess.invoke(getMethodAtMethod, constantPool, index);
		} catch (Exception e) {
			return super.getMemberAt(constantPool, index);
		}
	}

	/**
	 * Instance holder for lazy initialization and thread safety.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Singleton instance.
		 */
		private static final ConstantPoolAccessorOracleJDK INSTANCE = new ConstantPoolAccessorOracleJDK();

		/**
		 * Private constructor.
		 */
		private InstanceHolder() {
			// empty
		}
	}

}
