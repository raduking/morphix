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
package org.morphix.convert;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.morphix.convert.Converter.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;
import org.morphix.reflection.GenericType;

/**
 * Test class for {@link Converter}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterTest {

	private static final int TEST_INT_13 = 13;
	private static final int TEST_INT_17 = 17;
	private static final String TEST_INT_13_STRING = String.valueOf(TEST_INT_13);
	private static final String TEST_INT_17_STRING = String.valueOf(TEST_INT_17);

	public static class A {
		int x;
		int y;

		String s;

		public String getS() {
			return s;
		}
	}

	public static class B {
		String x;
		String y;

		String t;

		public void setT(final String t) {
			this.t = t;
		}
	}

	@Test
	void shouldConvertAtoB() {
		A a = new A();
		a.x = TEST_INT_13;

		B b = convert(a).to(B::new);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo(TEST_INT_13_STRING));
	}

	@Test
	void shouldConvertAtoBAndExtraConvertFunction() {
		A a = new A();
		a.x = TEST_INT_13;

		B b = convert(a).to(B::new, (s, d) -> d.x = "x=" + d.x);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo("x=" + TEST_INT_13_STRING));
	}

	@Test
	void shouldConvertAtoExistingB() {
		A a = new A();
		a.x = TEST_INT_13;

		B existingB = new B();
		B b = convert(a).to(existingB);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo(TEST_INT_13_STRING));
	}

	@Test
	void shouldConvertAtoBWithClassSyntax() {
		A a = new A();
		a.x = TEST_INT_13;

		B b = convert(a).to(B.class);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo(TEST_INT_13_STRING));
	}

	@Test
	void shouldConvertAtoBWithExtraConvertFunctionV1() {
		A a = new A();
		a.x = TEST_INT_13;
		a.y = TEST_INT_17;

		B b = convert(a)
				.with((final A s, final B d) -> d.x = "x=" + d.x)
				.to(B::new);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo("x=" + TEST_INT_13_STRING));
		assertThat(b.y, equalTo(TEST_INT_17_STRING));
	}

	@Test
	void shouldConvertAtoBWithExtraConvertFunctionV2() {
		A a = new A();
		a.x = TEST_INT_13;
		a.y = TEST_INT_17;

		B b = convert(a)
				.<B>with((s, d) -> d.x = "x=" + d.x)
				.to(B::new);

		assertThat(b, notNullValue());
		assertThat(b.x, equalTo("x=" + TEST_INT_13_STRING));
		assertThat(b.y, equalTo(TEST_INT_17_STRING));
	}

	@Test
	void shouldThrowExceptionIfExtraConvertHasWrongTypes() {
		A a = new A();

		ClassCastException result = null;
		try {
			convert(a)
					.with((final A s, final A d) -> d.x = 1 + d.x)
					.to(B::new);
		} catch (ClassCastException e) {
			result = e;
		}
		assertThat(result, notNullValue());
	}

	@Test
	void shouldKeepDeclaredOrderForExtraConvertFunctions() {
		List<Integer> order = new ArrayList<>();

		convert(new A())
				.with((final A s, final B d) -> {
					order.add(1);
				})
				.with((final A s, final B d) -> {
					order.add(2);
				})
				.to(B::new);

		List<Integer> expected = List.of(1, 2);

		assertThat(order, equalTo(expected));
	}

	@Test
	void shouldConvertSetToList() {
		Set<Integer> set = Set.of(1, 2, 3);

		List<String> list = convert(set).to(new GenericClass<List<String>>() {
			// empty
		});

		assertThat(list, containsInAnyOrder("1", "2", "3"));
	}

	@Test
	void shouldConvertSetToListWithBuiltGenericClassAndType() {
		Set<Integer> set = Set.of(1, 2, 3);

		List<String> list = convert(set).to(GenericClass.of(GenericType.of(List.class, String.class)));

		assertThat(list, containsInAnyOrder("1", "2", "3"));
	}

	@Test
	void shouldConvertSetToListWithBuiltGenericType() {
		Set<Integer> set = Set.of(1, 2, 3);

		List<String> list = convert(set).to(GenericType.of(List.class, String.class));

		assertThat(list, containsInAnyOrder("1", "2", "3"));
	}

	@Test
	void shouldConvertSetOfSetToListOfListWithBuiltGenericType() {
		Set<Set<Integer>> set = Set.of(Set.of(1), Set.of(2), Set.of(3));

		List<List<String>> list = convert(set).to(GenericType.of(List.class, GenericType.of(List.class, String.class)));

		assertThat(list, containsInAnyOrder(List.of("1"), List.of("2"), List.of("3")));
	}

	static class Empty {
		// empty
	}

	@Test
	void shouldConvertIfDestinationIsEmpty() {
		A a = new A();
		Empty empty = convert(a).to(Empty.class);

		assertThat(empty, notNullValue());
	}

	static class OnlyGetters {

		public int getX() {
			return TEST_INT_13;
		}
	}

	@Test
	void shouldConvertIfDestinationOnlyHasGetters() {
		A a = new A();
		OnlyGetters empty = convert(a).to(OnlyGetters.class);

		assertThat(empty, notNullValue());
	}

	public static record RA(int x, int y) {
		// empty
	}

	@Test
	void shouldConvertRecordToClass() {
		RA ra = new RA(TEST_INT_13, TEST_INT_17);

		A a = convert(ra).to(A.class);

		assertThat(a.x, equalTo(TEST_INT_13));
		assertThat(a.y, equalTo(TEST_INT_17));
	}

	@Disabled("Functionality not implemented yet.")
	@Test
	void shouldConvertClassToRecord() {
		A a = new A();
		a.x = TEST_INT_13;
		a.y = TEST_INT_17;

		RA ra = convert(a).to(RA.class);

		assertThat(ra.x(), equalTo(TEST_INT_13));
		assertThat(ra.y(), equalTo(TEST_INT_17));
	}
}
