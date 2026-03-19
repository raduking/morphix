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
package org.morphix.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link JavaObjects}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaObjectsTest {

	private static final String BUBU = "Bubu";
	private static final String CUCU = "Cucu";

	@Nested
	class CastTests {

		@Test
		void shouldCastToInferredType() {
			Object o = BUBU;

			String bubu = JavaObjects.cast(o);

			assertThat(bubu, notNullValue());
			assertThat(bubu, equalTo(BUBU));
		}

		@Test
		void shouldThrowExceptionWhenTryingToInstantiate() {
			UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(JavaObjects.class);

			assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
		}

		@Test
		void shouldThrowClassCastExceptionWhenObjectCannotBeCastToInferredType() {
			String x = CUCU;

			ClassCastException e = assertThrows(ClassCastException.class, () -> {
				@SuppressWarnings("unused")
				Integer xi = JavaObjects.cast(x);
			});

			assertThat(e, notNullValue());
		}
	}

	@Nested
	class IsEmptyTests {

		@Test
		void shouldReturnTrueOnNull() {
			boolean result = JavaObjects.isEmpty(null);

			assertTrue(result);
		}

		@Test
		void shouldReturnFalseOnNonNull() {
			Object o = new Object();

			boolean result = JavaObjects.isEmpty(o);

			assertFalse(result);
		}

		@Test
		void shouldReturnTrueOnEmptyString() {
			Object o = "";

			boolean result = JavaObjects.isEmpty(o);

			assertTrue(result);
		}

		@Test
		void shouldReturnFalseOnNonEmptyString() {
			Object o = BUBU;

			boolean result = JavaObjects.isEmpty(o);

			assertFalse(result);
		}

		@Test
		void shouldReturnTrueOnEmptyCollection() {
			Collection<?> o = mock(Collection.class);
			doReturn(true).when(o).isEmpty();

			boolean result = JavaObjects.isEmpty(o);

			assertTrue(result);
		}

		@Test
		void shouldReturnFalseOnNonEmptyCollection() {
			Collection<?> o = mock(Collection.class);
			doReturn(false).when(o).isEmpty();

			boolean result = JavaObjects.isEmpty(o);

			assertFalse(result);
		}

		@Test
		void shouldReturnTrueOnEmptyMap() {
			Map<?, ?> o = mock(Map.class);
			doReturn(true).when(o).isEmpty();

			boolean result = JavaObjects.isEmpty(o);

			assertTrue(result);
		}

		@Test
		void shouldReturnFalseOnNonEmptyMap() {
			Map<?, ?> o = mock(Map.class);
			doReturn(false).when(o).isEmpty();

			boolean result = JavaObjects.isEmpty(o);

			assertFalse(result);
		}

		@Test
		void shouldReturnTrueOnEmptyOptional() {
			Object o = Optional.empty();

			boolean result = JavaObjects.isEmpty(o);

			assertTrue(result);
		}

		@Test
		void shouldReturnFalseOnNonEmptyOptional() {
			Object o = Optional.of(BUBU);

			boolean result = JavaObjects.isEmpty(o);

			assertFalse(result);
		}

		@Test
		void shouldReturnTrueOnEmptyArray() {
			Object o = new Object[] { };

			boolean result = JavaObjects.isEmpty(o);

			assertTrue(result);
		}

		@Test
		void shouldReturnFalseOnNonEmptyArray() {
			Object o = new Object[] { BUBU };

			boolean result = JavaObjects.isEmpty(o);

			assertFalse(result);
		}
	}
}
