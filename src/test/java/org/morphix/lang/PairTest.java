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
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Pair}.
 *
 * @author Radu Sebastian LAZIN
 */
class PairTest {

	private static final String SOME_STRING = "someString";
	private static final Integer SOME_INTEGER = Integer.MAX_VALUE;

	@Test
	void shouldBuildAPairWithBothValues() {
		Pair<String, Integer> pair = Pair.of(SOME_STRING, SOME_INTEGER);

		assertThat(pair.left(), equalTo(SOME_STRING));
		assertThat(pair.right(), equalTo(SOME_INTEGER));
	}

	@Test
	void shouldConvertAPairToMap() {
		Pair<String, Integer> pair = Pair.of(SOME_STRING, SOME_INTEGER);

		Map<String, Integer> map = pair.toMap();

		assertThat(map.entrySet(), hasSize(1));
		assertThat(map.get(SOME_STRING), equalTo(SOME_INTEGER));
	}

	@Test
	void shouldConvertAPairToMapEntry() {
		Pair<String, Integer> pair = Pair.of(SOME_STRING, SOME_INTEGER);

		Map.Entry<String, Integer> entry = pair.toEntry();

		assertThat(entry.getKey(), equalTo(SOME_STRING));
		assertThat(entry.getValue(), equalTo(SOME_INTEGER));
	}
}
