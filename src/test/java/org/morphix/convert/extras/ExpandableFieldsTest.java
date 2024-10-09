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
package org.morphix.convert.extras;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ExpandableFields}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExpandableFieldsTest {

	@Test
	void shouldConstructNullExpandableFieldsObject() {
		ExpandableFields ef = ExpandableFields.of((String[]) null);

		boolean result = ef.equals(ExpandableFields.expandAll());

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldConstructEmptyListExpandableFieldsObject() {
		ExpandableFields ef = ExpandableFields.of();

		boolean result = ef.equals(ExpandableFields.expandNone());

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnFalseOnNullEquals() {
		ExpandableFields ef = ExpandableFields.of(Collections.singletonList("x"));

		boolean result = ef.equals(null);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseOnOtherObjectEquals() {
		ExpandableFields ef = ExpandableFields.of(Collections.singletonList("x"));
		Object mumu = "mumu";

		boolean result = ef.equals(mumu);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseOnNullEqualsWithVarargs() {
		ExpandableFields ef = ExpandableFields.of("x");

		boolean result = ef.equals(null);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseOnOtherObjectEqualsWithVarargs() {
		ExpandableFields ef = ExpandableFields.of("x");
		Object mumu = "mumu";

		boolean result = ef.equals(mumu);

		assertThat(result, equalTo(false));
	}

}
