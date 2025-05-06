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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link Nullables} {@code whenNotNull} methods.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class NullablesWhenNotNullTest {

	private static final String RESULT = "result";
	private static final String NOT_NULL = "notNull";

	@Mock
	private Supplier<String> mockSupplier;

	@Mock
	private Runnable mockRunnable;

	@Mock
	private Consumer<String> mockConsumer;

	@Test
	void shouldExecuteSupplierAndReturnValueWhenObjectIsNotNull() {
		String obj = NOT_NULL;
		when(mockSupplier.get()).thenReturn(RESULT);

		String result = Nullables.whenNotNull(obj, mockSupplier);

		assertEquals(RESULT, result);
		verify(mockSupplier).get();
	}

	@Test
	void shouldNotExecuteSupplierAndReturnNullWhenObjectIsNull() {
		String obj = null;

		String result = Nullables.whenNotNull(obj, mockSupplier);

		assertNull(result);
		verifyNoInteractions(mockSupplier);
	}

	@Test
	void shouldExecuteRunnableWhenObjectIsNotNull() {
		String obj = NOT_NULL;

		Nullables.whenNotNull(obj, mockRunnable);

		verify(mockRunnable).run();
	}

	@Test
	void shouldNotExecuteRunnableWhenObjectIsNull() {
		String obj = null;

		Nullables.whenNotNull(obj, mockRunnable);

		verifyNoInteractions(mockRunnable);
	}

	@Test
	void shouldExecuteConsumerWithObjectWhenObjectIsNotNull() {
		String obj = NOT_NULL;

		Nullables.whenNotNull(obj, mockConsumer);

		verify(mockConsumer).accept(obj);
	}

	@Test
	void shouldNotExecuteConsumerWhenObjectIsNull() {
		String obj = null;

		Nullables.whenNotNull(obj, mockConsumer);

		verifyNoInteractions(mockConsumer);
	}
}
