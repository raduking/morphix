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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.morphix.convert.Conversions.convertFrom;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.ReflectionException;

/**
 * Tests calling of getter methods on fields if they are available in source.
 *
 * @author Radu Sebastian LAZIN
 */
class GetterMethodsTest {

	private static final Long TEST_LONG_1 = 1234321L;
	private static final Long TEST_LONG_2 = 4321234L;

	private static final String TEST_STRING_LONG_2 = "4321234";
	private static final String TEST_EXCEPTION_MESSAGE = "This is a test exception message.";

	static class Src {
		Long id;

		public Long getId() {
			return TEST_LONG_2;
		}
	}

	static class Dst {
		String id;
	}

	@Test
	void shouldCallGetterMethodIfAvailable() {
		Src src = new Src();
		src.id = TEST_LONG_1;

		Dst dst = convertFrom(src, Dst::new);

		assertThat(dst.id, equalTo(TEST_STRING_LONG_2));
	}

	static class SrcEx {
		Long id;

		public Long getId() throws NoSuchMethodException {
			throw new NoSuchMethodException(TEST_EXCEPTION_MESSAGE);
		}
	}

	@Test
	void shouldEscalateExceptionIfGetterThrows() {
		SrcEx src = new SrcEx();

		assertThrows(ReflectionException.class, () -> convertFrom(src, Dst::new));
	}

	static class SrcDerived extends Src {
		// empty
	}

	@Test
	void shouldCallGetterFromSuperclass() {
		SrcDerived src = new SrcDerived();
		src.id = TEST_LONG_1;

		Dst dst = convertFrom(src, Dst::new);

		assertThat(dst.id, equalTo(TEST_STRING_LONG_2));
	}

}
