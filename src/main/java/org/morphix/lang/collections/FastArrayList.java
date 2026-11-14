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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.morphix.lang.JavaArrays;
import org.morphix.lang.JavaObjects;

/**
 * A resizable array-backed list optimized for bulk operations on arrays and other instances of this class.
 * <p>
 * Unlike {@link java.util.ArrayList}, this class supports adding elements directly from arrays without creating
 * intermediate collections or copying the source array.
 * <p>
 * This class is not thread-safe.
 *
 * @param <E> element type
 *
 * @author Radu Sebastian LAZIN
 */
public class FastArrayList<E> extends AbstractList<E> implements RandomAccess {

	/**
	 * Default values.
	 */
	public static final class Default {

		/**
		 * Default initial capacity.
		 */
		public static final int CAPACITY = 10;

		/**
		 * Hide constructor.
		 */
		private Default() {
			// empty
		}
	}

	/**
	 * Shared empty array for lists with default initial capacity.
	 */
	private static final Object[] DEFAULT_EMPTY = { };

	/**
	 * Maximum preferred array size.
	 */
	private static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

	/**
	 * Backing array.
	 */
	private Object[] elements;

	/**
	 * Number of elements in the list.
	 */
	private int size;

	/**
	 * Constructs an empty list with default initial capacity.
	 */
	public FastArrayList() {
		this.elements = DEFAULT_EMPTY;
	}

	/**
	 * Constructs an empty list with the specified initial capacity.
	 *
	 * @param initialCapacity initial capacity
	 */
	public FastArrayList(final int initialCapacity) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("Initial capacity cannot be negative: " + initialCapacity);
		}
		this.elements = 0 == initialCapacity ? JavaArrays.EMPTY : new Object[initialCapacity];
	}

	/**
	 * Constructs a list containing the elements of the specified collection.
	 *
	 * @param collection source collection
	 */
	public FastArrayList(final Collection<? extends E> collection) {
		Object[] array = collection.toArray();
		this.size = array.length;
		this.elements = 0 == size ? JavaArrays.EMPTY : Arrays.copyOf(array, size, Object[].class);
	}

	/**
	 * Constructs a list containing a copy of the specified array.
	 *
	 * @param array source array
	 */
	public FastArrayList(final E[] array) {
		this.size = array.length;
		this.elements = 0 == size ? JavaArrays.EMPTY : Arrays.copyOf(array, size, Object[].class);
	}

	/**
	 * Constructs a list with the specified backing array and size.
	 *
	 * @param elements backing array
	 * @param size number of elements
	 */
	private FastArrayList(final Object[] elements, final int size) {
		this.elements = elements;
		this.size = size;
	}

	/**
	 * Returns a list backed by the specified array without copying it.
	 * <p>
	 * Changes to the list are reflected in the array until the backing storage is replaced. The caller should relinquish
	 * ownership of the array.
	 *
	 * @param <T> element type
	 * @param array backing array
	 * @return list backed by the array
	 */
	public static <T> FastArrayList<T> wrap(final T[] array) {
		return new FastArrayList<>(array, array.length);
	}

	/**
	 * Returns the number of elements in the list.
	 *
	 * @return list size
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * Returns whether the list is empty.
	 *
	 * @return true if the list is empty
	 */
	@Override
	public boolean isEmpty() {
		return 0 == size;
	}

	/**
	 * Returns the current capacity of the list.
	 *
	 * @return backing array length
	 */
	public int capacity() {
		return elements.length;
	}

	/**
	 * @see AbstractList#get(int)
	 */
	@Override
	public E get(final int index) {
		Objects.checkIndex(index, size);
		return elementAt(index);
	}

	/**
	 * @see AbstractList#set(int, Object)
	 */
	@Override
	public E set(final int index, final E element) {
		Objects.checkIndex(index, size);

		E previous = elementAt(index);
		ensureCompatibleStorage(element);
		elements[index] = element;

		return previous;
	}

	/**
	 * @see AbstractList#add(Object)
	 */
	@Override
	public boolean add(final E element) {
		ensureCapacityFor(1);
		ensureCompatibleStorage(element);

		elements[size++] = element;
		++modCount;

		return true;
	}

	/**
	 * @see AbstractList#add(int, Object)
	 */
	@Override
	public void add(final int index, final E element) {
		checkPositionIndex(index);
		ensureCapacityFor(1);
		ensureCompatibleStorage(element);

		System.arraycopy(elements, index, elements, index + 1, size - index);
		elements[index] = element;

		++size;
		++modCount;
	}

	/**
	 * Adds all elements of the specified array to the end of the list.
	 *
	 * @param array source array
	 * @return true if the list changed
	 */
	public boolean addAll(final E[] array) {
		return addAll(size, array, 0, array.length);
	}

	/**
	 * Adds a range of elements from the specified array to the end of the list.
	 *
	 * @param array source array
	 * @param offset source offset
	 * @param length number of elements to add
	 * @return true if the list changed
	 */
	public boolean addAll(final E[] array, final int offset, final int length) {
		return addAll(size, array, offset, length);
	}

	/**
	 * Inserts all elements of the specified array at the given index.
	 *
	 * @param index insertion index
	 * @param array source array
	 * @return true if the list changed
	 */
	public boolean addAll(final int index, final E[] array) {
		return addAll(index, array, 0, array.length);
	}

	/**
	 * Inserts a range of elements from the specified array at the given index.
	 *
	 * @param index insertion index
	 * @param array source array
	 * @param offset source offset
	 * @param length number of elements to add
	 * @return true if the list changed
	 */
	public boolean addAll(final int index, final E[] array, final int offset, final int length) {
		checkPositionIndex(index);
		Objects.checkFromIndexSize(offset, length, array.length);

		return insertAll(index, array, offset, length);
	}

	/**
	 * @see AbstractList#addAll(Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends E> collection) {
		return addAll(size, collection);
	}

	/**
	 * @see AbstractList#addAll(int, Collection)
	 */
	@Override
	public boolean addAll(final int index, final Collection<? extends E> collection) {
		checkPositionIndex(index);

		if (collection instanceof FastArrayList<?> list) {
			return insertAll(index, list.elements, 0, list.size);
		}

		Object[] array = collection.toArray();
		return insertAll(index, array, 0, array.length);
	}

	/**
	 * Inserts elements from an array into the list.
	 *
	 * @param index insertion index
	 * @param array source array
	 * @param offset source offset
	 * @param length number of elements to insert
	 * @return true if the list changed
	 */
	private boolean insertAll(final int index, final Object[] array, final int offset, final int length) {
		if (0 == length) {
			return false;
		}

		ensureCapacityFor(length);

		// bulk insertion requires storage compatible with every element type
		ensureObjectStorage();

		Object[] source = array;
		int sourceOffset = offset;
		// shifting the tail overwrites a source range of the backing array that starts after the insertion index
		if (source == elements && offset > index) {
			source = Arrays.copyOfRange(array, offset, offset + length);
			sourceOffset = 0;
		}

		System.arraycopy(elements, index, elements, index + length, size - index);
		System.arraycopy(source, sourceOffset, elements, index, length);

		size += length;
		++modCount;

		return true;
	}

	/**
	 * @see AbstractList#remove(int)
	 */
	@Override
	public E remove(final int index) {
		Objects.checkIndex(index, size);

		E previous = elementAt(index);
		removeAt(index);

		return previous;
	}

	/**
	 * @see AbstractList#remove(Object)
	 */
	@Override
	public boolean remove(final Object obj) {
		int index = indexOf(obj);
		if (index < 0) {
			return false;
		}
		removeAt(index);
		return true;
	}

	/**
	 * Removes the element at the given index without validating the index.
	 *
	 * @param index element index
	 */
	private void removeAt(final int index) {
		int newSize = size - 1;

		if (index < newSize) {
			System.arraycopy(elements, index + 1, elements, index, newSize - index);
		}

		elements[newSize] = null;
		size = newSize;

		++modCount;
	}

	/**
	 * @see AbstractList#removeRange(int, int)
	 */
	@Override
	protected void removeRange(final int fromIndex, final int toIndex) {
		Objects.checkFromToIndex(fromIndex, toIndex, size);

		int count = toIndex - fromIndex;
		if (0 == count) {
			return;
		}

		System.arraycopy(elements, toIndex, elements, fromIndex, size - toIndex);

		int newSize = size - count;
		Arrays.fill(elements, newSize, size, null);

		size = newSize;
		++modCount;
	}

	/**
	 * @see AbstractList#clear()
	 */
	@Override
	public void clear() {
		if (0 == size) {
			return;
		}

		Arrays.fill(elements, 0, size, null);
		size = 0;

		++modCount;
	}

	/**
	 * @see AbstractList#indexOf(Object)
	 */
	@Override
	public int indexOf(final Object obj) {
		if (null == obj) {
			for (int i = 0; i < size; ++i) {
				if (null == elements[i]) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < size; ++i) {
				if (obj.equals(elements[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * @see AbstractList#lastIndexOf(Object)
	 */
	@Override
	public int lastIndexOf(final Object obj) {
		if (null == obj) {
			for (int i = size - 1; i >= 0; --i) {
				if (null == elements[i]) {
					return i;
				}
			}
		} else {
			for (int i = size - 1; i >= 0; --i) {
				if (obj.equals(elements[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * @see AbstractList#contains(Object)
	 */
	@Override
	public boolean contains(final Object obj) {
		return indexOf(obj) >= 0;
	}

	/**
	 * @see AbstractList#toArray()
	 */
	@Override
	public Object[] toArray() {
		return Arrays.copyOf(elements, size, Object[].class);
	}

	/**
	 * @see AbstractList#toArray(Object[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(final T[] array) {
		if (array.length < size) {
			return (T[]) Arrays.copyOf(elements, size, array.getClass());
		}

		System.arraycopy(elements, 0, array, 0, size);

		if (array.length > size) {
			array[size] = null;
		}

		return array;
	}

	/**
	 * @see Iterable#forEach(Consumer)
	 */
	@Override
	public void forEach(final Consumer<? super E> action) {
		Objects.requireNonNull(action);

		int expectedModCount = modCount;
		int length = size;

		// the backing array is not cached because set can replace it during iteration
		for (int i = 0; i < length && expectedModCount == modCount; ++i) {
			action.accept(elementAt(i));
		}

		checkForConcurrentModification(expectedModCount);
	}

	/**
	 * @see java.util.List#replaceAll(UnaryOperator)
	 */
	@Override
	public void replaceAll(final UnaryOperator<E> operator) {
		Objects.requireNonNull(operator);

		int expectedModCount = modCount;
		int length = size;

		for (int i = 0; i < length; ++i) {
			E value = operator.apply(elementAt(i));

			checkForConcurrentModification(expectedModCount);
			ensureCompatibleStorage(value);

			elements[i] = value;
		}

		checkForConcurrentModification(expectedModCount);
		++modCount;
	}

	/**
	 * @see java.util.List#sort(Comparator)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void sort(final Comparator<? super E> comparator) {
		int expectedModCount = modCount;

		Arrays.sort((E[]) elements, 0, size, comparator);

		checkForConcurrentModification(expectedModCount);
		++modCount;
	}

	/**
	 * @see Collection#removeIf(Predicate)
	 */
	@Override
	public boolean removeIf(final Predicate<? super E> predicate) {
		Objects.requireNonNull(predicate);

		int expectedModCount = modCount;
		int length = size;
		int first = 0;

		// skip the initial run of elements that are retained.
		while (first < length && expectedModCount == modCount
				&& !predicate.test(elementAt(first))) {
			++first;
		}

		checkForConcurrentModification(expectedModCount);

		if (first == length) {
			return false;
		}

		// record matching elements before modifying the list
		// this leaves the list unchanged if the predicate throws
		int remaining = length - first;
		long[] removed = new long[(int) ((remaining + 63L) >>> 6)];

		removed[0] = 1L;

		for (int i = first + 1; i < length && expectedModCount == modCount; ++i) {
			if (predicate.test(elementAt(i))) {
				int bit = i - first;
				removed[bit >>> 6] |= 1L << (bit & 63);
			}
		}

		checkForConcurrentModification(expectedModCount);

		int write = first;

		for (int read = first + 1; read < length; ++read) {
			int bit = read - first;

			if (0 == (removed[bit >>> 6] & (1L << (bit & 63)))) {
				elements[write++] = elements[read];
			}
		}

		Arrays.fill(elements, write, length, null);
		size = write;

		++modCount;

		return true;
	}

	/**
	 * @see Collection#removeAll(Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> collection) {
		Objects.requireNonNull(collection);
		return removeIf(collection::contains);
	}

	/**
	 * @see Collection#retainAll(Collection)
	 */
	@Override
	public boolean retainAll(final Collection<?> collection) {
		Objects.requireNonNull(collection);
		return removeIf(element -> !collection.contains(element));
	}

	/**
	 * Ensures that the backing array can hold at least the specified number of elements.
	 *
	 * @param minimumCapacity minimum required capacity
	 */
	public void ensureCapacity(final int minimumCapacity) {
		if (minimumCapacity > elements.length) {
			if (elements != DEFAULT_EMPTY || minimumCapacity > Default.CAPACITY) {
				grow(minimumCapacity);
			}
		}
	}

	/**
	 * Trims the backing array to the current list size.
	 */
	public void trimToSize() {
		if (size < elements.length) {
			elements = 0 == size ? JavaArrays.EMPTY : Arrays.copyOf(elements, size, Object[].class);
		}
	}

	/**
	 * Ensures that the backing array can hold the specified number of additional elements.
	 *
	 * @param count number of additional elements
	 */
	private void ensureCapacityFor(final int count) {
		int minimumCapacity = size + count;
		if (minimumCapacity < 0) {
			throw new OutOfMemoryError("Required array length exceeds maximum array size");
		}
		if (minimumCapacity > elements.length) {
			grow(minimumCapacity);
		}
	}

	/**
	 * Grows the backing array to accommodate the required capacity.
	 *
	 * @param minimumCapacity minimum required capacity
	 */
	private void grow(final int minimumCapacity) {
		int oldCapacity = elements.length;
		int newCapacity;
		if (elements == DEFAULT_EMPTY) {
			newCapacity = Math.max(Default.CAPACITY, minimumCapacity);
		} else {
			long preferredCapacity = (long) oldCapacity + (oldCapacity >> 1);
			if (preferredCapacity <= SOFT_MAX_ARRAY_LENGTH) {
				newCapacity = Math.max(minimumCapacity, (int) preferredCapacity);
			} else {
				newCapacity = Math.max(minimumCapacity, SOFT_MAX_ARRAY_LENGTH);
			}
		}
		elements = Arrays.copyOf(elements, newCapacity, Object[].class);
	}

	/**
	 * Ensures that the backing array can store the specified element.
	 * <p>
	 * This is only necessary when the list was constructed by wrapping an array whose runtime component type is more
	 * restrictive than Object.
	 *
	 * @param element element to store
	 */
	private void ensureCompatibleStorage(final E element) {
		if (null != element && !elements.getClass().getComponentType().isInstance(element)) {
			ensureObjectStorage();
		}
	}

	/**
	 * Ensures that the backing array is an Object array.
	 */
	private void ensureObjectStorage() {
		if (Object[].class != elements.getClass()) {
			elements = Arrays.copyOf(elements, elements.length, Object[].class);
		}
	}

	/**
	 * Returns the element at the given index without validating the index.
	 *
	 * @param index element index
	 * @return element
	 */
	private E elementAt(final int index) {
		return JavaObjects.cast(elements[index]);
	}

	/**
	 * Validates an insertion position.
	 *
	 * @param index insertion index
	 */
	private void checkPositionIndex(final int index) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
	}

	/**
	 * Checks whether the list has been structurally modified.
	 *
	 * @param expectedModCount expected modification count
	 */
	private void checkForConcurrentModification(final int expectedModCount) {
		if (expectedModCount != modCount) {
			throw new ConcurrentModificationException();
		}
	}
}
