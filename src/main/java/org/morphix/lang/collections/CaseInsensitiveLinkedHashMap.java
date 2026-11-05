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
package org.morphix.lang.collections;

import java.io.Serial;
import java.util.Locale;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;

/**
 * A {@link KeyNormalizingLinkedHashMap} that normalizes keys using {@link String#toLowerCase(Locale)} and preserves
 * insertion order as well as the casing of keys as they were first inserted.
 *
 * @param <V> the value type
 *
 * @author Radu Sebastian LAZIN
 */
public class CaseInsensitiveLinkedHashMap<V> extends KeyNormalizingLinkedHashMap<V> {

	/**
	 * Serial version UID, required for {@link java.io.Serializable} classes.
	 */
	@Serial
	private static final long serialVersionUID = 8356182637897455059L;

	/**
	 * Locale used to normalize keys.
	 */
	private final Locale locale;

	/**
	 * Creates a map with the given expected size and locale.
	 *
	 * @param expectedSize expected number of entries, used as an initial capacity hint
	 * @param locale locale used for key normalization; {@link Locale#getDefault()} is used when {@code null}
	 */
	public CaseInsensitiveLinkedHashMap(final int expectedSize, final Locale locale) {
		super(expectedSize);
		this.locale = Nullables.nonNullOrDefault(locale, Locale::getDefault);
	}

	/**
	 * Creates a map with the given expected size and {@link Locale#getDefault()}.
	 *
	 * @param expectedSize expected number of entries, used as an initial capacity hint
	 */
	public CaseInsensitiveLinkedHashMap(final int expectedSize) {
		this(expectedSize, null);
	}

	/**
	 * Creates a map with the default expected size and the given locale.
	 *
	 * @param locale locale used for key normalization; {@link Locale#getDefault()} is used when {@code null}
	 */
	public CaseInsensitiveLinkedHashMap(final Locale locale) {
		this(DEFAULT_EXPECTED_SIZE, locale);
	}

	/**
	 * Creates an empty map with the default expected size and {@link Locale#getDefault()}.
	 */
	public CaseInsensitiveLinkedHashMap() {
		this((Locale) null);
	}

	/**
	 * @see KeyNormalizingLinkedHashMap#normalizeKey(String)
	 */
	@Override
	protected String normalizeKey(final String key) {
		return key.toLowerCase(locale);
	}

	/**
	 * Returns the locale used to normalize keys for case-insensitive lookup.
	 *
	 * @return the locale used for key normalization
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @see KeyNormalizingLinkedHashMap#clone()
	 */
	@Override
	public CaseInsensitiveLinkedHashMap<V> clone() throws CloneNotSupportedException { // NOSONAR
		return JavaObjects.cast(super.clone());
	}

	/**
	 * @see KeyNormalizingLinkedHashMap#equals(Object)
	 */
	@Override
	public boolean equals(final Object other) {
		return super.equals(other);
	}

	/**
	 * @see KeyNormalizingLinkedHashMap#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
