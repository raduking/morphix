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
package org.morphix.convert.function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SimpleConverter}.
 *
 * @author Radu Sebastian LAZIN
 */
class SimpleConverterTest {

	@Test
	void shouldConvertUsingLambda() {
		SimpleConverter<Integer, String> converter = i -> "Value: " + i;

		String result = converter.convert(10);

		assertThat(result, equalTo("Value: 10"));
	}

	@Test
	void shouldConvertUsingMethodReference() {
		SimpleConverter<Double, String> converter = String::valueOf;

		String result = converter.convert(15.5);

		assertThat(result, equalTo("15.5"));
	}

	@Test
	void shouldApplyUsingFunctionInterface() {
		SimpleConverter<Integer, String> converter = i -> "Number: " + i;

		String result = converter.apply(7);

		assertThat(result, equalTo("Number: 7"));
	}
}
