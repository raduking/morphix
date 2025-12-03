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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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
	private static final String CUCU = "cucu";

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
	void shouldReturnValueOnNestingWithAndNotNullAndOrElse() {
		A a = new A();
		B b = new B();
		b.s = MUMU;
		a.b = b;

		String result = Nullables.whenNotNull(a)
				.andNotNull(A::getB)
				.andNotNull(B::getS)
				.orElse(BIBI);

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
	void shouldReturnDefaultWhenParameterIsNullOnThenNotNull() {
		B b = null;

		String result = Nullables.whenNotNull(b)
				.thenNotNull(B::getS)
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
	void shouldReturnNonNullWhenParameterIsNotNull() {
		B b = new B();
		b.s = BIBI;

		String result = Nullables.whenNotNull(b)
				.thenNotNull(B::getS)
				.orElse(() -> MUMU);

		assertThat(result, equalTo(BIBI));
	}

	@Test
	void shouldReturnYieldedSuppliedWhenParameterIsNotNull() {
		B b = new B();
		b.s = BIBI;

		String result = Nullables.whenNotNull(b)
				.thenYield(() -> CUCU)
				.orElse(() -> MUMU);

		assertThat(result, equalTo(CUCU));
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

	@Test
	void shouldReturnSuppliedWhenParameterIsNotNull() {
		B b = new B();

		String result = Nullables.whenNotNull(b)
				.thenReturn(() -> CUCU);

		assertThat(result, equalTo(CUCU));
	}

	@Test
	void shouldReturnThisWhenParameterIsNotNull() {
		B b = new B();

		B result = Nullables.whenNotNull(b)
				.thenReturn();

		assertThat(result, equalTo(b));
	}

	@Test
	void shouldReturnFunctionResultWhenParameterIsNotNull() {
		B b = new B();
		b.s = CUCU;

		String result = Nullables.whenNotNull(b)
				.thenReturn(B::getS);

		assertThat(result, equalTo(CUCU));
	}

	@Test
	void shouldReturnDefaultNotFunctionResultWhenParameterIsNull() {
		B b = null;

		String result = Nullables.whenNotNull(b)
				.thenOrDefault(B::getS, () -> MUMU);

		assertThat(result, equalTo(MUMU));
	}

	@Test
	void shouldReturnFunctionNotDefaultResultWhenParameterIsNotNull() {
		B b = new B();
		b.s = CUCU;

		String result = Nullables.whenNotNull(b)
				.thenOrDefault(B::getS, () -> MUMU);

		assertThat(result, equalTo(CUCU));
	}

	@Test
	void shouldReturnNullWhenParameterIsNull() {
		String result = Nullables.whenNotNull(null)
				.thenReturn(() -> CUCU);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldConsumeWhenParameterIsNotNull() {
		B b = new B();

		@SuppressWarnings("unchecked")
		Consumer<B> consumer = mock(Consumer.class);

		Nullables.whenNotNull(b).then(consumer);

		verify(consumer).accept(b);
	}

	@Test
	void shouldNotConsumeWhenParameterIsNull() {
		B b = null;

		@SuppressWarnings("unchecked")
		Consumer<B> consumer = mock(Consumer.class);

		Nullables.whenNotNull(b).then(consumer);

		verifyNoInteractions(consumer);
	}

	@Test
	void shouldRunWhenParameterIsNotNull() {
		B b = new B();

		Runnable runnable = mock(Runnable.class);

		Nullables.whenNotNull(b).then(runnable);

		verify(runnable).run();
	}

	@Test
	void shouldNotRunWhenParameterIsNull() {
		B b = null;

		Runnable runnable = mock(Runnable.class);

		Nullables.whenNotNull(b).then(runnable);

		verifyNoInteractions(runnable);
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

	@Test
	void shouldReturnValueWhenErrorMessageIsProvidedOnNestingWithAndNotNullIfNotNull() {
		A a = new A();
		B b = new B();
		a.b = b;
		b.s = CUCU;

		String result = Nullables.whenNotNull(a)
				.andNotNull(A::getB)
				.andNotNull(B::getS)
				.valueOrError(SOME_ERROR_MESSAGE);

		assertThat(result, equalTo(CUCU));
	}

	@Test
	void shouldReturnValueWhenDefaultIsProvidedOnNestingWithAndNotNullIfNotNullAndMatchesPredicate() {
		A a = new A();
		B b = new B();
		a.b = b;
		b.s = CUCU;

		String result = Nullables.whenNotNull(a)
				.andNotNull(A::getB)
				.andNotNull(B::getS)
				.valueWhenOrDefault(s -> s.equals(CUCU), MUMU);

		assertThat(result, equalTo(CUCU));
	}

	@Test
	void shouldReturnValueWhenDefaultIsProvidedOnNestingWithAndNotNullIfNotNullAndMatchesPredicateWithErrorMessage() {
		A a = new A();
		B b = new B();
		a.b = b;
		b.s = CUCU;

		String result = Nullables.whenNotNull(a)
				.andNotNull(A::getB)
				.andNotNull(B::getS)
				.valueWhenOrError(s -> s.equals(CUCU), SOME_ERROR_MESSAGE);

		assertThat(result, equalTo(CUCU));
	}

	@Test
	void shouldReturnValueWhenDefaultIsProvidedOnNestingWithAndNotNullIfNotNullAndNotMatchesPredicate() {
		A a = new A();
		B b = new B();
		a.b = b;
		b.s = CUCU;

		String result = Nullables.whenNotNull(a)
				.andNotNull(A::getB)
				.andNotNull(B::getS)
				.valueWhenOrDefault(s -> s.equals(MUMU), BIBI);

		assertThat(result, equalTo(BIBI));
	}

	@Test
	void shouldReturnValueWhenDefaultIsProvidedOnNestingWithAndNotNullIfNullAndMatchesPredicate() {
		A a = new A();
		B b = new B();
		a.b = b;

		String result = Nullables.whenNotNull(a)
				.andNotNull(A::getB)
				.andNotNull(B::getS)
				.valueWhenOrDefault(s -> s == null, BIBI);

		assertThat(result, equalTo(BIBI));
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
	void shouldFailFastOnAndNotNullWhenErrorMessageIsProvided() {
		A a = new A();
		@SuppressWarnings("unchecked")
		Consumer<B> consumer = mock(Consumer.class);

		Exception exception = null;
		try {
			Nullables.whenNotNull(a)
					.andNotNull(A::getB, SOME_ERROR_MESSAGE)
					.then(consumer);
		} catch (IllegalStateException e) {
			exception = e;
		}

		assertThat(exception, notNullValue());
		assertThat(exception.getMessage(), equalTo(SOME_ERROR_MESSAGE));
		verifyNoInteractions(consumer);
	}

	@SuppressWarnings("null")
	@Test
	void shouldFailFastOnAndNotNullTwiceWhenErrorMessageIsProvided() {
		A a = new A();
		a.b = new B();
		@SuppressWarnings("unchecked")
		Consumer<String> consumer = mock(Consumer.class);

		Exception exception = null;
		try {
			Nullables.whenNotNull(a)
					.andNotNull(A::getB, SOME_ERROR_MESSAGE)
					.andNotNull(B::getS, SOME_ERROR_MESSAGE)
					.then(consumer);
		} catch (IllegalStateException e) {
			exception = e;
		}

		assertThat(exception, notNullValue());
		assertThat(exception.getMessage(), equalTo(SOME_ERROR_MESSAGE));
		verifyNoInteractions(consumer);
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
		assertDoesNotThrow(() -> Nullables.requireNotNull()); // NOSONAR
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
		assertInstanceOf(UnsupportedOperationException.class, targetException);
	}

	@Test
	void shouldThrowExceptionOnRequireNullForNonNullInput() {
		Nullables.requireNull(null, SOME_ERROR_MESSAGE);

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Nullables.requireNull(MUMU, SOME_ERROR_MESSAGE));
		assertThat(e.getMessage(), equalTo(SOME_ERROR_MESSAGE));
	}

	@Test
	void shouldNotThrowExceptionOnRequireNullForNullInput() {
		assertDoesNotThrow(() -> Nullables.requireNull(null, SOME_ERROR_MESSAGE));
	}

	@Test
	void shouldNotThrowExceptionInArrayContainsNullValuesOnNonNullList() {
		String[] array = new String[] { MUMU, null, BIBI };
		List<String> list = assertDoesNotThrow(() -> Nullables.nonNullList(array));

		assertThat(list, hasSize(3));
		for (int i = 0; i < array.length; ++i) {
			assertThat(array[i], equalTo(list.get(i)));
		}
	}

	@Test
	void shouldThrowExceptionIfTheListReturnedFromAnArrayIsModified() {
		List<String> list = Nullables.nonNullList(MUMU, BIBI);

		assertThrows(Exception.class, () -> list.add(CUCU));
	}

	@Test
	void shouldThrowExceptionIfTheListReturnedFromANullArrayIsModified() {
		List<String> list = Nullables.nonNullList((String[]) null);

		assertThrows(Exception.class, () -> list.add(CUCU));
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
