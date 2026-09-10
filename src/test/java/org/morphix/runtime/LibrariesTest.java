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
package org.morphix.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.TypedArgument;
import org.morphix.reflection.TypedArguments;

/**
 * Test class for {@link Libraries}.
 *
 * @author Radu Sebastian LAZIN
 */
class LibrariesTest {

	private static final String NAME = "testName";
	private static final Integer NUMBER = 666;

	static class DefaultType {
		// empty
	}

	static class FirstLibraryType extends DefaultType {
		// empty
	}

	static class SecondLibraryType extends DefaultType {
		// empty
	}

	static class DefaultParameterizedType {
		// empty
	}

	static class FirstParameterizedLibraryType extends DefaultParameterizedType {

		private final String name;
		private final Integer number;

		FirstParameterizedLibraryType(final String name, final Integer number) {
			this.name = name;
			this.number = number;
		}
	}

	static class SecondParameterizedLibraryType extends DefaultParameterizedType {

		private final String name;
		private final Integer number;

		SecondParameterizedLibraryType(final String name, final Integer number) {
			this.name = name;
			this.number = number;
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnInstanceOfFirstPresentLibrary() {
		OptionalLibrary<FirstLibraryType> first =
				OptionalLibrary.present(FirstLibraryType.class);

		OptionalLibrary<SecondLibraryType> second =
				OptionalLibrary.present(SecondLibraryType.class);

		Supplier<DefaultType> fallback = mock(Supplier.class);

		DefaultType result = Libraries.instance(fallback, first, second);

		assertThat(result, instanceOf(FirstLibraryType.class));
		verifyNoInteractions(fallback);
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldSkipLibrariesThatAreNotPresent() {
		OptionalLibrary<FirstLibraryType> notPresent =
				OptionalLibrary.notPresent(FirstLibraryType.class);

		OptionalLibrary<SecondLibraryType> present =
				OptionalLibrary.present(SecondLibraryType.class);

		Supplier<DefaultType> fallback = mock(Supplier.class);

		DefaultType result = Libraries.instance(fallback, notPresent, present);

		assertThat(result, instanceOf(SecondLibraryType.class));
		verifyNoInteractions(fallback);
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnFallbackWhenNoLibrariesArePresent() {
		OptionalLibrary<FirstLibraryType> first =
				OptionalLibrary.notPresent(FirstLibraryType.class);

		OptionalLibrary<SecondLibraryType> second =
				OptionalLibrary.notPresent(SecondLibraryType.class);

		DefaultType fallbackInstance = new DefaultType();
		Supplier<DefaultType> fallback = mock(Supplier.class);
		when(fallback.get()).thenReturn(fallbackInstance);

		DefaultType result = Libraries.instance(fallback, first, second);

		assertThat(result, sameInstance(fallbackInstance));
		verify(fallback).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnFallbackWhenNoLibrariesAreProvided() {
		DefaultType fallbackInstance = new DefaultType();
		Supplier<DefaultType> fallback = mock(Supplier.class);
		when(fallback.get()).thenReturn(fallbackInstance);

		DefaultType result = Libraries.instance(fallback);

		assertThat(result, sameInstance(fallbackInstance));
		verify(fallback).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnFallbackWhenLibrariesArrayIsNull() {
		DefaultType fallbackInstance = new DefaultType();
		Supplier<DefaultType> fallback = mock(Supplier.class);
		when(fallback.get()).thenReturn(fallbackInstance);

		DefaultType result = Libraries.instance(fallback, (OptionalLibrary<DefaultType>[]) null);

		assertThat(result, sameInstance(fallbackInstance));
		verify(fallback).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnInstanceOfFirstPresentLibraryUsingTypedArguments() {
		OptionalLibrary<FirstParameterizedLibraryType> first =
				OptionalLibrary.present(FirstParameterizedLibraryType.class);

		OptionalLibrary<SecondParameterizedLibraryType> second =
				OptionalLibrary.present(SecondParameterizedLibraryType.class);

		Supplier<DefaultParameterizedType> fallback = mock(Supplier.class);

		TypedArguments arguments = TypedArguments.of(
				TypedArgument.of(String.class, NAME),
				TypedArgument.of(Integer.class, NUMBER));

		DefaultParameterizedType result = Libraries.instance(arguments, fallback, first, second);

		assertThat(result, instanceOf(FirstParameterizedLibraryType.class));
		FirstParameterizedLibraryType library = (FirstParameterizedLibraryType) result;
		assertThat(library.name, equalTo(NAME));
		assertThat(library.number, equalTo(NUMBER));
		verifyNoInteractions(fallback);
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldSkipLibrariesThatAreNotPresentUsingTypedArguments() {
		OptionalLibrary<FirstParameterizedLibraryType> notPresent =
				OptionalLibrary.notPresent(FirstParameterizedLibraryType.class);

		OptionalLibrary<SecondParameterizedLibraryType> present =
				OptionalLibrary.present(SecondParameterizedLibraryType.class);

		Supplier<DefaultParameterizedType> fallback = mock(Supplier.class);

		TypedArguments arguments = TypedArguments.of(
				TypedArgument.of(String.class, NAME),
				TypedArgument.of(Integer.class, NUMBER));

		DefaultParameterizedType result = Libraries.instance(arguments, fallback, notPresent, present);

		assertThat(result, instanceOf(SecondParameterizedLibraryType.class));
		SecondParameterizedLibraryType library = (SecondParameterizedLibraryType) result;
		assertThat(library.name, equalTo(NAME));
		assertThat(library.number, equalTo(NUMBER));
		verifyNoInteractions(fallback);
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnFallbackWhenNoLibrariesArePresentUsingTypedArguments() {
		OptionalLibrary<FirstParameterizedLibraryType> first =
				OptionalLibrary.notPresent(FirstParameterizedLibraryType.class);

		OptionalLibrary<SecondParameterizedLibraryType> second =
				OptionalLibrary.notPresent(SecondParameterizedLibraryType.class);

		DefaultParameterizedType fallbackInstance = new DefaultParameterizedType();
		Supplier<DefaultParameterizedType> fallback = mock(Supplier.class);
		when(fallback.get()).thenReturn(fallbackInstance);

		TypedArguments arguments = TypedArguments.of(String.class, NAME);

		DefaultParameterizedType result = Libraries.instance(arguments, fallback, first, second);

		assertThat(result, sameInstance(fallbackInstance));
		verify(fallback).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnFallbackWhenNoLibrariesAreProvidedUsingTypedArguments() {
		DefaultParameterizedType fallbackInstance = new DefaultParameterizedType();
		Supplier<DefaultParameterizedType> fallback = mock(Supplier.class);
		when(fallback.get()).thenReturn(fallbackInstance);

		TypedArguments arguments = TypedArguments.of(String.class, NAME);

		DefaultParameterizedType result = Libraries.instance(arguments, fallback);

		assertThat(result, sameInstance(fallbackInstance));
		verify(fallback).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnFallbackWhenLibrariesArrayIsNullUsingTypedArguments() {
		DefaultParameterizedType fallbackInstance = new DefaultParameterizedType();
		Supplier<DefaultParameterizedType> fallback = mock(Supplier.class);
		when(fallback.get()).thenReturn(fallbackInstance);

		TypedArguments arguments = TypedArguments.of(String.class, NAME);

		DefaultParameterizedType result = Libraries.instance(arguments, fallback,
				(OptionalLibrary<DefaultParameterizedType>[]) null);

		assertThat(result, sameInstance(fallbackInstance));
		verify(fallback).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldThrowWhenTypedArgumentsIsNull() {
		Supplier<DefaultParameterizedType> fallback = mock(Supplier.class);

		NullPointerException nullPointerException = assertThrows(
				NullPointerException.class,
				() -> Libraries.instance((TypedArguments) null, fallback));

		assertThat(nullPointerException.getMessage(), equalTo("arguments must not be null"));
	}

	@Test
	void shouldThrowWhenFallbackSupplierIsNullForTypedArguments() {
		TypedArguments arguments = TypedArguments.of(String.class, NAME);

		NullPointerException nullPointerException = assertThrows(
				NullPointerException.class,
				() -> Libraries.instance(arguments, (Supplier<DefaultParameterizedType>) null));

		assertThat(nullPointerException.getMessage(), equalTo("fallbackSupplier must not be null"));
	}
}
