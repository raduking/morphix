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
package org.morphix.lang.leak;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test class for {@link LeakDetectionLevel}.
 *
 * @author Radu Sebastian LAZIN
 */
class LeakDetectionLevelTest {

	@AfterEach
	void cleanup() {
		System.clearProperty(LeakDetectionLevel.PROPERTY);
	}

	@Test
	void shouldDefaultToSimple() {
		assertEquals(LeakDetectionLevel.SIMPLE, LeakDetectionLevel.current());
	}

	@Test
	void shouldParseLevel() {
		System.setProperty(LeakDetectionLevel.PROPERTY, "ADVANCED");

		assertEquals(LeakDetectionLevel.ADVANCED, LeakDetectionLevel.current());
	}

	@Test
	void shouldIgnoreCase() {
		System.setProperty(LeakDetectionLevel.PROPERTY, "paranoid");

		assertEquals(LeakDetectionLevel.PARANOID, LeakDetectionLevel.current());
	}

	@ParameterizedTest
	@ValueSource(strings = { "   simple", "simple   ", "  simple  ", "banana", "  ", "" })
	void shouldReturnSimpleWhenInvalid(final String value) {
		System.setProperty(LeakDetectionLevel.PROPERTY, value);

		assertEquals(LeakDetectionLevel.SIMPLE, LeakDetectionLevel.current());
	}
}
