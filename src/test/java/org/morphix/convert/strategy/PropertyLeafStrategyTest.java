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
package org.morphix.convert.strategy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link PropertyLeafStrategy}.
 *
 * @author Radu Sebastian LAZIN
 */
class PropertyLeafStrategyTest {

	@Test
	void shouldReturnNullForNullLeafValue() {
		PropertyLeafStrategy strategy = new PropertyLeafStrategy();

		Object result = strategy.convert(null, null, null);

		assertThat(result, nullValue());
	}

	static final class UnsupportedLeaf {
		// empty
	}

	@Test
	void shouldThrowIllegalStateExceptionForUnsupportedLeafType() {
		PropertyLeafStrategy strategy = new PropertyLeafStrategy();

		UnsupportedLeaf value = new UnsupportedLeaf();

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> strategy.convert(value, null, null));

		assertThat(e.getMessage(), equalTo("Unsupported property leaf type: " + UnsupportedLeaf.class.getName()));
	}
}
