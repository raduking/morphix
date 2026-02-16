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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.morphix.lang.function.ValueFunction.from;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ValueFunction}.
 *
 * @author Radu Sebastian LAZIN
 */
class ValueFunctionTest {

	private static final String MESSAGE = "Message";
	private static final int SOME_INT = 666;
	private static final long SOME_LONG = 667L;

	@Test
	void shouldCreateAFieldValueFunction() throws Exception {
		Long expected = SOME_LONG;

		Long result = from(expected).value();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldBehaveAsASupplier() {
		Integer expected = SOME_INT;

		Integer result = from(expected).get();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldThrowExceptionOnSupplierGetIfTheValueFunctionThrows() {
		ValueFunction<String> vf = () -> {
			throw new UnsupportedOperationException(MESSAGE);
		};

		IllegalStateException e = assertThrows(IllegalStateException.class, vf::get);

		assertThat(e.getMessage(), equalTo("Error returning value"));
		assertThat(e.getCause().getClass(), equalTo(UnsupportedOperationException.class));
		assertThat(e.getCause().getMessage(), equalTo(MESSAGE));
	}
}
