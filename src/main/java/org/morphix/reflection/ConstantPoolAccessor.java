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
package org.morphix.reflection;

import java.lang.reflect.Member;
import java.util.stream.Stream;

import org.morphix.reflection.jvm.ConstantPoolAccessorOracleJDK;

/**
 * Accesses the java constant pool.
 *
 * @author Radu Sebastian LAZIN
 */
public class ConstantPoolAccessor { // NOSONAR we want this as singleton

	/**
	 * Default constant pool size.
	 */
	protected static final int DEFAULT_SIZE = 0;

	/**
	 * Default constructor.
	 */
	public ConstantPoolAccessor() {
		// empty
	}

	/**
	 * Returns true if this constant pool accessor is usable to access the java constant pool.
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
	protected Member getMemberAt(final Object constantPool, final int index) {
		return null;
	}

	/**
	 * Instance holder for lazy initialization and thread safety.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static final class InstanceHolder {

		/**
		 * Holds an array of {@link ConstantPoolAccessor} implementations.
		 */
		private static final ConstantPoolAccessor[] IMPLEMENTATIONS = {
				ConstantPoolAccessorOracleJDK.getInstance()
		};

		/**
		 * Initializes the singleton for the constant pool accessor from the usable implementations.
		 */
		private static final ConstantPoolAccessor INSTANCE = Stream.of(IMPLEMENTATIONS)
				.filter(ConstantPoolAccessor::isUsable)
				.findFirst()
				.orElseGet(ConstantPoolAccessor::new);

		/**
		 * Private constructor.
		 */
		private InstanceHolder() {
			// empty
		}
	}

}
