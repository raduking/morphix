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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.lang.function.SetterFunction;

/**
 * Test class for {@link Nullables} {@code set} methods.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class NullablesSetMethodsTest {

	@Mock
	private SetterFunction<String> mockSetterFunction;

	@Mock
	private Consumer<String> mockConsumer;

	@Mock
	private Supplier<String> mockDefaultValueSupplier;

	@Test
	void shouldCallSetterFunctionWithValueWhenValueIsNotNull() {
		String value = "testValue";

		Nullables.set(value, mockSetterFunction);

		verify(mockSetterFunction).set(value);
	}

	@Test
	void shouldNotCallSetterFunctionWhenValueIsNull() {
		String value = null;

		Nullables.set(value, mockSetterFunction);

		verify(mockSetterFunction, never()).set(any());
	}

	@Test
	void shouldCallConsumerWithValueWhenValueIsNotNull() {
		String value = "testValue";

		Nullables.set(value, mockConsumer, "defaultValue");

		verify(mockConsumer).accept(value);
	}

	@Test
	void shouldCallConsumerWithDefaultValueWhenValueIsNull() {
		String value = null;
		when(mockDefaultValueSupplier.get()).thenReturn("defaultValue");

		Nullables.set(value, mockConsumer, mockDefaultValueSupplier);

		verify(mockConsumer).accept("defaultValue");
	}

	@Test
	void shouldCallConsumerWithDefaultValueWhenValueIsNull_DirectValueVersion() {
		String value = null;

		Nullables.set(value, mockConsumer, "defaultValue");

		verify(mockConsumer).accept("defaultValue");
	}

	@Test
	void shouldNotCallConsumerWhenValueIsNullAndDefaultValueIsNull() {
		String value = null;

		Nullables.set(value, mockConsumer, (String) null);

		verify(mockConsumer).accept(null);
	}

	@Test
	void shouldCallConsumerWithNullWhenValueIsNullAndDefaultValueSupplierReturnsNull() {
		String value = null;
		when(mockDefaultValueSupplier.get()).thenReturn(null);

		Nullables.set(value, mockConsumer, mockDefaultValueSupplier);

		verify(mockConsumer).accept(null);
	}
}
