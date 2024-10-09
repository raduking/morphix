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
package org.morphix.lang.function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link MethodCaller}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodCallerTest {

	private static final String TEST_STRING = "testString";

	@Test
	void shouldCallSetterIfArgumentIsNotNull() {
		A a = new A(null);

		MethodCaller.nonNullCall(a::setS, TEST_STRING);

		assertThat(a.s, equalTo(TEST_STRING));
	}

	@Test
	void shouldNotCallSetterIfArgumentIsNull() {
		A a = new A(TEST_STRING);

		MethodCaller.nonNullCall(a::setS, null);

		assertThat(a.s, equalTo(TEST_STRING));
	}

	public static class A {

		String s;

		public A(final String s) {
			this.s = s;
		}

		public void setS(final String s) {
			this.s = s;
		}
	}

}
