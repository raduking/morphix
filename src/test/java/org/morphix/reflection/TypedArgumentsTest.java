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
package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link TypedArguments}.
 *
 * @author Radu Sebastian LAZIN
 */
class TypedArgumentsTest {

	private static final String FIRST_VALUE = "firstValue";
	private static final String SECOND_VALUE = "secondValue";

	@Nested
	class ConstructorTest {

		@Test
		void shouldCreateTypedArgumentsFromList() {
			List<TypedArgument> list = List.of(TypedArgument.of(String.class, FIRST_VALUE));

			TypedArguments arguments = new TypedArguments(list);

			assertThat(arguments.arguments(), equalTo(list));
		}

		@Test
		void shouldThrowWhenArgumentsIsNull() {
			NullPointerException nullPointerException =
					assertThrows(NullPointerException.class, () -> new TypedArguments(null));

			assertThat(nullPointerException.getMessage(), equalTo("arguments must not be null"));
		}

		@Test
		void shouldThrowWhenArgumentsContainNull() {
			List<TypedArgument> list = new ArrayList<>();
			list.add(TypedArgument.of(String.class, FIRST_VALUE));
			list.add(null);

			assertThrows(NullPointerException.class, () -> new TypedArguments(list));
		}

		@Test
		void shouldCopyTheArgumentsList() {
			List<TypedArgument> list = new ArrayList<>();
			list.add(TypedArgument.of(String.class, FIRST_VALUE));

			TypedArguments arguments = new TypedArguments(list);
			list.add(TypedArgument.of(String.class, SECOND_VALUE));

			assertThat(arguments.size(), equalTo(1));
			assertThat(arguments.arguments(), equalTo(List.of(TypedArgument.of(String.class, FIRST_VALUE))));
		}

		@Test
		void shouldReturnUnmodifiableArguments() {
			TypedArguments arguments = new TypedArguments(List.of(TypedArgument.of(String.class, FIRST_VALUE)));

			List<TypedArgument> list = arguments.arguments();

			assertThrows(UnsupportedOperationException.class, () -> list.add(TypedArgument.of(String.class, SECOND_VALUE)));
		}

		@Test
		void shouldCreateEmptyTypedArgumentsFromEmptyList() {
			TypedArguments arguments = new TypedArguments(List.of());

			assertThat(arguments.isEmpty(), equalTo(true));
		}
	}

	@Nested
	class OfVarargsTest {

		@Test
		void shouldCreateTypedArgumentsFromGivenArguments() {
			TypedArguments arguments = TypedArguments.of(
					TypedArgument.of(String.class, FIRST_VALUE),
					TypedArgument.of(Integer.class, 666));

			List<TypedArgument> expected = List.of(
					TypedArgument.of(String.class, FIRST_VALUE),
					TypedArgument.of(Integer.class, 666));

			assertThat(arguments.arguments(), equalTo(expected));
		}

		@Test
		void shouldThrowWhenArgumentsArrayIsNull() {
			NullPointerException nullPointerException =
					assertThrows(NullPointerException.class, () -> TypedArguments.of((TypedArgument[]) null));

			assertThat(nullPointerException.getMessage(), equalTo("arguments must not be null"));
		}

		@Test
		void shouldCreateEmptyTypedArgumentsWhenNoArgumentsAreGiven() {
			TypedArguments arguments = TypedArguments.of();

			assertThat(arguments.isEmpty(), equalTo(true));
		}
	}

	@Nested
	class OfSingleArgumentTest {

		@Test
		void shouldCreateTypedArgumentsWithSingleArgument() {
			TypedArguments arguments = TypedArguments.of(Integer.class, 666);

			assertThat(arguments.arguments(), equalTo(List.of(TypedArgument.of(Integer.class, 666))));
		}

		@Test
		void shouldCreateTypedArgumentsWithNullValue() {
			TypedArguments arguments = TypedArguments.of(String.class, null);

			assertThat(arguments.arguments().get(0), equalTo(TypedArgument.of(String.class, null)));
		}

		@Test
		void shouldThrowWhenTypeIsNull() {
			NullPointerException nullPointerException =
					assertThrows(NullPointerException.class, () -> TypedArguments.of((Class<?>) null, FIRST_VALUE));

			assertThat(nullPointerException.getMessage(), equalTo("type must not be null"));
		}
	}

	@Nested
	class TypesTest {

		@Test
		void shouldReturnArgumentTypes() {
			TypedArguments arguments = TypedArguments.of(
					TypedArgument.of(String.class, FIRST_VALUE),
					TypedArgument.of(Integer.class, 666));

			Class<?>[] types = arguments.types();

			assertThat(types.length, equalTo(2));
			assertThat(types[0], equalTo(String.class));
			assertThat(types[1], equalTo(Integer.class));
		}

		@Test
		void shouldReturnEmptyArrayWhenThereAreNoArguments() {
			Class<?>[] types = TypedArguments.of().types();

			assertThat(types.length, equalTo(0));
		}
	}

	@Nested
	class ValuesTest {

		@Test
		void shouldReturnArgumentValues() {
			TypedArguments arguments = TypedArguments.of(
					TypedArgument.of(String.class, FIRST_VALUE),
					TypedArgument.of(Integer.class, null));

			Object[] values = arguments.values();

			assertThat(values.length, equalTo(2));
			assertThat(values[0], equalTo(FIRST_VALUE));
			assertThat(values[1], nullValue());
		}

		@Test
		void shouldReturnEmptyArrayWhenThereAreNoArguments() {
			Object[] values = TypedArguments.of().values();

			assertThat(values.length, equalTo(0));
		}
	}

	@Nested
	class IsEmptyTest {

		@Test
		void shouldReturnTrueWhenThereAreNoArguments() {
			TypedArguments arguments = TypedArguments.of();

			assertThat(arguments.isEmpty(), equalTo(true));
		}

		@Test
		void shouldReturnFalseWhenThereAreArguments() {
			TypedArguments arguments = TypedArguments.of(String.class, FIRST_VALUE);

			assertThat(arguments.isEmpty(), equalTo(false));
		}
	}

	@Nested
	class SizeTest {

		@Test
		void shouldReturnNumberOfArguments() {
			TypedArguments arguments = TypedArguments.of(
					TypedArgument.of(String.class, FIRST_VALUE),
					TypedArgument.of(Integer.class, 666));

			assertThat(arguments.size(), equalTo(2));
		}

		@Test
		void shouldReturnZeroWhenThereAreNoArguments() {
			TypedArguments arguments = TypedArguments.of();

			assertThat(arguments.size(), equalTo(0));
		}
	}

	@Nested
	class EqualsAndHashCodeTest {

		@Test
		void shouldBeEqualWhenArgumentsAreEqual() {
			TypedArguments first = TypedArguments.of(String.class, FIRST_VALUE);
			TypedArguments second = TypedArguments.of(String.class, FIRST_VALUE);

			assertThat(first, equalTo(second));
			assertThat(first.hashCode(), equalTo(second.hashCode()));
		}

		@Test
		void shouldNotBeEqualWhenArgumentsDiffer() {
			TypedArguments first = TypedArguments.of(String.class, FIRST_VALUE);
			TypedArguments second = TypedArguments.of(String.class, SECOND_VALUE);

			assertThat(first, not(equalTo(second)));
		}
	}

	@Nested
	class ToStringTest {

		@Test
		void shouldReturnToStringWithArguments() {
			TypedArguments arguments = TypedArguments.of(String.class, FIRST_VALUE);

			String result = arguments.toString();

			assertThat(result, equalTo(
					"TypedArguments[arguments=[TypedArgument[type=class java.lang.String, value=" + FIRST_VALUE + "]]]"));
		}
	}
}
