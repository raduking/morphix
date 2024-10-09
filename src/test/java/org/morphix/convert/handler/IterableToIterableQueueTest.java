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
package org.morphix.convert.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.Conversions.convertFrom;

import java.util.List;
import java.util.Objects;
import java.util.Queue;

import org.junit.jupiter.api.Test;

/**
 * Tests conversions from {@link Iterable} to {@link Iterable} when {@link Queue} is involved.
 *
 * @author Radu Sebastian LAZIN
 */
class IterableToIterableQueueTest {

	private static final Long TEST_LONG = 17L;
	private static final String TEST_STRING = "17";

	private static final Integer TEST_INTEGER_1 = 7;
	private static final Integer TEST_INTEGER_2 = 13;

	private static final String TEST_STRING_1 = "7";
	private static final String TEST_STRING_2 = "13";

	public static class A {
		int x;
	}

	public static class B {
		String x;

		@Override
		public boolean equals(final Object obj) {
			// basic equals implementation
			if (null == obj)
				return false;
			return Objects.equals(x, ((B) obj).x);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

	public static class Source {
		Long id;
		List<A> bees;
	}

	public static class Destination {
		String id;
		Queue<B> bees;

		public Queue<B> getBees() {
			return bees;
		}
	}

	@Test
	void shouldConvertListsToQueues() {
		Source src = new Source();
		src.id = TEST_LONG;

		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		src.bees = List.of(a1, a2);

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(TEST_STRING));

		B b1 = new B();
		b1.x = TEST_STRING_1;
		B b2 = new B();
		b2.x = TEST_STRING_2;
		List<B> expectedBees = List.of(b1, b2);

		assertThat(dst.bees, equalTo(expectedBees));
	}

}
