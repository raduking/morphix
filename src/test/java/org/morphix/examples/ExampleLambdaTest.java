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
package org.morphix.examples;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.Conversions.convertFrom;

import org.junit.jupiter.api.Test;

/**
 * Converter example test using a lambda for extra conversions where the fields don't match.
 *
 * @author Radu Sebastian LAZIN
 */
class ExampleLambdaTest {

	public static class A {
		int x;
		int y;
	}

	public static class B {
		String x;
		String z;
	}

	@Test
	void example() {
		A src = new A();
		src.x = 17;
		src.y = 13;

		B dst = convertFrom(src, B::new, (s, d) -> {
			d.z = String.valueOf(s.y);
		});

		assertThat(dst.x, equalTo("17"));
		assertThat(dst.z, equalTo("13"));
	}
}
