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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utility methods for lists which are missing from JDK.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Lists {

	/**
	 * Returns the list given as parameter or an empty list if the parameter is {@code null}.
	 * <p>
	 * Usable for iterating on lists that can be {@code null} without null checks.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * List&lt;String&gt; list;
	 * for (String element : safe(list)) {
	 * 	// ...
	 * }
	 * </pre>
	 * <p>
	 * will not throw a {@link NullPointerException} even if the list is {@code null}.
	 *
	 * @param <T> element type
	 *
	 * @param list a list
	 * @return the list given as parameter or an empty list if the parameter is null
	 */
	static <T> List<T> safe(final List<T> list) {
		return null == list ? Collections.emptyList() : list;
	}

	/**
	 * Transforms an array to an immutable list. This method returns an immutable list and does the necessary null checks
	 * which {@link List#of(Object...)} does not do. When calling:
	 *
	 * <pre>
	 * var list = List.of(null);
	 * </pre>
	 *
	 * will throw an exception whereas calling:
	 *
	 * <pre>
	 * var list = Lists.asList(null);
	 * </pre>
	 *
	 * will return an empty immutable list.
	 *
	 * @param <T> element type
	 *
	 * @param a array
	 * @return list with the given arrays elements
	 */
	@SafeVarargs
	static <T> List<T> asList(final T... a) {
		return null != a ? List.of(a) : Collections.emptyList();
	}

	/**
	 * Returns the first element from the given collection, or {@code null} if the list is null or empty.
	 * <p>
	 * Will not throw a {@link NullPointerException} even if the collection is {@code null}.
	 *
	 * @param <T> element type
	 *
	 * @param list a list
	 * @return The first element from the given list, if the list is not null or empty, or null otherwise.
	 */
	static <T> T first(final List<T> list) {
		return first(list, null);
	}

	/**
	 * Returns the first element from the given list, or defaultValue if the list is null or empty or the value returned is
	 * null so that the value returned can only be {@code null} if the default value given is {@code null}.
	 * <p>
	 * Will not throw a {@link NullPointerException} even if the list is {@code null}.
	 *
	 * @param <T> element type
	 *
	 * @param list a list
	 * @param defaultValue default value list doesn't have a first value
	 * @return The first element from the given list, if the list is not null or empty, defaultValue otherwise.
	 */
	static <T> T first(final List<T> list, final T defaultValue) {
		return isNotEmpty(list) ? list.getFirst() : defaultValue;
	}

	/**
	 * Returns the last element from the given list, or {@code null} if the list is null or empty.
	 * <p>
	 * Will not throw a {@link NullPointerException} even if the list is {@code null}.
	 *
	 * @param <T> element type
	 *
	 * @param list a list
	 * @return The last element from the given list, if the list is not null or empty, or null otherwise.
	 */
	static <T> T last(final List<T> list) {
		return isNotEmpty(list) ? list.getLast() : null;
	}

	/**
	 * Null-safe check if the specified list is empty.
	 * <p>
	 * Null returns true.
	 *
	 * @param <T> element type
	 *
	 * @param list the list to check, may be null
	 * @return true if empty or null
	 */
	static <T> boolean isEmpty(final List<T> list) {
		return null == list || list.isEmpty();
	}

	/**
	 * Null-safe check if the specified list is not empty.
	 * <p>
	 * Null returns false.
	 *
	 * @param <T> element type
	 *
	 * @param coll the list to check, may be null
	 * @return true if non-null and non-empty
	 */
	static <T> boolean isNotEmpty(final List<T> coll) {
		return !isEmpty(coll);
	}

	/**
	 * Merges two sorted lists returning a new mutable sorted list.
	 * <ul>
	 * <li>Both input lists must be sorted in ascending order.</li>
	 * <li>The returned list will be sorted in ascending order.</li>
	 * <li>The returned list is a new list.</li>
	 * <li>If one of the lists is empty, a copy of the other list is returned.</li>
	 * <li>if both are empty, an empty list is returned.</li>
	 * </ul>
	 * The algorithm has O(n + m) complexity (assuming that advancing in the iterators is O(1))
	 *
	 * @param <T> element type
	 *
	 * @param sortedList1 first sorted list
	 * @param sortedList2 second sorted list
	 * @return merged sorted list
	 */
	static <T extends Comparable<? super T>> List<T> merge(final List<T> sortedList1, final List<T> sortedList2) {
		if (isEmpty(sortedList1)) {
			return new ArrayList<>(safe(sortedList2));
		}
		if (isEmpty(sortedList2)) {
			return new ArrayList<>(safe(sortedList1));
		}
		int resultSize = sortedList1.size() + sortedList2.size();
		var result = new ArrayList<T>(resultSize);

		Iterator<T> list1Iterator = sortedList1.iterator();
		Iterator<T> list2Iterator = sortedList2.iterator();

		T item1 = list1Iterator.next();
		T item2 = list2Iterator.next();

		while (list1Iterator.hasNext() && list2Iterator.hasNext()) {
			if (item1.compareTo(item2) < 0) {
				result.add(item1);
				item1 = list1Iterator.next();
			} else {
				result.add(item2);
				item2 = list2Iterator.next();
			}
		}
		if (item1.compareTo(item2) < 0) {
			result.add(item1);
			result.add(item2);
		} else {
			result.add(item2);
			result.add(item1);
		}
		if (list1Iterator.hasNext()) {
			list1Iterator.forEachRemaining(result::add);
		} else {
			list2Iterator.forEachRemaining(result::add);
		}
		return result;
	}

	/**
	 * Partitions the given list into sublists of the specified size. The last sublist may be smaller if the total number of
	 * elements is not divisible by {@code size}. If the input list is {@code null}, empty, or {@code size <= 0}, an empty
	 * list is returned.
	 * <p>
	 * The returned partitions are independent immutable lists and are not backed by the original list. Changes to the input
	 * list after calling this method do not affect the returned partitions.
	 * <p>
	 * Time complexity is {@code O(n)} where {@code n} is the size of the input list.
	 *
	 * @param <T> element type
	 *
	 * @param list the list to partition
	 * @param size the maximum size of each sublist
	 * @return a list of sublists, where each sublist has at most 'size' elements
	 */
	static <T> List<List<T>> partition(final List<T> list, final int size) {
		if (isEmpty(list) || size <= 0) {
			return Collections.emptyList();
		}
		int partitions = (list.size() + size - 1) / size;
		List<List<T>> result = new ArrayList<>(partitions);
		int n = list.size();
		for (int i = 0; i < n; i += size) {
			int end = Math.min(i + size, n);
			result.add(List.copyOf(list.subList(i, end)));
		}
		return result;
	}
}
