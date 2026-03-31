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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Holder}.
 *
 * @author Radu Sebastian LAZIN
 */
class HolderTest {

	private static final String TEST_STRING = "mumu";

	@Test
	void shouldConstructAHolderWithoutAValue() {
		Holder<String> holder = new Holder<>();

		assertThat(holder.getValue(), nullValue());
	}

	@Test
	void shouldConstructAHolderWithAValue() {
		Holder<String> holder = new Holder<>(TEST_STRING);

		assertThat(holder.getValue(), equalTo(TEST_STRING));
	}

	@Test
	void shouldConstructAHolderWithoutAValueWithStaticMethod() {
		Holder<String> holder = Holder.empty();

		assertThat(holder.getValue(), nullValue());
	}

	@Test
	void shouldConstructAHolderWithAValueWithStaticMethod() {
		Holder<String> holder = Holder.of(TEST_STRING);

		assertThat(holder.getValue(), equalTo(TEST_STRING));
	}

	@Nested
	class EqualsTests {

		@Test
		void shouldCheckValueOnEquals() {
			Holder<String> holder1 = Holder.of(TEST_STRING);
			Holder<String> holder2 = Holder.of(TEST_STRING);

			assertThat(holder1, equalTo(holder2));
		}

		@Test
		void shouldCheckNullValueOnEquals() {
			Holder<String> holder1 = Holder.empty();
			Holder<String> holder2 = Holder.empty();

			assertThat(holder1, equalTo(holder2));
		}

		@Test
		void shouldNotEqualIfOneValueIsNull() {
			Holder<String> holder1 = Holder.empty();
			Holder<String> holder2 = Holder.of(TEST_STRING);

			assertThat(holder1, not(equalTo(holder2)));
		}

		@Test
		void shouldNotEqualIfValuesAreDifferent() {
			Holder<String> holder1 = Holder.of(TEST_STRING);
			Holder<String> holder2 = Holder.of("different");

			assertThat(holder1, not(equalTo(holder2)));
		}

		@Test
		void shouldNotEqualIfOtherIsNotAHolder() {
			Holder<String> holder = Holder.of(TEST_STRING);
			String other = TEST_STRING;

			assertThat(holder, not(equalTo(other)));
		}

		@Test
		void shouldEqualSameInstance() {
			Holder<String> holder = Holder.of(TEST_STRING);

			assertThat(holder, equalTo(holder));
		}
	}

	@Test
	void shouldHaveHashCodeBasedOnValue() {
		Holder<String> holder1 = Holder.of(TEST_STRING);
		Holder<String> holder2 = Holder.of(TEST_STRING);

		assertThat(holder1.hashCode(), equalTo(holder2.hashCode()));
		assertThat(holder1.hashCode(), equalTo(TEST_STRING.hashCode()));
	}
}
