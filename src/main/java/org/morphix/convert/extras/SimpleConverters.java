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
package org.morphix.convert.extras;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.morphix.convert.function.SimpleConverter;

/**
 * Multiple simple converters encapsulation.
 *
 * @author Radu Sebastian LAZIN
 */
public class SimpleConverters implements Iterable<SimpleConverter<?, ?>> {

	/**
	 * List of simple converters.
	 */
	private final List<SimpleConverter<?, ?>> converters;

	/**
	 * Private constructor.
	 *
	 * @param converters list of simple converters
	 */
	private SimpleConverters(final List<SimpleConverter<?, ?>> converters) {
		this.converters = Collections.unmodifiableList(converters);
	}

	/**
	 * Returns an iterator for the simple converters.
	 *
	 * @return an iterator for the simple converters
	 */
	@Override
	public Iterator<SimpleConverter<?, ?>> iterator() {
		return converters.iterator();
	}

	/**
	 * Returns true if this object has any converters, false otherwise.
	 *
	 * @return true if this object has any converters, false otherwise
	 */
	public boolean hasConverters() {
		return this != empty();
	}

	/**
	 * Returns the number of converters.
	 *
	 * @return the number of converters
	 */
	private int size() {
		return converters.size();
	}

	/**
	 * Builds a simple converters instance from the given convert method.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 *
	 * @param convertMethod simple converter method
	 * @return a new simple converters instance
	 */
	public static <S, D> SimpleConverters of(final SimpleConverter<S, D> convertMethod) {
		if (null == convertMethod) {
			return empty();
		}
		return of(List.of(convertMethod));
	}

	/**
	 * Builds a simple converters instance from the given convert methods.
	 *
	 * @param convertMethods array of convert methods
	 * @return a new simple converters instance
	 */
	public static SimpleConverters of(final SimpleConverter<?, ?>... convertMethods) {
		if (null == convertMethods || 0 == convertMethods.length) {
			return empty();
		}
		return of(List.of(convertMethods));
	}

	/**
	 * Builds a simple converters instance from the given convert methods.
	 *
	 * @param convertMethods array of convert methods
	 * @return a new simple converters instance
	 */
	public static SimpleConverters of(final List<SimpleConverter<?, ?>> convertMethods) {
		if (null == convertMethods || convertMethods.isEmpty()) {
			return empty();
		}
		return new SimpleConverters(convertMethods);
	}

	/**
	 * Builds a simple converters instance by adding the given converter to all the converters from the given simple
	 * converters.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 *
	 * @param convertMethod simple converter
	 * @param simpleConverters another simple converters instance.
	 * @return a new simple converters object
	 */
	public static <S, D> SimpleConverters of(final SimpleConverter<S, D> convertMethod, final SimpleConverters simpleConverters) {
		SimpleConverters newSimpleConverters = SimpleConverters.of(convertMethod);
		int size = newSimpleConverters.size() + simpleConverters.size();
		if (0 == size) {
			return empty();
		}

		List<SimpleConverter<?, ?>> converters = new ArrayList<>(size);
		converters.addAll(newSimpleConverters.converters);
		converters.addAll(simpleConverters.converters);
		return of(converters);
	}

	/**
	 * Returns an empty {@link SimpleConverters} instance.
	 *
	 * @return an empty simple converters instance
	 */
	public static SimpleConverters empty() {
		return Empty.INSTANCE;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SimpleConverters that = (SimpleConverters) obj;
		// TODO: there is no way in java to verify that two method references are the same
		// because they will both have different IDs so we only check the number of
		// simple converters, see if there's a way to implement this to check the actual types
		return this.converters.size() == that.converters.size();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.converters);
	}

	/**
	 * Holds the empty instance, so it only gets instantiated once in multi-threaded environments.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class Empty {

		/**
		 * Empty {@link SimpleConverters} instance.
		 */
		private static final SimpleConverters INSTANCE = new SimpleConverters(Collections.emptyList());

	}

}
