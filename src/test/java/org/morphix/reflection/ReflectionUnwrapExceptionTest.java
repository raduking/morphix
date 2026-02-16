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
package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Reflection#unwrapException(Throwable)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionUnwrapExceptionTest {

	private static final String TEST_MESSAGE = "Test";

	@Test
	void shouldUnwrapInvocationTargetException() {
		RuntimeException expected = new RuntimeException(TEST_MESSAGE);
		InvocationTargetException e = new InvocationTargetException(expected);

		Throwable result = Reflection.unwrapException(e);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTheGivenInvocationTargetExceptionWhenCauseIsNull() {
		InvocationTargetException expected = new InvocationTargetException(null);

		Throwable result = Reflection.unwrapException(expected);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTheGivenInvocationTargetExceptionWhenCauseIsTheSame() {
		Throwable expected = new Throwable();

		Throwable result = Reflection.unwrapException(expected);

		assertThat(result, equalTo(expected));
	}

	public static class TestThrowable extends Throwable {
		@Serial
		private static final long serialVersionUID = 8780308628483444613L;

		private final Throwable cause = this;

		@Override
		public synchronized Throwable getCause() {
			return cause;
		}
	}

	@Test
	void shouldReturnTheGivenInvocationTargetExceptionWhenCauseIsTheSameAsThis() {
		TestThrowable expected = new TestThrowable();

		Throwable result = Reflection.unwrapException(expected);

		assertThat(result, equalTo(expected));
	}

}
