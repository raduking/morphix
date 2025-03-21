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

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link Nullables#nonNullOrDefault(Object, Supplier)} and
 * {@link Nullables#nonNullOrDefault(Object, Object)}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class NullablesNonNullOrDefaultTest {

	private static final String ACTUAL_VALUE = "actualValue";
	private static final String DEFAULT_VALUE = "defaultValue";

	@Mock
	private Supplier<String> mockDefaultValueSupplier;

	@Test
	void shouldReturnValueWhenValueIsNotNull_SupplierVersion() {
		String value = ACTUAL_VALUE;

		String result = Nullables.nonNullOrDefault(value, mockDefaultValueSupplier);

		assertEquals(ACTUAL_VALUE, result);
		verifyNoInteractions(mockDefaultValueSupplier);
	}

	@Test
	void shouldReturnDefaultValueFromSupplierWhenValueIsNull_SupplierVersion() {
		String value = null;
		when(mockDefaultValueSupplier.get()).thenReturn(DEFAULT_VALUE);

		String result = Nullables.nonNullOrDefault(value, mockDefaultValueSupplier);

		assertEquals(DEFAULT_VALUE, result);
		verify(mockDefaultValueSupplier).get();
	}

	@Test
	void shouldReturnValueWhenValueIsNotNull_DirectValueVersion() {
		String value = ACTUAL_VALUE;

		String result = Nullables.nonNullOrDefault(value, DEFAULT_VALUE);

		assertEquals(ACTUAL_VALUE, result);
	}

	@Test
	void shouldReturnDefaultValueWhenValueIsNull_DirectValueVersion() {
		String value = null;

		String result = Nullables.nonNullOrDefault(value, DEFAULT_VALUE);

		assertEquals(DEFAULT_VALUE, result);
	}

	@Test
	void shouldReturnNullWhenValueIsNullAndDefaultValueIsNull_DirectValueVersion() {
		String value = null;

		String result = Nullables.nonNullOrDefault(value, (String) null);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenValueIsNullAndDefaultSupplierReturnsNull_SupplierVersion() {
		String value = null;
		when(mockDefaultValueSupplier.get()).thenReturn(null);

		String result = Nullables.nonNullOrDefault(value, mockDefaultValueSupplier);

		assertNull(result);
		verify(mockDefaultValueSupplier).get();
	}
}
