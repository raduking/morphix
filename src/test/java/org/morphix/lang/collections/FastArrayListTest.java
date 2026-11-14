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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;

/**
 * Test class for {@link FastArrayList}.
 *
 * @author Radu Sebastian LAZIN
 */
class FastArrayListTest {

	private static FastArrayList<String> listOf(final String... elements) {
		FastArrayList<String> list = new FastArrayList<>();
		for (String element : elements) {
			list.add(element);
		}
		return list;
	}

	/**
	 * Returns a list holding {@code [1, 2]} backed by an {@link Integer} array of length 3, so it has spare capacity and a
	 * storage component type more restrictive than {@link Object}.
	 */
	private static FastArrayList<Object> typedStorageWithSpareCapacity() {
		FastArrayList<Object> list = FastArrayList.<Object>wrap(new Integer[] { 1, 2, 3 });
		list.remove(2);
		return list;
	}

	@Nested
	class ConstructorTests {

		@Test
		void shouldCreateEmptyListWithDefaultConstructor() {
			FastArrayList<String> list = new FastArrayList<>();

			assertThat(list, empty());
			assertThat(list.size(), equalTo(0));
			assertThat(list.capacity(), equalTo(0));
		}

		@Test
		void shouldCreateListWithGivenInitialCapacity() {
			FastArrayList<String> list = new FastArrayList<>(5);

			assertThat(list, empty());
			assertThat(list.capacity(), equalTo(5));
		}

		@Test
		void shouldCreateListWithZeroInitialCapacity() {
			FastArrayList<String> list = new FastArrayList<>(0);

			assertThat(list, empty());
			assertThat(list.capacity(), equalTo(0));
		}

		@Test
		void shouldThrowExceptionForNegativeInitialCapacity() {
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new FastArrayList<>(-1));

			assertThat(e.getMessage(), equalTo("Initial capacity cannot be negative: -1"));
		}

		@Test
		void shouldCreateListFromCollection() {
			FastArrayList<String> list = new FastArrayList<>(List.of("a", "b", "c"));

			assertThat(list, contains("a", "b", "c"));
			assertThat(list.isEmpty(), equalTo(false));
			assertThat(list.size(), equalTo(3));
			assertThat(list.capacity(), equalTo(3));
		}

		@Test
		void shouldCreateEmptyListFromEmptyCollection() {
			FastArrayList<String> list = new FastArrayList<>(List.of());

			assertThat(list, empty());
			assertThat(list.capacity(), equalTo(0));
		}

		@Test
		void shouldThrowExceptionForNullCollection() {
			assertThrows(NullPointerException.class, () -> new FastArrayList<>((List<String>) null));
		}

		@Test
		void shouldCreateListFromArrayCopy() {
			String[] array = { "a", "b" };
			FastArrayList<String> list = new FastArrayList<>(array);
			array[0] = "z";

			assertThat(list, contains("a", "b"));
			assertThat(list.capacity(), equalTo(2));
		}

		@Test
		void shouldCreateEmptyListFromEmptyArray() {
			FastArrayList<String> list = new FastArrayList<>(new String[0]);

			assertThat(list, empty());
			assertThat(list.capacity(), equalTo(0));
		}

		@Test
		void shouldThrowExceptionForNullArray() {
			assertThrows(NullPointerException.class, () -> new FastArrayList<>((String[]) null));
		}

		@Test
		void shouldAcceptAnyElementAfterCopyingTypedArray() {
			FastArrayList<Object> list = new FastArrayList<>(new Integer[] { 1 });
			list.set(0, "x");
			list.add("y");

			assertThat(list, contains("x", "y"));
		}
	}

	@Nested
	class WrapTests {

		@Test
		void shouldWrapArrayWithoutCopying() {
			String[] array = { "a", "b" };
			FastArrayList<String> list = FastArrayList.wrap(array);

			assertThat(list, contains("a", "b"));
			assertThat(list.capacity(), equalTo(2));

			list.set(0, "z");
			assertThat(array[0], equalTo("z"));
		}

		@Test
		void shouldStopReflectingChangesAfterBackingStorageIsReplaced() {
			String[] array = { "a", "b" };
			FastArrayList<String> list = FastArrayList.wrap(array);
			list.add("c");
			list.set(0, "z");

			assertThat(list, contains("z", "b", "c"));
			assertThat(array, arrayContaining("a", "b"));
		}

		@Test
		void shouldWrapEmptyArray() {
			FastArrayList<String> list = FastArrayList.wrap(new String[0]);
			list.add("a");

			assertThat(list, contains("a"));
		}

		@Test
		void shouldThrowExceptionForNullArray() {
			assertThrows(NullPointerException.class, () -> FastArrayList.wrap(null));
		}

		@Test
		void shouldKeepWritingIntoTypedStorageForCompatibleElements() {
			Integer[] array = { 1, 2, 3 };
			FastArrayList<Object> list = FastArrayList.<Object>wrap(array);
			list.remove(2);
			list.add(4);

			assertThat(array, arrayContaining(1, 2, 4));
		}

		@Test
		void shouldClearFreedSlotsInWrappedArray() {
			String[] array = { "a", "b", "c" };
			FastArrayList<String> list = FastArrayList.wrap(array);
			list.remove(0);

			assertThat(array, arrayContaining("b", "c", null));
		}
	}

	@Nested
	class TypedStorageTests {

		@Test
		void shouldAddIncompatibleElement() {
			FastArrayList<Object> list = typedStorageWithSpareCapacity();
			list.add("x");

			assertThat(list, contains(1, 2, "x"));
		}

		@Test
		void shouldAddIncompatibleElementAtIndex() {
			FastArrayList<Object> list = typedStorageWithSpareCapacity();
			list.add(0, "x");

			assertThat(list, contains("x", 1, 2));
		}

		@Test
		void shouldSetIncompatibleElement() {
			FastArrayList<Object> list = typedStorageWithSpareCapacity();
			Object previous = list.set(1, "x");

			assertThat(previous, equalTo(2));
			assertThat(list, contains(1, "x"));
		}

		@Test
		void shouldSetNullElement() {
			FastArrayList<Object> list = typedStorageWithSpareCapacity();
			list.set(0, null);

			assertThat(list, contains(null, 2));
		}

		@Test
		void shouldAddAllIncompatibleElements() {
			FastArrayList<Object> list = typedStorageWithSpareCapacity();
			list.addAll(new Object[] { "x" });

			assertThat(list, contains(1, 2, "x"));
		}

		@Test
		void shouldReplaceAllWithIncompatibleElements() {
			FastArrayList<Object> list = typedStorageWithSpareCapacity();
			list.replaceAll(String::valueOf);

			assertThat(list, contains("1", "2"));
		}
	}

	@Nested
	class AccessTests {

		@Test
		void shouldGetElementAtIndex() {
			FastArrayList<String> list = listOf("a", "b");

			assertThat(list.get(0), equalTo("a"));
			assertThat(list.get(1), equalTo("b"));
		}

		@Test
		void shouldThrowExceptionOnGetWithInvalidIndex() {
			FastArrayList<String> list = listOf("a");

			assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
			assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
		}

		@Test
		void shouldThrowExceptionOnGetWithIndexBeyondSizeButWithinCapacity() {
			FastArrayList<String> list = new FastArrayList<>(10);
			list.add("a");

			assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
		}

		@Test
		void shouldSetElementAndReturnPrevious() {
			FastArrayList<String> list = listOf("a", "b");

			assertThat(list.set(1, "z"), equalTo("b"));
			assertThat(list, contains("a", "z"));
		}

		@Test
		void shouldThrowExceptionOnSetWithInvalidIndex() {
			FastArrayList<String> list = listOf("a");

			assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, "z"));
			assertThrows(IndexOutOfBoundsException.class, () -> list.set(1, "z"));
		}

		@Test
		void shouldFindIndexOfElements() {
			FastArrayList<String> list = listOf("a", null, "b", "a", null);

			assertThat(list.indexOf("a"), equalTo(0));
			assertThat(list.lastIndexOf("a"), equalTo(3));
			assertThat(list.indexOf(null), equalTo(1));
			assertThat(list.lastIndexOf(null), equalTo(4));
		}

		@Test
		void shouldReturnMinusOneForMissingElements() {
			FastArrayList<String> list = listOf("a", "b");

			assertThat(list.indexOf("x"), equalTo(-1));
			assertThat(list.lastIndexOf("x"), equalTo(-1));
			assertThat(list.indexOf(null), equalTo(-1));
			assertThat(list.lastIndexOf(null), equalTo(-1));
		}

		@Test
		void shouldNotFindElementsBeyondSize() {
			FastArrayList<String> list = new FastArrayList<>(10);

			assertThat(list.indexOf(null), equalTo(-1));
			assertThat(list.lastIndexOf(null), equalTo(-1));
		}

		@Test
		void shouldCheckIfElementIsContained() {
			FastArrayList<String> list = listOf("a", null);

			assertThat(list.contains("a"), equalTo(true));
			assertThat(list.contains(null), equalTo(true));
			assertThat(list.contains("x"), equalTo(false));
		}
	}

	@Nested
	class AddTests {

		@Test
		void shouldAddElements() {
			FastArrayList<String> list = new FastArrayList<>();

			assertThat(list.add("a"), equalTo(true));
			assertThat(list.add(null), equalTo(true));
			assertThat(list, contains("a", null));
		}

		@Test
		void shouldAddElementAtIndex() {
			FastArrayList<String> list = listOf("b", "d");
			list.add(0, "a");
			list.add(2, "c");
			list.add(4, "e");

			assertThat(list, contains("a", "b", "c", "d", "e"));
		}

		@Test
		void shouldAddElementAtIndexWhenFull() {
			FastArrayList<String> list = new FastArrayList<>(2);
			list.add("a");
			list.add("c");
			list.add(1, "b");

			assertThat(list, contains("a", "b", "c"));
		}

		@Test
		void shouldThrowExceptionOnAddWithInvalidIndex() {
			FastArrayList<String> list = listOf("a", "b");

			IndexOutOfBoundsException e = assertThrows(IndexOutOfBoundsException.class, () -> list.add(3, "x"));
			assertThat(e.getMessage(), equalTo("Index: 3, Size: 2"));

			assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, "x"));
			assertThat(list, contains("a", "b"));
		}

		@Test
		void shouldThrowOutOfMemoryErrorWhenSizeWouldOverflow() {
			FastArrayList<String> list = new FastArrayList<>();
			Fields.IgnoreAccess.set(list, "size", Integer.MAX_VALUE);

			OutOfMemoryError e = assertThrows(OutOfMemoryError.class, () -> list.add("a"));
			assertThat(e.getMessage(), equalTo("Required array length exceeds maximum array size"));
		}
	}

	@Nested
	class AddAllArrayTests {

		@Test
		void shouldAddAllElementsOfArray() {
			FastArrayList<String> list = listOf("a");

			assertThat(list.addAll(new String[] { "b", "c" }), equalTo(true));
			assertThat(list, contains("a", "b", "c"));
		}

		@Test
		void shouldNotChangeListWhenAddingEmptyArray() {
			FastArrayList<String> list = listOf("a");

			assertThat(list.addAll(new String[0]), equalTo(false));
			assertThat(list, contains("a"));
		}

		@Test
		void shouldAddRangeOfArray() {
			FastArrayList<String> list = listOf("a");

			assertThat(list.addAll(new String[] { "x", "b", "c", "y" }, 1, 2), equalTo(true));
			assertThat(list, contains("a", "b", "c"));
		}

		@Test
		void shouldInsertAllElementsOfArrayAtIndex() {
			FastArrayList<String> list = listOf("a", "d");

			assertThat(list.addAll(1, new String[] { "b", "c" }), equalTo(true));
			assertThat(list, contains("a", "b", "c", "d"));
		}

		@Test
		void shouldInsertRangeOfArrayAtIndex() {
			FastArrayList<String> list = listOf("a", "d");

			assertThat(list.addAll(1, new String[] { "x", "b", "c", "y" }, 1, 2), equalTo(true));
			assertThat(list, contains("a", "b", "c", "d"));
		}

		@Test
		void shouldThrowExceptionForInvalidArrayRange() {
			FastArrayList<String> list = listOf("a");
			String[] array = { "b", "c" };

			assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(array, 1, 2));
			assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(array, -1, 1));
			assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(array, 0, -1));
			assertThat(list, contains("a"));
		}

		@Test
		void shouldThrowExceptionForInvalidInsertionIndex() {
			FastArrayList<String> list = listOf("a");

			assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(2, new String[] { "b" }));
			assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(-1, new String[] { "b" }));
		}

		@Test
		void shouldThrowExceptionForNullArray() {
			FastArrayList<String> list = listOf("a");

			assertThrows(NullPointerException.class, () -> list.addAll((String[]) null));
		}

		@Test
		void shouldGrowToExactSizeWhenAddingManyElementsToDefaultList() {
			FastArrayList<Integer> list = new FastArrayList<>();
			Integer[] array = new Integer[15];
			for (int i = 0; i < array.length; ++i) {
				array[i] = i;
			}
			list.addAll(array);

			assertThat(list.size(), equalTo(15));
			assertThat(list.capacity(), equalTo(15));
			assertThat(list.get(14), equalTo(14));
		}

		@Test
		void shouldInsertRangeOfOwnBackingArray() {
			Object[] array = { "a", "b", "c" };
			FastArrayList<Object> list = FastArrayList.wrap(array);
			list.remove(2);

			list.addAll(0, array, 1, 1);

			assertThat(list, contains("b", "a", "b"));
		}

		@Test
		void shouldThrowOutOfMemoryErrorWhenSizeWouldOverflow() {
			FastArrayList<String> list = new FastArrayList<>();
			Fields.IgnoreAccess.set(list, "size", Integer.MAX_VALUE - 1);

			assertThrows(OutOfMemoryError.class, () -> list.addAll(new String[] { "a", "b" }));
		}
	}

	@Nested
	class AddAllCollectionTests {

		@Test
		void shouldAddAllElementsOfCollection() {
			FastArrayList<String> list = listOf("a");

			assertThat(list.addAll(List.of("b", "c")), equalTo(true));
			assertThat(list, contains("a", "b", "c"));
		}

		@Test
		void shouldAddAllElementsOfAnotherFastArrayList() {
			FastArrayList<String> list = listOf("a");

			assertThat(list.addAll(listOf("b", "c")), equalTo(true));
			assertThat(list, contains("a", "b", "c"));
		}

		@Test
		void shouldAddOnlyTheElementsOfAnotherFastArrayListWithSpareCapacity() {
			FastArrayList<String> source = new FastArrayList<>(10);
			source.add("b");
			FastArrayList<String> list = listOf("a");
			list.addAll(source);

			assertThat(list, contains("a", "b"));
		}

		@Test
		void shouldAddAllElementsOfFastArrayListWithTypedStorage() {
			FastArrayList<Object> list = typedStorageWithSpareCapacity();
			list.addAll(FastArrayList.wrap(new String[] { "x" }));

			assertThat(list, contains(1, 2, "x"));
		}

		@Test
		void shouldInsertAllElementsOfCollectionAtIndex() {
			FastArrayList<String> list = listOf("a", "d");

			assertThat(list.addAll(1, List.of("b", "c")), equalTo(true));
			assertThat(list, contains("a", "b", "c", "d"));
		}

		@Test
		void shouldNotChangeListWhenAddingEmptyCollection() {
			FastArrayList<String> list = listOf("a");

			assertThat(list.addAll(List.of()), equalTo(false));
			assertThat(list.addAll(new FastArrayList<>()), equalTo(false));
			assertThat(list, contains("a"));
		}

		@Test
		void shouldThrowExceptionForInvalidIndex() {
			FastArrayList<String> list = listOf("a");

			assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(2, List.of("b")));
			assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(-1, List.of("b")));
		}

		@Test
		void shouldThrowExceptionForNullCollection() {
			FastArrayList<String> list = listOf("a");

			assertThrows(NullPointerException.class, () -> list.addAll((List<String>) null));
		}

		@Test
		void shouldAddItselfWhenGrowing() {
			FastArrayList<String> list = new FastArrayList<>(3);
			list.add("a");
			list.add("b");
			list.add("c");
			list.addAll(list);

			assertThat(list, contains("a", "b", "c", "a", "b", "c"));
		}

		@Test
		void shouldAddItselfWithoutGrowing() {
			FastArrayList<String> list = new FastArrayList<>(10);
			list.add("a");
			list.add("b");
			list.add("c");
			list.addAll(list);

			assertThat(list, contains("a", "b", "c", "a", "b", "c"));
		}

		@Test
		void shouldInsertItselfInTheMiddleWithoutGrowing() {
			FastArrayList<String> list = new FastArrayList<>(10);
			list.add("a");
			list.add("b");
			list.add("c");
			list.addAll(1, list);

			assertThat(list, contains("a", "a", "b", "c", "b", "c"));
		}
	}

	@Nested
	class RemoveTests {

		@Test
		void shouldRemoveElementAtIndex() {
			FastArrayList<String> list = listOf("a", "b", "c");

			assertThat(list.remove(1), equalTo("b"));
			assertThat(list, contains("a", "c"));
			assertThat(list.remove(1), equalTo("c"));
			assertThat(list, contains("a"));
		}

		@Test
		void shouldThrowExceptionOnRemoveWithInvalidIndex() {
			FastArrayList<String> list = listOf("a");

			assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
			assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));
		}

		@Test
		void shouldRemoveFirstOccurrenceOfElement() {
			FastArrayList<String> list = listOf("a", "b", "a");

			assertThat(list.remove("a"), equalTo(true));
			assertThat(list, contains("b", "a"));
		}

		@Test
		void shouldRemoveNullElement() {
			FastArrayList<String> list = listOf("a", null);

			assertThat(list.remove(null), equalTo(true));
			assertThat(list, contains("a"));
		}

		@Test
		void shouldNotRemoveMissingElement() {
			FastArrayList<String> list = listOf("a");

			assertThat(list.remove("x"), equalTo(false));
			assertThat(list, contains("a"));
		}

		@Test
		void shouldRemoveRangeThroughSubList() {
			FastArrayList<String> list = listOf("a", "b", "c", "d");
			list.subList(1, 3).clear();

			assertThat(list, contains("a", "d"));
		}

		@Test
		void shouldRemoveRangeAtTheEnd() {
			String[] array = { "a", "b", "c" };
			FastArrayList<String> list = FastArrayList.wrap(array);
			list.removeRange(1, 3);

			assertThat(list, contains("a"));
			assertThat(array, arrayContaining("a", null, null));
		}

		@Test
		void shouldNotChangeListForEmptyRange() {
			FastArrayList<String> list = listOf("a", "b");
			list.removeRange(1, 1);

			assertThat(list, contains("a", "b"));
		}

		@Test
		void shouldThrowExceptionForInvalidRange() {
			FastArrayList<String> list = listOf("a", "b");

			assertThrows(IndexOutOfBoundsException.class, () -> list.removeRange(2, 1));
			assertThrows(IndexOutOfBoundsException.class, () -> list.removeRange(0, 3));
			assertThrows(IndexOutOfBoundsException.class, () -> list.removeRange(-1, 1));
		}

		@Test
		void shouldClearListAndKeepCapacity() {
			String[] array = { "a", "b" };
			FastArrayList<String> list = FastArrayList.wrap(array);
			list.clear();

			assertThat(list, empty());
			assertThat(list.capacity(), equalTo(2));
			assertThat(array[0], nullValue());
			assertThat(array[1], nullValue());
		}

		@Test
		void shouldNotModifyEmptyListOnClear() {
			FastArrayList<String> list = new FastArrayList<>();
			Iterator<String> iterator = list.iterator();
			list.clear();

			assertThat(list, empty());
			assertThrows(NoSuchElementException.class, iterator::next);
		}
	}

	@Nested
	class RemoveIfTests {

		@Test
		void shouldReturnFalseWhenNothingIsRemoved() {
			FastArrayList<String> list = listOf("a", "b");

			assertThat(list.removeIf("x"::equals), equalTo(false));
			assertThat(list, contains("a", "b"));
		}

		@Test
		void shouldReturnFalseForEmptyList() {
			FastArrayList<String> list = new FastArrayList<>();

			assertThat(list.removeIf(e -> true), equalTo(false));
		}

		@Test
		void shouldRemoveMatchingElements() {
			FastArrayList<String> list = listOf("a", "x", "b", "x", "c");

			assertThat(list.removeIf("x"::equals), equalTo(true));
			assertThat(list, contains("a", "b", "c"));
		}

		@Test
		void shouldRemoveFirstAndLastElements() {
			FastArrayList<String> list = listOf("x", "a", "x");

			assertThat(list.removeIf("x"::equals), equalTo(true));
			assertThat(list, contains("a"));
		}

		@Test
		void shouldRemoveAllElements() {
			FastArrayList<String> list = listOf("a", "b");

			assertThat(list.removeIf(e -> true), equalTo(true));
			assertThat(list, empty());
		}

		@Test
		void shouldRemoveMatchingElementsAcrossManyBitmapWords() {
			FastArrayList<Integer> list = new FastArrayList<>();
			List<Integer> expected = new ArrayList<>();
			for (int i = 0; i < 200; ++i) {
				list.add(i);
				if (i % 3 != 0 || i < 5) {
					expected.add(i);
				}
			}

			assertThat(list.removeIf(e -> e >= 5 && e % 3 == 0), equalTo(true));
			assertThat(list, equalTo(expected));
		}

		@Test
		void shouldClearFreedSlots() {
			String[] array = { "x", "a", "x" };
			FastArrayList<String> list = FastArrayList.wrap(array);
			list.removeIf("x"::equals);

			assertThat(array, arrayContaining("a", null, null));
		}

		@Test
		void shouldLeaveListUnchangedWhenPredicateThrowsBeforeFirstMatch() {
			FastArrayList<String> list = listOf("a", "b");

			assertThrows(IllegalStateException.class, () -> list.removeIf(e -> {
				throw new IllegalStateException();
			}));
			assertThat(list, contains("a", "b"));
		}

		@Test
		void shouldLeaveListUnchangedWhenPredicateThrowsAfterFirstMatch() {
			FastArrayList<String> list = listOf("x", "a", "b");

			assertThrows(IllegalStateException.class, () -> list.removeIf(e -> {
				if ("b".equals(e)) {
					throw new IllegalStateException();
				}
				return "x".equals(e);
			}));
			assertThat(list, contains("x", "a", "b"));
		}

		@Test
		void shouldThrowExceptionWhenPredicateModifiesListBeforeFirstMatch() {
			FastArrayList<String> list = listOf("a", "b");

			assertThrows(ConcurrentModificationException.class, () -> list.removeIf(e -> {
				list.add("c");
				return false;
			}));
		}

		@Test
		void shouldThrowExceptionWhenPredicateModifiesListAfterFirstMatch() {
			FastArrayList<String> list = listOf("x", "a", "b");

			assertThrows(ConcurrentModificationException.class, () -> list.removeIf(e -> {
				if ("a".equals(e)) {
					list.add("c");
				}
				return "x".equals(e);
			}));
		}

		@Test
		void shouldThrowExceptionForNullPredicate() {
			FastArrayList<String> list = listOf("a");

			assertThrows(NullPointerException.class, () -> list.removeIf(null));
		}

		@Test
		void shouldRemoveAllElementsContainedInCollection() {
			FastArrayList<String> list = listOf("a", "b", "c", "b");

			assertThat(list.removeAll(List.of("b", "x")), equalTo(true));
			assertThat(list, contains("a", "c"));
			assertThat(list.removeAll(List.of()), equalTo(false));
		}

		@Test
		void shouldRemoveAllElementsOfItself() {
			FastArrayList<String> list = listOf("a", "b");

			assertThat(list.removeAll(list), equalTo(true));
			assertThat(list, empty());
		}

		@Test
		void shouldRetainOnlyElementsContainedInCollection() {
			FastArrayList<String> list = listOf("a", "b", "c", "b");

			assertThat(list.retainAll(List.of("b", "x")), equalTo(true));
			assertThat(list, contains("b", "b"));
			assertThat(list.retainAll(List.of("b")), equalTo(false));
		}

		@Test
		void shouldThrowExceptionForNullCollection() {
			FastArrayList<String> list = listOf("a");

			assertThrows(NullPointerException.class, () -> list.removeAll(null));
			assertThrows(NullPointerException.class, () -> list.retainAll(null));
		}
	}

	@Nested
	class BulkOperationTests {

		@Test
		void shouldVisitElementsInOrder() {
			FastArrayList<String> list = listOf("a", "b", "c");
			List<String> visited = new ArrayList<>();
			list.forEach(visited::add);

			assertThat(visited, contains("a", "b", "c"));
		}

		@Test
		void shouldNotVisitAnythingForEmptyList() {
			FastArrayList<String> list = new FastArrayList<>(5);
			List<String> visited = new ArrayList<>();
			list.forEach(visited::add);

			assertThat(visited, empty());
		}

		@Test
		void shouldSeeValuesSetDuringForEachWhenStorageIsReplaced() {
			FastArrayList<Object> list = FastArrayList.<Object>wrap(new String[] { "a", "b" });
			List<Object> visited = new ArrayList<>();
			list.forEach(e -> {
				visited.add(e);
				if (1 == visited.size()) {
					list.set(1, 42);
				}
			});

			assertThat(visited, contains("a", 42));
		}

		@Test
		void shouldThrowExceptionWhenActionModifiesList() {
			FastArrayList<String> list = listOf("a", "b");

			assertThrows(ConcurrentModificationException.class, () -> list.forEach(e -> list.add("c")));
		}

		@Test
		void shouldThrowExceptionForNullAction() {
			FastArrayList<String> list = listOf("a");

			assertThrows(NullPointerException.class, () -> list.forEach(null));
		}

		@Test
		void shouldReplaceAllElements() {
			FastArrayList<String> list = listOf("a", "b");
			list.replaceAll(String::toUpperCase);

			assertThat(list, contains("A", "B"));
		}

		@Test
		void shouldThrowExceptionWhenOperatorModifiesList() {
			FastArrayList<String> list = listOf("a", "b");

			assertThrows(ConcurrentModificationException.class, () -> list.replaceAll(e -> {
				list.add("c");
				return e;
			}));
		}

		@Test
		void shouldInvalidateIteratorsOnReplaceAll() {
			FastArrayList<String> list = listOf("a", "b");
			Iterator<String> iterator = list.iterator();
			list.replaceAll(String::toUpperCase);

			assertThrows(ConcurrentModificationException.class, iterator::next);
		}

		@Test
		void shouldThrowExceptionForNullOperator() {
			FastArrayList<String> list = listOf("a");

			assertThrows(NullPointerException.class, () -> list.replaceAll(null));
		}

		@Test
		void shouldSortWithComparator() {
			FastArrayList<String> list = listOf("b", "c", "a");
			list.sort(Comparator.reverseOrder());

			assertThat(list, contains("c", "b", "a"));
		}

		@Test
		void shouldSortWithNaturalOrdering() {
			FastArrayList<String> list = listOf("b", "c", "a");
			list.sort(null);

			assertThat(list, contains("a", "b", "c"));
		}

		@Test
		void shouldSortOnlyTheElementsWithinSize() {
			FastArrayList<String> list = new FastArrayList<>(10);
			list.add("b");
			list.add("a");
			list.sort(null);

			assertThat(list, contains("a", "b"));
		}

		@Test
		void shouldSortWrappedTypedArrayInPlace() {
			String[] array = { "b", "a" };
			FastArrayList<String> list = FastArrayList.wrap(array);
			list.sort(null);

			assertThat(array, arrayContaining("a", "b"));
		}

		@Test
		void shouldThrowExceptionWhenComparatorModifiesList() {
			FastArrayList<String> list = listOf("b", "a");

			assertThrows(ConcurrentModificationException.class, () -> list.sort((first, second) -> {
				list.add("c");
				return first.compareTo(second);
			}));
		}

		@Test
		void shouldInvalidateIteratorsOnSort() {
			FastArrayList<String> list = listOf("b", "a");
			Iterator<String> iterator = list.iterator();
			list.sort(null);

			assertThrows(ConcurrentModificationException.class, iterator::next);
		}
	}

	@Nested
	class ToArrayTests {

		@Test
		void shouldReturnIndependentObjectArray() {
			FastArrayList<String> list = listOf("a", "b");
			Object[] array = list.toArray();
			array[0] = "z";

			assertThat(array.getClass(), equalTo(Object[].class));
			assertThat(list, contains("a", "b"));
		}

		@Test
		void shouldReturnObjectArrayForWrappedTypedArray() {
			FastArrayList<String> list = FastArrayList.wrap(new String[] { "a" });

			assertThat(list.toArray().getClass(), equalTo(Object[].class));
		}

		@Test
		void shouldReturnEmptyArrayForEmptyList() {
			FastArrayList<String> list = new FastArrayList<>(5);

			assertThat(list.toArray().length, equalTo(0));
		}

		@Test
		void shouldAllocateNewTypedArrayWhenGivenArrayIsTooSmall() {
			FastArrayList<String> list = listOf("a", "b");
			String[] target = new String[1];
			String[] result = list.toArray(target);

			assertThat(result, not(sameInstance(target)));
			assertThat(result, arrayContaining("a", "b"));
		}

		@Test
		void shouldFillGivenArrayWhenItHasExactSize() {
			FastArrayList<String> list = listOf("a", "b");
			String[] target = new String[2];
			String[] result = list.toArray(target);

			assertThat(result, sameInstance(target));
			assertThat(result, arrayContaining("a", "b"));
		}

		@Test
		void shouldTerminateWithNullWhenGivenArrayIsLarger() {
			FastArrayList<String> list = listOf("a");
			String[] target = { "x", "y", "z" };
			String[] result = list.toArray(target);

			assertThat(result, sameInstance(target));
			assertThat(result, arrayContaining("a", null, "z"));
		}

		@Test
		void shouldThrowExceptionForNullArray() {
			FastArrayList<String> list = listOf("a");

			assertThrows(NullPointerException.class, () -> list.toArray((String[]) null));
		}

		@Test
		void shouldThrowExceptionForIncompatibleArrayType() {
			FastArrayList<Object> list = new FastArrayList<>();
			list.add(1);

			assertThrows(ArrayStoreException.class, () -> list.toArray(new String[0]));
		}
	}

	@Nested
	class CapacityTests {

		@Test
		void shouldUseDefaultCapacityOnFirstAdd() {
			FastArrayList<String> list = new FastArrayList<>();
			list.add("a");

			assertThat(list.capacity(), equalTo(FastArrayList.Default.CAPACITY));
		}

		@Test
		void shouldGrowByHalf() {
			FastArrayList<Integer> list = new FastArrayList<>();
			for (int i = 0; i <= FastArrayList.Default.CAPACITY; ++i) {
				list.add(i);
			}

			assertThat(list.capacity(), equalTo(15));
		}

		@Test
		void shouldGrowFromZeroInitialCapacity() {
			FastArrayList<String> list = new FastArrayList<>(0);
			list.add("a");
			assertThat(list.capacity(), equalTo(1));

			list.add("b");
			assertThat(list.capacity(), equalTo(2));

			list.add("c");
			assertThat(list.capacity(), equalTo(3));
		}

		@Test
		void shouldNotAllocateForSmallCapacityOnDefaultList() {
			FastArrayList<String> list = new FastArrayList<>();
			list.ensureCapacity(5);

			assertThat(list.capacity(), equalTo(0));
		}

		@Test
		void shouldAllocateForLargeCapacityOnDefaultList() {
			FastArrayList<String> list = new FastArrayList<>();
			list.ensureCapacity(20);

			assertThat(list.capacity(), equalTo(20));
		}

		@Test
		void shouldEnsureCapacityOnNonDefaultList() {
			FastArrayList<String> list = new FastArrayList<>(0);
			list.ensureCapacity(3);

			assertThat(list.capacity(), equalTo(3));
		}

		@Test
		void shouldNotShrinkOnEnsureCapacity() {
			FastArrayList<String> list = new FastArrayList<>(5);
			list.ensureCapacity(3);
			list.ensureCapacity(-1);

			assertThat(list.capacity(), equalTo(5));
		}

		@Test
		void shouldGrowByAtLeastHalfOnEnsureCapacity() {
			FastArrayList<String> list = new FastArrayList<>(10);
			list.ensureCapacity(11);

			assertThat(list.capacity(), equalTo(15));
		}

		@Test
		void shouldTrimToSize() {
			FastArrayList<String> list = new FastArrayList<>(10);
			list.add("a");
			list.add("b");
			list.trimToSize();

			assertThat(list.capacity(), equalTo(2));
			assertThat(list, contains("a", "b"));
		}

		@Test
		void shouldTrimEmptyListToZeroCapacityAndStillAcceptElements() {
			FastArrayList<String> list = new FastArrayList<>(10);
			list.trimToSize();
			assertThat(list.capacity(), equalTo(0));

			list.add("a");
			assertThat(list, contains("a"));
		}

		@Test
		void shouldNotChangeCapacityWhenAlreadyTrimmed() {
			FastArrayList<String> list = FastArrayList.wrap(new String[] { "a" });
			list.trimToSize();

			assertThat(list.capacity(), equalTo(1));
		}
	}

	@Nested
	class ListContractTests {

		@Test
		void shouldBeRandomAccess() {
			assertThat(new FastArrayList<>(), instanceOf(RandomAccess.class));
		}

		@Test
		void shouldBeEqualToOtherListsWithSameElements() {
			FastArrayList<String> list = listOf("a", null);
			List<String> other = new ArrayList<>();
			other.add("a");
			other.add(null);

			assertThat(list, equalTo(other));
			assertThat(other, equalTo(list));
			assertThat(list.hashCode(), equalTo(other.hashCode()));
			assertThat(list.toString(), equalTo("[a, null]"));
		}

		@Test
		void shouldRemoveThroughIterator() {
			FastArrayList<String> list = listOf("a", "b", "c");
			Iterator<String> iterator = list.iterator();
			iterator.next();
			iterator.next();
			iterator.remove();

			assertThat(list, contains("a", "c"));
			assertThat(iterator.next(), equalTo("c"));
		}

		@Test
		void shouldModifyThroughListIterator() {
			FastArrayList<String> list = listOf("a", "c");
			ListIterator<String> iterator = list.listIterator();
			iterator.next();
			iterator.set("x");
			iterator.add("b");

			assertThat(list, contains("x", "b", "c"));
		}

		@Test
		void shouldFailFastOnConcurrentModification() {
			FastArrayList<String> list = listOf("a", "b");
			Iterator<String> iterator = list.iterator();
			list.add("c");

			assertThrows(ConcurrentModificationException.class, iterator::next);
		}

		@Test
		void shouldModifyThroughSubList() {
			FastArrayList<String> list = listOf("a", "d");
			List<String> subList = list.subList(1, 1);
			subList.addAll(List.of("b", "c"));

			assertThat(list, contains("a", "b", "c", "d"));
			assertThat(subList.get(0), equalTo("b"));
		}

		@Test
		void shouldIterateOverNullElements() {
			FastArrayList<String> list = listOf((String) null);

			assertThat(list.iterator().next(), nullValue());
		}
	}
}
