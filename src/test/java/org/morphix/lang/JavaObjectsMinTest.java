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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JavaObjects#max(Comparable, Comparable)}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaObjectsMinTest {

	@Test
	void shouldReturnMinimum() {
		Integer x = 10;
		Integer y = 20;

		Integer result = JavaObjects.min(x, y);

		assertThat(result, equalTo(x));
	}

	@Test
	void shouldReturnMinimumSecondWhenFirstParamIsNull() {
		Integer y = 20;

		Integer result = JavaObjects.min(null, y);

		assertThat(result, equalTo(y));
	}

	@Test
	void shouldReturnMinimumFirstWhenSecondParamIsNull() {
		Integer x = 10;

		Integer result = JavaObjects.min(x, null);

		assertThat(result, equalTo(x));
	}

}
