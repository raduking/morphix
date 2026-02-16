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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link Runnables}.
 *
 * @author Radu Sebastian LAZIN
 */
class RunnablesTest {

	private static final String TEST_STRING = "testString";

	@Test
	void shouldComposeARunnableWithASupplierAndRunThemSequentially() {
		List<Integer> list = new ArrayList<>();

		Runnable runnable = () -> {
			list.add(1);
		};
		Supplier<String> supplier = () -> {
			list.add(2);
			return TEST_STRING;
		};

		String result = Runnables.compose(runnable, supplier).get();

		assertThat(result, equalTo(TEST_STRING));
		assertThat(list, equalTo(List.of(1, 2)));
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateClass() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(Runnables.class);

		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
