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

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ToStringFunction}.
 *
 * @author Radu Sebastian LAZIN
 */
class ToStringFunctionTest {

	@Test
	void shouldConvertToStringUsingIdentity() {
		ToStringFunction<Object> toStringFunction = ToStringFunction.identity();

		Object obj = new Object();
		String result = toStringFunction.toString(obj);

		assertThat(result, equalTo(obj.toString()));
	}

	@Test
	void shouldConvertToStringUsingToLowerCase() {
		ToStringFunction<Object> toStringFunction = ToStringFunction.toLowerCase();

		Object obj = new Object();
		String result = toStringFunction.toString(obj);

		assertThat(result, equalTo(obj.toString().toLowerCase()));
	}

	@Test
	void shouldConvertToStringUsingToUpperCase() {
		ToStringFunction<Object> toStringFunction = ToStringFunction.toUpperCase();

		Object obj = new Object();
		String result = toStringFunction.toString(obj);

		assertThat(result, equalTo(obj.toString().toUpperCase()));
	}

	@Test
	void shouldConvertToStringUsingCustomFunction() {
		ToStringFunction<Object> toStringFunction = object -> "Custom: " + object.toString();

		Object obj = new Object();
		String result = toStringFunction.toString(obj);
		String resultFromApply = toStringFunction.apply(obj);

		assertThat(result, equalTo("Custom: " + obj.toString()));
		assertThat(resultFromApply, equalTo("Custom: " + obj.toString()));
	}
}
