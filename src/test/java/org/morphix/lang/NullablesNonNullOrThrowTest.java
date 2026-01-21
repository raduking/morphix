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
package org.morphix.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link Nullables#nonNullOrThrow(Object, Supplier)}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class NullablesNonNullOrThrowTest {

	private static final String ACTUAL_VALUE = "actualValue";
	private static final String EXCEPTION_MESSAGE = "default exception";

	@Mock
	private Supplier<Exception> mockThrowableSupplier;

	@Test
	void shouldReturnValueWhenValueIsNotNull() {
		String value = ACTUAL_VALUE;

		String result = Nullables.nonNullOrThrow(value, mockThrowableSupplier);

		assertEquals(ACTUAL_VALUE, result);
		verifyNoInteractions(mockThrowableSupplier);
	}

	@Test
	void shouldReturnDefaultValueFromSupplierWhenValueIsNull() {
		String value = null;
		when(mockThrowableSupplier.get()).thenReturn(new Exception(EXCEPTION_MESSAGE));

		Exception exception = assertThrows(Exception.class, () -> Nullables.nonNullOrThrow(value, mockThrowableSupplier));

		assertEquals(EXCEPTION_MESSAGE, exception.getMessage());
		verify(mockThrowableSupplier).get();
	}
}
