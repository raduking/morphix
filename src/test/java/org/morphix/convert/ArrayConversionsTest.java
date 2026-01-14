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
package org.morphix.convert;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ArrayConversions}.
 *
 * @author Radu Sebastian LAZIN
 */
class ArrayConversionsTest {

	@Test
	void shouldConvertArrayToAnotherArrayWIthIntFunction() {
		Integer[] source = new Integer[] { 1, 2, 3, 4, 5 };

		String[] result = ArrayConversions.convertArray(source, String::valueOf).toArray(String[]::new);

		assertThat(result.length, equalTo(source.length));
		for (int i = 0; i < source.length; i++) {
			assertThat(result[i], equalTo(String.valueOf(source[i])));
		}
	}

	@Test
	void shouldConvertArrayToAnotherArrayWithInstanceFunction() {
		Integer[] source = new Integer[] { 1, 2, 3, 4, 5 };

		String[] result = ArrayConversions.convertArray(source, String::valueOf).toArray(() -> new String[source.length]);

		assertThat(result.length, equalTo(source.length));
		for (int i = 0; i < source.length; i++) {
			assertThat(result[i], equalTo(String.valueOf(source[i])));
		}
	}

	@Test
	void shouldConvertArrayToAnotherArrayWithGivenArraySmaller() {
		Integer[] source = new Integer[] { 1, 2, 3, 4, 5 };

		String[] result = ArrayConversions.convertArray(source, String::valueOf).to(new String[0]);

		assertThat(result.length, equalTo(source.length));
		for (int i = 0; i < source.length; i++) {
			assertThat(result[i], equalTo(String.valueOf(source[i])));
		}
	}
}
