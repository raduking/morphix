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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link TypedArgument}.
 *
 * @author Radu Sebastian LAZIN
 */
class TypedArgumentTest {

	private static final String FIRST_VALUE = "firstValue";
	private static final String SECOND_VALUE = "secondValue";

	@Nested
	class ConstructorTest {

		@Test
		void shouldCreateTypedArgumentWithTypeAndValue() {
			TypedArgument argument = new TypedArgument(String.class, FIRST_VALUE);

			assertThat(argument.type(), equalTo(String.class));
			assertThat(argument.value(), equalTo(FIRST_VALUE));
		}

		@Test
		void shouldCreateTypedArgumentWithNullValue() {
			TypedArgument argument = new TypedArgument(String.class, null);

			assertThat(argument.type(), equalTo(String.class));
			assertThat(argument.value(), equalTo(null));
		}
	}

	@Nested
	class OfTest {

		@Test
		void shouldCreateTypedArgumentWithTypeAndValue() {
			TypedArgument argument = TypedArgument.of(Integer.class, 666);

			assertThat(argument.type(), equalTo(Integer.class));
			assertThat(argument.value(), equalTo(666));
		}

		@Test
		void shouldCreateTypedArgumentWithNullValue() {
			TypedArgument argument = TypedArgument.of(String.class, null);

			assertThat(argument.type(), equalTo(String.class));
			assertThat(argument.value(), equalTo(null));
		}

		@Test
		void shouldThrowWhenTypeIsNull() {
			NullPointerException nullPointerException =
					assertThrows(NullPointerException.class, () -> TypedArgument.of(null, FIRST_VALUE));

			assertThat(nullPointerException.getMessage(), equalTo("type must not be null"));
		}
	}

	@Nested
	class EqualsAndHashCodeTest {

		@Test
		void shouldBeEqualWhenTypeAndValueAreEqual() {
			TypedArgument first = TypedArgument.of(String.class, FIRST_VALUE);
			TypedArgument second = TypedArgument.of(String.class, FIRST_VALUE);

			assertThat(first, equalTo(second));
			assertThat(first.hashCode(), equalTo(second.hashCode()));
		}

		@Test
		void shouldNotBeEqualWhenTypeDiffers() {
			TypedArgument first = TypedArgument.of(String.class, FIRST_VALUE);
			TypedArgument second = TypedArgument.of(Integer.class, FIRST_VALUE);

			assertThat(first, not(equalTo(second)));
		}

		@Test
		void shouldNotBeEqualWhenValueDiffers() {
			TypedArgument first = TypedArgument.of(String.class, FIRST_VALUE);
			TypedArgument second = TypedArgument.of(String.class, SECOND_VALUE);

			assertThat(first, not(equalTo(second)));
		}
	}

	@Nested
	class ToStringTest {

		@Test
		void shouldReturnToStringWithTypeAndValue() {
			TypedArgument argument = TypedArgument.of(String.class, FIRST_VALUE);

			String result = argument.toString();

			assertThat(result, equalTo("TypedArgument[type=class java.lang.String, value=" + FIRST_VALUE + "]"));
		}
	}
}
