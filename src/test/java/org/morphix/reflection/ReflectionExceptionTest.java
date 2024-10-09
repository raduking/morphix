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
package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ReflectionException}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionExceptionTest {

	private static final String TEST_MESSAGE = "testMessage";

	@Test
	void shouldSetTheMessage() {
		ReflectionException e = new ReflectionException(TEST_MESSAGE);

		assertThat(e.getMessage(), equalTo(TEST_MESSAGE));
	}

	@Test
	void shouldSetTheCauseAndMessage() {
		Throwable cause = new Exception();
		ReflectionException e = new ReflectionException(TEST_MESSAGE, cause);

		assertThat(e.getCause(), equalTo(cause));
		assertThat(e.getMessage(), equalTo(TEST_MESSAGE));
	}

	@Test
	void shouldSetTheCause() {
		Throwable cause = new Exception();
		ReflectionException e = new ReflectionException(cause);

		assertThat(e.getCause(), equalTo(cause));
	}

	@Test
	void shouldWrapExceptionIfIsRunnableThrows() {
		Runnable runnable = mock(Runnable.class);
		doThrow(new RuntimeException()).when(runnable).run();

		ReflectionException e = assertThrows(ReflectionException.class, () -> ReflectionException.wrapThrowing(runnable, TEST_MESSAGE));
		assertThat(e.getMessage(), equalTo(TEST_MESSAGE));
	}

	@Test
	void shouldWrapExceptionIfIsSupplierThrows() {
		Supplier<?> supplier = mock(Supplier.class);
		doThrow(new RuntimeException()).when(supplier).get();

		ReflectionException e = assertThrows(ReflectionException.class, () -> ReflectionException.wrapThrowing(supplier, TEST_MESSAGE));
		assertThat(e.getMessage(), equalTo(TEST_MESSAGE));
	}

}
