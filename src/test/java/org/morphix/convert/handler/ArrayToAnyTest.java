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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.morphix.convert.ArrayConversions.convertArray;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests conversions from array to any.
 *
 * @author Radu Sebastian LAZIN
 */
class ArrayToAnyTest {

	private static final Integer TEST_INTEGER_1 = 7;
	private static final Integer TEST_INTEGER_2 = 13;
	private static final String TEST_STRING_1 = "7";
	private static final String TEST_STRING_2 = "13";

	public static class A {
		int x;

		@Override
		public boolean equals(final Object obj) {
			// basic equals implementation
			if (null == obj)
				return false;
			return Objects.equals(x, ((A) obj).x);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
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

	@Test
	void shouldNotConvertArraysToObjects() {
		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		A[] as = new A[] { a1, a2 };

		B b = convertArray(as, B::new).toAny(B.class);

		assertThat(b, equalTo(null));
	}

	@Test
	void shouldConvertArraysToArrays() {
		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		A[] as = new A[] { a1, a2 };

		B[] bs = convertArray(as, B::new).toAny(B[].class);

		assertThat(bs[0].x, equalTo(TEST_STRING_1));
		assertThat(bs[1].x, equalTo(TEST_STRING_2));
	}

	@Test
	void shouldConvertArraysToList() {
		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		A[] as = new A[] { a1, a2 };

		List<B> bs = convertArray(as, B::new).toList();

		assertThat(bs.get(0).x, equalTo(TEST_STRING_1));
		assertThat(bs.get(1).x, equalTo(TEST_STRING_2));
	}

	@Test
	void shouldConvertArraysToSet() {
		Integer[] as = new Integer[] { TEST_INTEGER_1, TEST_INTEGER_2 };

		Set<String> bs = convertArray(as, s -> String.valueOf(s)).toSet();

		assertThat(bs, containsInAnyOrder(TEST_STRING_1, TEST_STRING_2));
	}
}
