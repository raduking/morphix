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

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Holds a serializable value of type <code>T</code>. This class is useful for legacy/native APIs to be used as output
 * parameters in methods.
 *
 * @param <T> serializable value type
 *
 * @author Radu Sebastian LAZIN
 */
public final class SerializableHolder<T extends Serializable> implements Serializable {

	/**
	 * Serial version UID.
	 */
	@Serial
	private static final long serialVersionUID = -3646862166951388160L;

	/**
	 * The value contained in the holder.
	 */
	private T value;

	/**
	 * Creates a new holder with a <code>null</code> value.
	 */
	public SerializableHolder() {
		// empty
	}

	/**
	 * Creates a new holder with the specified value.
	 *
	 * @param value value to be stored in the holder
	 */
	public SerializableHolder(final T value) {
		setValue(value);
	}

	/**
	 * Creates a new holder with no value.
	 *
	 * @param <U> value type
	 * @return holder
	 */
	public static <U extends Serializable> SerializableHolder<U> empty() {
		return of(null);
	}

	/**
	 * Creates a new holder with the given value.
	 *
	 * @param <U> value type
	 * @param value value to hold
	 * @return holder
	 */
	public static <U extends Serializable> SerializableHolder<U> of(final U value) {
		return new SerializableHolder<>(value);
	}

	/**
	 * Returns the held value.
	 *
	 * @return the held value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Sets the held value.
	 *
	 * @param value value to hold
	 */
	public void setValue(final T value) {
		this.value = value;
	}

	/**
	 * Alias for {@link #getValue()}.
	 *
	 * @return the held value
	 */
	public T get() {
		return getValue();
	}

	/**
	 * Alias for {@link #setValue(Serializable)}.
	 *
	 * @param value value to hold
	 */
	public void set(final T value) {
		setValue(value);
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof SerializableHolder<?> that) {
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
