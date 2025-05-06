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
package org.morphix.lang;

/**
 * Java objects utility methods, contains several useful methods when working with objects. It is called JavaObjects
 * similar to {@link java.util.Objects} to avoid name collision.
 *
 * @author Radu Sebastian LAZIN
 */
public final class JavaObjects {

	/**
	 * Private constructor.
	 */
	private JavaObjects() {
		// empty
	}

	/**
	 * Casts the parameter to the required type. The advantage of this method is that no more {@link SuppressWarnings} is
	 * necessary and also the type is inferred by the compiler.
	 *
	 * @param <T> type to cast to
	 *
	 * @param o object to cast
	 * @return object cast to type T
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cast(final Object o) {
		return (T) o;
	}

	/**
	 * Returns maximum of two comparable objects.
	 *
	 * @param <T> objects type
	 * @param a first object
	 * @param b second object
	 * @return maximum
	 */
	public static <T extends Comparable<? super T>> T max(final T a, final T b) {
		if (null == a) {
			return b;
		}
		if (null == b) {
			return a;
		}
		return a.compareTo(b) > 0 ? a : b;
	}

	/**
	 * Returns minimum of two comparable objects.
	 *
	 * @param <T> objects type
	 * @param a first object
	 * @param b second object
	 * @return minimum
	 */
	public static <T extends Comparable<? super T>> T min(final T a, final T b) {
		if (null == a) {
			return b;
		}
		if (null == b) {
			return a;
		}
		return a.compareTo(b) < 0 ? a : b;
	}

}
