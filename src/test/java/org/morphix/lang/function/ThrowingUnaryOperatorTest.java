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
package org.morphix.lang.function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link ThrowingUnaryOperator}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class ThrowingUnaryOperatorTest {

	private static final String FORTY_TWO = "42";
	private static final String INPUT = "input";
	private static final String CHECKED_EXCEPTION = "Checked exception";
	private static final String RUNTIME_EXCEPTION = "Runtime exception";
	private static final String TEST_ERROR = "Test error";

	@Mock
	private ThrowingUnaryOperator<String> mockThrowingUnaryOperator;

	@Test
	void shouldReturnValueWhenThrowingFunctionDoesNotThrowException() throws Throwable {
		when(mockThrowingUnaryOperator.apply(INPUT)).thenReturn(FORTY_TWO);

		UnaryOperator<String> function = ThrowingUnaryOperator.unchecked(mockThrowingUnaryOperator);
		String result = function.apply(INPUT);

		assertThat(result, is(FORTY_TWO));
		verify(mockThrowingUnaryOperator).apply(INPUT);
	}

	@Test
	void shouldRethrowCheckedExceptionAsUncheckedException() throws Throwable {
		Exception checkedException = new Exception(CHECKED_EXCEPTION);
		when(mockThrowingUnaryOperator.apply(INPUT)).thenThrow(checkedException);

		UnaryOperator<String> function = ThrowingUnaryOperator.unchecked(mockThrowingUnaryOperator);

		Exception exception = assertThrows(Exception.class, () -> function.apply(INPUT));
		assertThat(exception.getMessage(), is(CHECKED_EXCEPTION));
		verify(mockThrowingUnaryOperator).apply(INPUT);
	}

	@Test
	void shouldRethrowRuntimeExceptionAsIs() throws Throwable {
		RuntimeException runtimeException = new RuntimeException(RUNTIME_EXCEPTION);
		when(mockThrowingUnaryOperator.apply(INPUT)).thenThrow(runtimeException);

		UnaryOperator<String> function = ThrowingUnaryOperator.unchecked(mockThrowingUnaryOperator);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> function.apply(INPUT));
		assertThat(exception.getMessage(), is(RUNTIME_EXCEPTION));
		verify(mockThrowingUnaryOperator).apply(INPUT);
	}

	@Test
	void shouldRethrowErrorAsIs() throws Throwable {
		Error error = new Error(TEST_ERROR);
		when(mockThrowingUnaryOperator.apply(INPUT)).thenThrow(error);

		UnaryOperator<String> function = ThrowingUnaryOperator.unchecked(mockThrowingUnaryOperator);

		Error thrownError = assertThrows(Error.class, () -> function.apply(INPUT));
		assertThat(thrownError.getMessage(), is(TEST_ERROR));
		verify(mockThrowingUnaryOperator).apply(INPUT);
	}
}
