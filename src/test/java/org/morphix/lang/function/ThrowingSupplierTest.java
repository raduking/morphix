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
package org.morphix.lang.function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link ThrowingSupplier}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class ThrowingSupplierTest {

	@Mock
	private ThrowingSupplier<String> mockThrowingSupplier;

	@Test
	void shouldReturnValueWhenThrowingSupplierDoesNotThrowException() throws Throwable {
		when(mockThrowingSupplier.get()).thenReturn("success");

		Supplier<String> supplier = ThrowingSupplier.unchecked(mockThrowingSupplier);
		String result = supplier.get();

		assertThat(result, is("success"));
		verify(mockThrowingSupplier).get();
	}

	@Test
	void shouldRethrowCheckedExceptionAsUncheckedException() throws Throwable {
		Exception checkedException = new Exception("Checked exception");
		when(mockThrowingSupplier.get()).thenThrow(checkedException);

		Supplier<String> supplier = ThrowingSupplier.unchecked(mockThrowingSupplier);

		Exception exception = assertThrows(Exception.class, supplier::get);
		assertThat(exception.getMessage(), is("Checked exception"));
		verify(mockThrowingSupplier).get();
	}

	@Test
	void shouldRethrowRuntimeExceptionAsIs() throws Throwable {
		RuntimeException runtimeException = new RuntimeException("Runtime exception");
		when(mockThrowingSupplier.get()).thenThrow(runtimeException);

		Supplier<String> supplier = ThrowingSupplier.unchecked(mockThrowingSupplier);

		RuntimeException exception = assertThrows(RuntimeException.class, supplier::get);
		assertThat(exception.getMessage(), is("Runtime exception"));
		verify(mockThrowingSupplier).get();
	}

	@Test
	void shouldRethrowErrorAsIs() throws Throwable {
		Error error = new Error("Test error");
		when(mockThrowingSupplier.get()).thenThrow(error);

		Supplier<String> supplier = ThrowingSupplier.unchecked(mockThrowingSupplier);

		Error thrownError = assertThrows(Error.class, supplier::get);
		assertThat(thrownError.getMessage(), is("Test error"));
		verify(mockThrowingSupplier).get();
	}
}
