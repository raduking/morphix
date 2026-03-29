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
package org.morphix.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test class for {@link Temporals}.
 *
 * @author Radu Sebastian LAZIN
 */
class TemporalsTest {

	@Test
	void shouldFormatADoubleToStringAsSecondsWith3DecimalsRoundingDown() {
		double value = 1.2341;

		String result = Temporals.formatToSeconds(value);

		assertThat(result, equalTo("1.234s"));
	}

	@Test
	void shouldFormatADoubleToStringAsSecondsWith3DecimalsRoundingUp() {
		double value = 1.2346;

		String result = Temporals.formatToSeconds(value);

		assertThat(result, equalTo("1.235s"));
	}

	@Test
	void shouldReturnNotAvailableIfInputIsNaNOnFormatToSecondsWithDouble() {
		String result = Temporals.formatToSeconds(Double.NaN);

		assertThat(result, equalTo("N/A"));
	}

	@Nested
	class ParseSimpleDurationTest {

		@Test
		void shouldParseSimpleDurationSeconds() {
			String duration = "1s";

			Duration result = Temporals.parseSimpleDuration(duration);

			assertThat(result, equalTo(Duration.ofSeconds(1)));
		}

		@Test
		void shouldParseSimpleDurationMinutes() {
			String duration = "1m";

			Duration result = Temporals.parseSimpleDuration(duration);

			assertThat(result, equalTo(Duration.ofMinutes(1)));
		}

		@Test
		void shouldParseSimpleDurationHours() {
			String duration = "1h";

			Duration result = Temporals.parseSimpleDuration(duration);

			assertThat(result, equalTo(Duration.ofHours(1)));
		}

		@Test
		void shouldParseSimpleDurationDays() {
			String duration = "1d";

			Duration result = Temporals.parseSimpleDuration(duration);

			assertThat(result, equalTo(Duration.ofDays(1)));
		}

		@Test
		void shouldThrowExceptionIfUnsupportedFormat() {
			String duration = "1x";

			IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Temporals.parseSimpleDuration(duration));

			assertThat(e.getMessage(), equalTo("Unsupported unit: x in: " + duration));
		}

		@ParameterizedTest
		@ValueSource(strings = { "", " ", "\t", "\n" })
		void shouldThrowExceptionIfInputIsBlank(final String duration) {
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Temporals.parseSimpleDuration(duration));

			assertThat(e.getMessage(), equalTo("Input cannot be empty or null"));
		}
	}
}
