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
package org.morphix.lang.accumulator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link AccumulatorException}.
 *
 * @author Radu Sebastian LAZIN
 */
class AccumulatorExceptionTest {

	private static final String SOME_EXCEPTION_MESSAGE = "Some exception message";

	@Test
	void shouldAddAllFieldsWhenBuildingException() {
		Accumulator<Exception> accumulator = ExceptionsAccumulator.of(Set.of(RuntimeException.class, IllegalArgumentException.class));

		Throwable cause = new IllegalStateException();
		AccumulatorException e = new AccumulatorException(SOME_EXCEPTION_MESSAGE, cause, accumulator);

		assertThat(e.getMessage(), equalTo(SOME_EXCEPTION_MESSAGE));
		assertThat(e.getCause(), equalTo(cause));
		assertThat(e.getAccumulator(), equalTo(accumulator));
	}

	@Test
	void shouldBuildToStringWithAccumulatorValue() {
		Accumulator<Exception> accumulator = ExceptionsAccumulator.of(Set.of(RuntimeException.class, IllegalArgumentException.class));
		RuntimeException e1 = new RuntimeException();
		accumulator.accumulate(() -> {
			throw e1;
		});
		IllegalArgumentException e2 = new IllegalArgumentException();
		accumulator.accumulate(() -> {
			throw e2;
		});

		Throwable cause = new IllegalStateException();
		AccumulatorException e = new AccumulatorException(SOME_EXCEPTION_MESSAGE, cause, accumulator);

		String expected = AccumulatorException.class.getName() + ": " + SOME_EXCEPTION_MESSAGE + " Accumulated exceptions: " + List.of(e1, e2);

		String result = e.toString();

		assertThat(result, equalTo(expected));
	}
}
