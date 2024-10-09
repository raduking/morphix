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
package org.morphix.convert;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.Conversions.convertFrom;

import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;

/**
 * Test class for converter with types.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterTypesTest {

	public static class A {
		String b;
	}

	public static class B {
		int b;
	}

	@Test
	void shouldConvertFromType() {
		A a = new A();
		a.b = "13";

		Type type = B.class;

		B b = convertFrom(a, type);

		assertThat(b.b, equalTo(13));
	}

}
