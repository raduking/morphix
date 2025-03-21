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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link ThrowingRunnable}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class ThrowingRunnableTest {

	private static final String RUNTIME_EXCEPTION = "Runtime exception";
	private static final String TEST_ERROR = "Test error";
	private static final String CHECKED_EXCEPTION = "Checked exception";

	@Mock
	private ThrowingRunnable mockThrowingRunnable;

	@Test
	void shouldRunThrowingRunnableWithoutException() throws Throwable {
		doNothing().when(mockThrowingRunnable).run();

		Runnable runnable = ThrowingRunnable.unchecked(mockThrowingRunnable);
		runnable.run();

		verify(mockThrowingRunnable).run();
	}

	@Test
	void shouldRethrowCheckedExceptionAsUncheckedException() throws Throwable {
		Exception checkedException = new Exception(CHECKED_EXCEPTION);
		doThrow(checkedException).when(mockThrowingRunnable).run();

		Runnable runnable = ThrowingRunnable.unchecked(mockThrowingRunnable);

		Exception exception = assertThrows(Exception.class, runnable::run);
		assertThat(exception.getMessage(), is(CHECKED_EXCEPTION));
		verify(mockThrowingRunnable).run();
	}

	@Test
	void shouldRethrowRuntimeExceptionAsIs() throws Throwable {
		RuntimeException runtimeException = new RuntimeException(RUNTIME_EXCEPTION);
		doThrow(runtimeException).when(mockThrowingRunnable).run();

		Runnable runnable = ThrowingRunnable.unchecked(mockThrowingRunnable);

		RuntimeException exception = assertThrows(RuntimeException.class, runnable::run);
		assertThat(exception.getMessage(), is(RUNTIME_EXCEPTION));
		verify(mockThrowingRunnable).run();
	}

	@Test
	void shouldRethrowErrorAsIs() throws Throwable {
		Error error = new Error(TEST_ERROR);
		doThrow(error).when(mockThrowingRunnable).run();

		Runnable runnable = ThrowingRunnable.unchecked(mockThrowingRunnable);

		Error thrownError = assertThrows(Error.class, runnable::run);
		assertThat(thrownError.getMessage(), is(TEST_ERROR));
		verify(mockThrowingRunnable).run();
	}
}
