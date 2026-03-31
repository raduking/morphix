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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Libraries}.
 *
 * @author Radu Sebastian LAZIN
 */
class LibrariesTest {

	static class DefaultType {
		// empty
	}

	static class FirstLibraryType extends DefaultType {
		// empty
	}

	static class SecondLibraryType extends DefaultType {
		// empty
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
}
