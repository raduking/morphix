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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ReflectionException;

/**
 * Test class for {@link Nullables}.
 *
 * @author Radu Sebastian LAZIN
 */
class NullablesTest {

	private static final String SOME_ERROR_MESSAGE = "Some error message";
	private static final String SOME_OTHER_ERROR_MESSAGE = "Other error message";
	private static final String MUMU = "mumu";
	private static final String BIBI = "bibi";

	@Test
	void shouldReturnValueOnNestingWithAndNotNull() {
		A a = new A();
		B b = new B();
		b.s = MUMU;
		a.b = b;

		String result = Nullables.whenNotNull(a)
				.andNotNull(A::getB)
				.andNotNull(B::getS)
				.valueOrDefault(BIBI);

		assertThat(result, equalTo(MUMU));
	}

	@Test
	void shouldReturnValueWithNonNullAlias() {
		String a = null;

		String result = Nullables.notNull(a)
				.orElse(() -> MUMU);

		assertThat(result, equalTo(MUMU));
	}

	@Test
	void shouldReturnDefaultWhenParameterIsNull() {
		B b = null;

		String result = Nullables.whenNotNull(b)
				.thenYield(B::getS)
				.orElse(() -> MUMU);

		assertThat(result, equalTo(MUMU));
	}

	@Test
	void shouldReturnYieldedWhenParameterIsNotNull() {
		B b = new B();
		b.s = BIBI;

		String result = Nullables.whenNotNull(b)
				.thenYield(B::getS)
				.orElse(() -> MUMU);

		assertThat(result, equalTo(BIBI));
	}

	@Test
	void shouldReturnDefaultOnNestingAndNotNullEvenIfOnePathIsNull() {
		A a = new A();

		String result = Nullables.whenNotNull(a)
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
			Nullables.whenNotNull(a)
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
	void shouldFailFastWhenErrorMessageIsProvidedOnNestingWithAndNotNullWithErrorMessageProvided() {
		A a = new A();
		B b = new B();
		a.b = b;

		Exception exception = null;
		try {
			Nullables.whenNotNull(a, SOME_OTHER_ERROR_MESSAGE)
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
	void shouldFailFastWhenErrorMessageIsProvidedOnNestingWithAndNotNullWithErrorMessageProvidedForNotNullAlias() {
		A a = new A();
		B b = new B();
		a.b = b;

		Exception exception = null;
		try {
			Nullables.notNull(a, SOME_OTHER_ERROR_MESSAGE)
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
	void shouldFailFastWhenErrorMessageIsProvided() {
		A a = null;

		Exception exception = null;
		try {
			Nullables.whenNotNull(a, SOME_ERROR_MESSAGE);
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
			Nullables.whenNotNull(a)
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
			Nullables.whenNotNull(a)
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
			Nullables.whenNotNull(a)
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
			Nullables.whenNotNull(null, SOME_ERROR_MESSAGE).thenReturn(() -> list.add(1));
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
			Nullables.notNull(null, SOME_ERROR_MESSAGE).orElse(() -> list.add(1));
		} catch (IllegalStateException e) {
			exception = e;
		}

		assertThat(exception, notNullValue());
		assertThat(exception.getMessage(), equalTo(SOME_ERROR_MESSAGE));
		assertThat(list, hasSize(0));
	}

    @Test
    void shouldNotThrowExceptionWhenAllObjectsAreNotNull() {
        Object obj1 = new Object();
        Object obj2 = "test";
        Object obj3 = 42;

        assertDoesNotThrow(() -> Nullables.requireNotNull(obj1, obj2, obj3));
    }

    @Test
    void shouldThrowNullPointerExceptionWhenAnyObjectIsNull() {
        Object obj1 = new Object();
        Object obj2 = null;
        Object obj3 = 42;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> Nullables.requireNotNull(obj1, obj2, obj3));
        assertThat(exception.getMessage(), is(nullValue()));
    }

    @Test
    void shouldThrowNullPointerExceptionWhenAllObjectsAreNull() {
        Object obj1 = null;
        Object obj2 = null;
        Object obj3 = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> Nullables.requireNotNull(obj1, obj2, obj3));
        assertThat(exception.getMessage(), is(nullValue()));
    }

    @Test
    void shouldNotThrowExceptionWhenNoObjectsAreProvided() {
    	assertDoesNotThrow(() -> Nullables.requireNotNull());
    }

	@Test
	void shouldThrowExceptionIfClassIsInstantiatedWithDefaultConstructor() {
		Throwable targetException = null;
		Constructor<Nullables> defaultConstructor = Constructors.getDeclaredConstructor(Nullables.class);
		try {
			Constructors.IgnoreAccess.newInstance(defaultConstructor);
		} catch (ReflectionException e) {
			targetException = ((InvocationTargetException) e.getCause()).getTargetException();
			assertThat(targetException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
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
