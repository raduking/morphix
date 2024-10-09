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

import java.lang.reflect.Constructor;

/**
 * Helper class for accessing constructors that are not accessible.
 *
 * @param <T> type for which the constructor refers to
 *
 * @author Radu Sebastian LAZIN
 */
public class ConstructorAccessor<T> extends MemberAccessor<Constructor<T>> {

	/**
	 * Constructor for accessing an unaccessible constructor.
	 *
	 * @param constructor unaccessible constructor
	 */
	public ConstructorAccessor(final Constructor<T> constructor) {
		super(null, constructor);
	}

}
