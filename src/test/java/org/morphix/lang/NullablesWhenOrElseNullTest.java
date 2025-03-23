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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link Nullables#whenOrElseNull(boolean, Supplier)}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class NullablesWhenOrElseNullTest {

	@Mock
	private Supplier<String> mockSupplier;

	@Test
	void shouldReturnSupplierValueWhenConditionIsTrue() {
		when(mockSupplier.get()).thenReturn("value");

		String result = Nullables.whenOrElseNull(true, mockSupplier);

		assertThat(result, is("value"));
		verify(mockSupplier).get();
	}

	@Test
	void shouldReturnNullWhenConditionIsFalse() {
		String result = Nullables.whenOrElseNull(false, mockSupplier);

		assertThat(result, is(nullValue()));
		verifyNoInteractions(mockSupplier);
	}

	@Test
	void shouldNotCallSupplierWhenConditionIsFalse() {
		Nullables.whenOrElseNull(false, mockSupplier);

		verifyNoInteractions(mockSupplier);
	}

	@Test
	void shouldCallSupplierWhenConditionIsTrue() {
		when(mockSupplier.get()).thenReturn("value");

		Nullables.whenOrElseNull(true, mockSupplier);

		verify(mockSupplier).get();
	}
}
