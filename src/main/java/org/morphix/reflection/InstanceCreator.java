/*
 * Copyright 2025 the original author or authors.
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

import java.util.stream.Stream;

import org.morphix.reflection.jvm.InstanceCreatorOracleJDK;

/**
 * Tries to create an instance even when a default constructor is not available.
 *
 * @author Radu Sebastian LAZIN
 */
public class InstanceCreator {

	/**
	 * Default constructor.
	 */
	public InstanceCreator() {
		// empty
	}

	/**
	 * Returns true if this instance creator is usable to create objects without default constructors.
	 *
	 * @return true if this instance creator is usable, false otherwise
	 */
	public boolean isUsable() {
		return false;
	}

	/**
	 * Creates a new instance of type T even if T doesn't have a constructor. Throws {@link ReflectionException} if the
	 * instance couldn't be created.
	 *
	 * @param <T> instance type
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

		/**
		 * Holds an array of {@link InstanceCreator} implementations.
		 */
		private static final InstanceCreator[] IMPLEMENTATIONS = {
				InstanceCreatorOracleJDK.getInstance()
		};

		/**
		 * Initializes the singleton for the instance creator from the usable implementations.
		 */
		private static InstanceCreator instance =
				Stream.of(IMPLEMENTATIONS).filter(InstanceCreator::isUsable).findFirst()
						.orElse(new InstanceCreator());

		/**
		 * Private constructor.
		 */
		private InstanceHolder() {
			// empty
		}
	}

}
