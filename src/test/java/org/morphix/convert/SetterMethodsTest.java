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

/**
 * Tests calling of setter methods on fields if they are available in destination.
 *
 * @author Radu Sebastian LAZIN
 */
class SetterMethodsTest {

	private static final Long TEST_LONG_1 = 1234321L;

	private static final String TEST_STRING_LONG_2 = "4321234";
	private static final String TEST_EXCEPTION_MESSAGE = "This is a test exception message.";

	public static class Source {
		Long id;
	}

	public static class Destination {
		String id;

		@SuppressWarnings("unused")
		public void setId(final String id) {
			this.id = TEST_STRING_LONG_2;
		}
	}

	@Test
	void shouldCallSetterMethodIfAvailable() {
		Source src = new Source();
		src.id = TEST_LONG_1;

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(TEST_STRING_LONG_2));
	}

	public static class DestinationEx {
		String id;

		@SuppressWarnings("unused")
		public void setId(final String id) throws NoSuchMethodException {
			throw new NoSuchMethodException(TEST_EXCEPTION_MESSAGE);
		}
	}

	@Test
	void shouldEscalateExceptionIfSetterThrows() {
		Source src = new Source();
		src.id = TEST_LONG_1;

		assertThrows(ObjectConverterException.class, () -> convertFrom(src, DestinationEx::new));
	}

	public static class DestinationDerived extends Destination {
		// empty
	}

	@Test
	void shouldCallSetterFromSuperclass() {
		Source src = new Source();
		src.id = TEST_LONG_1;

		DestinationDerived dst = convertFrom(src, DestinationDerived::new);

		assertThat(dst.id, equalTo(TEST_STRING_LONG_2));
	}

}
