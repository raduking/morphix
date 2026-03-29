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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link AlwaysEmptyList}.
 *
 * @author Radu Sebastian LAZIN
 */
class AlwaysEmptyListTest {

	@Test
	void shouldAlwaysBehaveAsAnEmptyList() {
		List<String> list = AlwaysEmptyList.of();

		assertThat(list.size(), equalTo(0));
		assertThat(list.isEmpty(), equalTo(true));

		assertThat(list.contains("x"), equalTo(false));
		assertThat(list.contains(null), equalTo(false));

		assertThat(list.indexOf("x"), equalTo(-1));
		assertThat(list.lastIndexOf("x"), equalTo(-1));

		assertThat(list.subList(0, 0), sameInstance(list));
	}

	@Test
	void shouldIgnoreAllMutatingOperations() {
		List<String> list = AlwaysEmptyList.of();

		assertThat(list.add("x"), equalTo(false));
		assertThat(list.addAll(List.of("a", "b")), equalTo(false));
		assertThat(list.addAll(0, List.of("a")), equalTo(false));

		assertThat(list.remove("x"), equalTo(false));
		assertThat(list.remove(0), equalTo(null));
		assertThat(list.removeFirst(), equalTo(null));
		assertThat(list.removeLast(), equalTo(null));
		assertThat(list.removeAll(List.of("x")), equalTo(false));
		assertThat(list.retainAll(List.of("x")), equalTo(false));

		assertThat(list.set(0, "x"), equalTo(null));
		list.add(0, "x");
		list.clear();

		assertThat(list.isEmpty(), equalTo(true));
		assertThat(list.size(), equalTo(0));
	}

	@Test
	void shouldHandleContainsAllCorrectly() {
		List<String> list = AlwaysEmptyList.of();

		assertThat(list.containsAll(List.of()), equalTo(true));
		assertThat(list.containsAll(List.of("x")), equalTo(false));
		assertThat(list.containsAll(null), equalTo(true));
	}

	@Test
	void shouldReturnNullOrEmptyOnElementAccess() {
		List<String> list = AlwaysEmptyList.of();

		assertThat(list.getFirst(), equalTo(null));
		assertThat(list.get(123), equalTo(null));
		assertThat(list.getLast(), equalTo(null));
	}

	@Test
	void shouldReturnEmptyArrayOnToArrayCalls() {
		List<String> list = AlwaysEmptyList.of();

		Object[] array = list.toArray();
		assertThat(array.length, equalTo(0));

		String[] target = new String[] { "x" };
		String[] result = list.toArray(target);

		assertThat(result, sameInstance(target));
		assertThat(result[0], equalTo(null));

		target = new String[] { };
		result = list.toArray(target);
		assertThat(result.length, equalTo(0));

		target = null;
		result = list.toArray(target);
		assertThat(result, equalTo(null));
	}

	@Test
	void shouldProvideEmptyIterators() {
		List<String> list = AlwaysEmptyList.of();

		Iterator<String> it = list.iterator();
		assertThat(it.hasNext(), equalTo(false));
		assertDoesNotThrow(it::remove);
		assertDoesNotThrow(() -> it.forEachRemaining(null));

		assertThatThrownBy(it::next, NoSuchElementException.class);

		ListIterator<String> lit = list.listIterator();
		assertThat(lit.hasNext(), equalTo(false));
		assertThat(lit.hasPrevious(), equalTo(false));
		assertThat(lit.nextIndex(), equalTo(0));
		assertThat(lit.previousIndex(), equalTo(-1));

		lit = list.listIterator(10);
		assertThat(lit.hasNext(), equalTo(false));
		assertThat(lit.hasPrevious(), equalTo(false));
		assertThat(lit.nextIndex(), equalTo(0));
		assertThat(lit.previousIndex(), equalTo(-1));

		assertThatThrownBy(lit::next, NoSuchElementException.class);
		assertThatThrownBy(lit::previous, NoSuchElementException.class);

		// mutation methods are no-ops
		lit.add("x");
		lit.set("x");
	}

	private static void assertThatThrownBy(final Runnable action, final Class<? extends Throwable> type) {
		try {
			action.run();
		} catch (Throwable t) {
			assertThat(t, instanceOf(type));
			return;
		}
		throw new AssertionError("Expected exception of type " + type.getName());
	}
}
