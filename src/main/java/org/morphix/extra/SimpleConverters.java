package org.morphix.extra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.morphix.function.SimpleConverter;

/**
 * Multiple converters encapsulation.
 *
 * @author Radu Sebastian LAZIN
 */
public class SimpleConverters implements Iterable<SimpleConverter<?, ?>> {

	private final List<SimpleConverter<?, ?>> converters;

	private SimpleConverters(final List<SimpleConverter<?, ?>> converters) {
		this.converters = Collections.unmodifiableList(converters);
	}

	@Override
	public Iterator<SimpleConverter<?, ?>> iterator() {
		return converters.iterator();
	}

	public boolean hasConverters() {
		return this != empty();
	}

	private int size() {
		return converters.size();
	}

	public static <S, D> SimpleConverters of(final SimpleConverter<S, D> convertMethod) {
		if (null == convertMethod) {
			return empty();
		}
		SimpleConverter<?, ?>[] convertMethods = new SimpleConverter<?, ?>[] { convertMethod };
		return of(convertMethods);
	}

	public static SimpleConverters of(final SimpleConverter<?, ?>... convertMethods) {
		if (null == convertMethods || 0 == convertMethods.length) {
			return empty();
		}
		return new SimpleConverters(List.of(convertMethods));
	}

	public static <S, D> SimpleConverters of(final SimpleConverter<S, D> convertMethod, final SimpleConverters simpleConverters) {
		SimpleConverters newSimpleConverters = SimpleConverters.of(convertMethod);
		int size = newSimpleConverters.size() + simpleConverters.size();
		if (0 == size) {
			return empty();
		}

		List<SimpleConverter<?, ?>> converters = new ArrayList<>(size);
		converters.addAll(newSimpleConverters.converters);
		converters.addAll(simpleConverters.converters);
		return new SimpleConverters(converters);
	}

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
		// there is no way in java to verify that two method references are the same
		// because they will both have different IDs so we only check the number of
		// simple converters.
		// TODO: see if there's a way to implement this to check the actual types
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
	 * Holds the empty instance, so it only gets instantiated once in
	 * multi-threaded environments.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class Empty {

		private static final SimpleConverters INSTANCE = new SimpleConverters(Collections.emptyList());

	}

}
