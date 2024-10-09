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
package org.morphix.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.morphix.lang.Nullables.notNull;
import static org.morphix.lang.Nullables.whenNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Nullables}.
 *
 * @author Radu Sebastian LAZIN
 */
class NullablesTest {

	private static final String SOME_ERROR_MESSAGE = "Some error message";
	private static final String MUMU = "mumu";
	private static final String BIBI = "bibi";

	@Test
	void shouldReturnValueOnNestingWithAndNotNull() {
		A a = new A();
		B b = new B();
		b.s = MUMU;
		a.b = b;

		String result = whenNotNull(a)
				.andNotNull(A::getB)
				.andNotNull(B::getS)
				.valueOrDefault(BIBI);

		assertThat(result, equalTo(MUMU));
	}

	@Test
	void shouldReturnValueWithNonNullAlias() {
		String a = null;

		String result = notNull(a)
				.orElse(() -> MUMU);

		assertThat(result, equalTo(MUMU));
	}

	@Test
	void shouldReturnDefaultWhenParameterIsNull() {
		B b = null;

		String result = whenNotNull(b)
				.thenYield(B::getS)
				.orElse(() -> MUMU);

		assertThat(result, equalTo(MUMU));
	}

	@Test
	void shouldReturnYieldedWhenParameterIsNotNull() {
		B b = new B();
		b.s = BIBI;

		String result = whenNotNull(b)
				.thenYield(B::getS)
				.orElse(() -> MUMU);

		assertThat(result, equalTo(BIBI));
	}

	@Test
	void shouldReturnDefaultOnNestingAndNotNullEvenIfOnePathIsNull() {
		A a = new A();

		String result = whenNotNull(a)
				.andNotNull(A::getB)
				.andNotNull(B::getS)
				.valueOrDefault(BIBI);

		assertThat(result, equalTo(BIBI));
	}

	@SuppressWarnings("null")
	@Test
	void shouldFailFastWhenErrorMessageIsProvidedOnNestingWithAndNotNull() {
		A a = new A();
		B b = new B();
		a.b = b;

		Exception exception = null;
		try {
			whenNotNull(a)
					.andNotNull(A::getB)
					.andNotNull(B::getS)
					.valueOrError(SOME_ERROR_MESSAGE);
		} catch (IllegalStateException e) {
			exception = e;
		}

		assertThat(exception, notNullValue());
		assertThat(exception.getMessage(), equalTo(SOME_ERROR_MESSAGE));
	}

	@SuppressWarnings("null")
	@Test
	void shouldFailFastWhenErrorMessageIsProvidedOnNestingWithAndNotNullAndUsingValueWithPredicate() {
		A a = new A();
		B b = new B();
		b.s = "";
		a.b = b;

		Exception exception = null;
		try {
			whenNotNull(a)
					.andNotNull(A::getB)
					.andNotNull(B::getS)
					.valueWhenOrError(NullablesTest::isNotBlank, SOME_ERROR_MESSAGE);
		} catch (IllegalStateException e) {
			exception = e;
		}

		assertThat(exception, notNullValue());
		assertThat(exception.getMessage(), equalTo(SOME_ERROR_MESSAGE));
	}

	@SuppressWarnings("null")
	@Test
	void shouldFailFastWhenErrorMessageIsProvidedOnNestingWithAndNotNullAndUsingValueWithPredicateTrueButValueNull() {
		A a = new A();
		B b = new B();
		b.s = null;
		a.b = b;

		Exception exception = null;
		try {
			whenNotNull(a)
					.andNotNull(A::getB)
					.andNotNull(B::getS)
					.valueWhenOrError(Objects::isNull, SOME_ERROR_MESSAGE);
		} catch (IllegalStateException e) {
			exception = e;
		}

		assertThat(exception, notNullValue());
		assertThat(exception.getMessage(), equalTo(SOME_ERROR_MESSAGE));
	}

	@SuppressWarnings("null")
	@Test
	void shouldFailWhenErrorMessageIsProvidedOnNestingWithAndNotNullAndUsingValueWithPredicateTrueButCannotReachValueOnChain() {
		A a = new A();

		Exception exception = null;
		try {
			whenNotNull(a)
					.andNotNull(A::getB)
					.andNotNull(B::getS)
					.valueWhenOrError(NullablesTest::isNotBlank, SOME_ERROR_MESSAGE);
		} catch (IllegalStateException e) {
			exception = e;
		}

		assertThat(exception, notNullValue());
		assertThat(exception.getMessage(), equalTo(SOME_ERROR_MESSAGE));
	}

	@SuppressWarnings("null")
	@Test
	void shouldFailFastWhenOnFirstCallValueIsNull() {
		List<Integer> list = new ArrayList<>();

		Exception exception = null;
		try {
			whenNotNull(null, SOME_ERROR_MESSAGE).thenReturn(() -> list.add(1));
		} catch (IllegalStateException e) {
			exception = e;
		}

		assertThat(exception, notNullValue());
		assertThat(exception.getMessage(), equalTo(SOME_ERROR_MESSAGE));
		assertThat(list, hasSize(0));
	}

	@SuppressWarnings("null")
	@Test
	void shouldFailFastWhenOnFirstCallValueIsNullWithAlias() {
		List<Integer> list = new ArrayList<>();

		Exception exception = null;
		try {
			notNull(null, SOME_ERROR_MESSAGE).orElse(() -> list.add(1));
		} catch (IllegalStateException e) {
			exception = e;
		}

		assertThat(exception, notNullValue());
		assertThat(exception.getMessage(), equalTo(SOME_ERROR_MESSAGE));
		assertThat(list, hasSize(0));
	}

	public static class A {

		private Integer i;

		private B b;

		public Integer getI() {
			return i;
		}

		public B getB() {
			return b;
		}

	}

	public static class B {

		private String s;

		public String getS() {
			return s;
		}

	}

	private static boolean isNotBlank(final String s) {
		return s != null && !s.isBlank();
	}

}
