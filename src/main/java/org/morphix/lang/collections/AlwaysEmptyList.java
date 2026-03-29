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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.morphix.lang.JavaObjects;

/**
 * A list that is always empty no matter what operations are called on it. This class is useful because
 * {@link Collections#emptyList()} returns an immutable collection which throws exceptions on most list operations and
 * this one doesn't. It is impervious to change since it doesn't have any state.
 * <p>
 * WARNING: This class intentionally violates the standard List mutation and index contracts. All mutating operations
 * are no-ops and will not throw exceptions. This type is intended as a null-object or defensive list for integration
 * scenarios.
 * <p>
 * The class is intentionally null tolerant. All methods handle null parameters gracefully.
 *
 * @param <E> element type
 *
 * @author Radu Sebastian LAZIN
 */
public final class AlwaysEmptyList<E> implements List<E> {

	/**
	 * Singleton instance of an always empty list.
	 */
	private static final AlwaysEmptyList<Object> ALWAYS_EMPTY_LIST = new AlwaysEmptyList<>();

	/**
	 * Private constructor, use {@link #of()} to obtain an instance.
	 */
	private AlwaysEmptyList() {
		// empty
	}

	/**
	 * Returns a singleton instance of an always empty list.
	 *
	 * @param <T> element type
	 * @return always empty list
	 */
	public static <T> List<T> of() {
		return JavaObjects.cast(ALWAYS_EMPTY_LIST);
	}

	/**
	 * @see List#size()
	 */
	@Override
	public int size() {
		return 0;
	}

	/**
	 * @see List#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return true;
	}

	/**
	 * @see List#contains(Object)
	 */
	@Override
	public boolean contains(final Object o) {
		return false;
	}

	/**
	 * @see List#toArray()
	 */
	@Override
	public Object[] toArray() {
		return new Object[] { };
	}

	/**
	 * @see List#toArray(Object[])
	 */
	@Override
	public <T> T[] toArray(final T[] a) {
		if (null != a && a.length > 0) {
			a[0] = null;
		}
		return a;
	}

	/**
	 * @see List#add(Object)
	 */
	@Override
	public boolean add(final E e) {
		return false;
	}

	/**
	 * @see List#remove(Object)
	 */
	@Override
	public boolean remove(final Object o) {
		return false;
	}

	/**
	 * @see List#removeFirst()
	 */
	@Override
	public E removeFirst() {
		return null;
	}

	/**
	 * @see List#removeLast()
	 */
	@Override
	public E removeLast() {
		return null;
	}

	/**
	 * @see List#containsAll(Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
		return null == c || c.isEmpty();
	}

	/**
	 * @see List#addAll(Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends E> c) {
		return false;
	}

	/**
	 * @see List#addAll(int, Collection)
	 */
	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		return false;
	}

	/**
	 * @see List#removeAll(Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
		return false;
	}

	/**
	 * @see List#retainAll(Collection)
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {
		return false;
	}

	/**
	 * @see List#clear()
	 */
	@Override
	public void clear() {
		// nothing to clear
	}

	/**
	 * @see List#get(int)
	 */
	@Override
	public E get(final int index) {
		return null;
	}

	/**
	 * @see List#getFirst()
	 */
	@Override
	public E getFirst() {
		return null;
	}

	/**
	 * @see List#getLast()
	 */
	@Override
	public E getLast() {
		return null;
	}

	/**
	 * @see List#set(int, Object)
	 */
	@Override
	public E set(final int index, final E element) {
		return null;
	}

	/**
	 * @see List#add(int, Object)
	 */
	@Override
	public void add(final int index, final E element) {
		// add has no effect
	}

	/**
	 * @see List#remove(int)
	 */
	@Override
	public E remove(final int index) {
		return null;
	}

	/**
	 * @see List#indexOf(Object)
	 */
	@Override
	public int indexOf(final Object o) {
		return -1;
	}

	/**
	 * @see List#lastIndexOf(Object)
	 */
	@Override
	public int lastIndexOf(final Object o) {
		return -1;
	}

	/**
	 * @see List#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return EmptyIterator.of();
	}

	/**
	 * @see List#listIterator()
	 */
	@Override
	public ListIterator<E> listIterator() {
		return EmptyListIterator.of();
	}

	/**
	 * @see List#listIterator(int)
	 */
	@Override
	public ListIterator<E> listIterator(final int index) {
		return EmptyListIterator.of();
	}

	/**
	 * @see List#subList(int, int)
	 */
	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		return this;
	}

	/**
	 * Empty iterator implementation.
	 *
	 * @param <E> element type
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class EmptyIterator<E> implements Iterator<E> {

		/**
		 * Singleton instance of an empty iterator.
		 */
		private static final EmptyIterator<Object> EMPTY_ITERATOR = new EmptyIterator<>();

		/**
		 * Private constructor, use {@link #of()} to obtain an instance.
		 */
		private EmptyIterator() {
			// empty
		}

		/**
		 * Returns a singleton instance of an empty iterator.
		 *
		 * @param <T> element type
		 * @return empty iterator
		 */
		public static <T> EmptyIterator<T> of() {
			return JavaObjects.cast(EMPTY_ITERATOR);
		}

		/**
		 * @see Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return false;
		}

		/**
		 * @see Iterator#next()
		 */
		@Override
		public E next() {
			throw new NoSuchElementException();
		}

		/**
		 * @see Iterator#remove()
		 */
		@Override
		public void remove() {
			// nothing to remove
		}

		/**
		 * @see Iterator#forEachRemaining(Consumer)
		 */
		@Override
		public void forEachRemaining(final Consumer<? super E> action) {
			// intentionally no-op, even if action is null
		}
	}

	/**
	 * Empty list iterator implementation.
	 *
	 * @param <E> element type
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class EmptyListIterator<E> extends EmptyIterator<E> implements ListIterator<E> {

		/**
		 * Singleton instance of an empty list iterator.
		 */
		private static final EmptyListIterator<Object> EMPTY_LIST_ITERATOR = new EmptyListIterator<>();

		/**
		 * Private constructor, use {@link #of()} to obtain an instance.
		 */
		private EmptyListIterator() {
			// empty
		}

		/**
		 * Returns a singleton instance of an empty list iterator.
		 *
		 * @param <T> element type
		 * @return empty list iterator
		 */
		public static <T> EmptyListIterator<T> of() {
			return JavaObjects.cast(EMPTY_LIST_ITERATOR);
		}

		/**
		 * @see ListIterator#hasPrevious()
		 */
		@Override
		public boolean hasPrevious() {
			return false;
		}

		/**
		 * @see ListIterator#previous()
		 */
		@Override
		public E previous() {
			throw new NoSuchElementException();
		}

		/**
		 * @see ListIterator#nextIndex()
		 */
		@Override
		public int nextIndex() {
			return 0;
		}

		/**
		 * @see ListIterator#previousIndex()
		 */
		@Override
		public int previousIndex() {
			return -1;
		}

		/**
		 * @see ListIterator#set(Object)
		 */
		@Override
		public void set(final E e) {
			// nothing to set
		}

		/**
		 * @see ListIterator#add(Object)
		 */
		@Override
		public void add(final E e) {
			// nothing to add
		}
	}
}
