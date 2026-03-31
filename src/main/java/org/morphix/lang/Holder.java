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
package org.morphix.lang;

import java.util.Objects;

/**
 * Holds a value of type <code>T</code>. This class is useful for legacy/native APIs to be used as output parameters in
 * methods.
 * <p>
 * WARNING: this class is not thread-safe. It should be used only in single-threaded contexts or with external
 * synchronization.
 *
 * @param <T> value type
 *
 * @author Radu Sebastian LAZIN
 */
public final class Holder<T> {

	/**
	 * The value contained in the holder.
	 */
	private T value;

	/**
	 * Creates a new holder with a <code>null</code> value.
	 */
	public Holder() {
		// empty
	}

	/**
	 * Create a new holder with the specified value.
	 *
	 * @param value value to be stored in the holder
	 */
	public Holder(final T value) {
		setValue(value);
	}

	/**
	 * Returns the hold value.
	 *
	 * @return the hold value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Sets the hold value.
	 *
	 * @param value value to hold
	 */
	public void setValue(final T value) {
		this.value = value;
	}

	/**
	 * Creates a new holder with no value.
	 *
	 * @param <U> value type
	 *
	 * @return holder
	 */
	public static <U> Holder<U> empty() {
		return of(null);
	}

	/**
	 * Creates a new holder with the given value
	 *
	 * @param <U> value type
	 *
	 * @param value value to hold
	 * @return holder
	 */
	public static <U> Holder<U> of(final U value) {
		return new Holder<>(value);
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Holder<?> that) {
			return Objects.equals(this.value, that.value);
		}
		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}
}
