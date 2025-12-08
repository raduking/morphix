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
package org.morphix.convert.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.morphix.convert.Conversions;
import org.morphix.reflection.InstanceCreator;

/**
 * Conversion should work with classes without default constructors.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToAnyWithoutDefaultConstructorTest {

	private static final Integer TEST_INTEGER = 11;
	private static final String TEST_STRING = TEST_INTEGER.toString();

	public static class A {
		String x;
	}

	public static class B {
		Integer x;

		public B(final Integer x) {
			this.x = x;
		}
	}

	public static class Src {
		A a;
	}

	public static class Dst {
		B a;
	}

	@BeforeEach
	void setUp() {
		InstanceCreator instanceCreator = InstanceCreator.getInstance();
		assumeTrue(instanceCreator.isUsable());
	}

	@Test
	void shouldConvertObjects() {
		Src src = new Src();
		A a = new A();
		a.x = TEST_STRING;
		src.a = a;

		Dst dst = Conversions.convertFrom(src, Dst::new);

		assertThat(dst.a.x, equalTo(TEST_INTEGER));
	}

}
