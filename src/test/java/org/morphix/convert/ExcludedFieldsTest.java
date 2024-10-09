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
import static org.morphix.convert.Converter.convert;

import org.junit.jupiter.api.Test;
import org.morphix.convert.extras.ExcludedFields;

/**
 * Test class for excluded fields.
 *
 * @author Radu Sebastian LAZIN
 */
class ExcludedFieldsTest {

	private static final Long TEST_LONG = 19L;
	private static final String TEST_STRING_LONG = String.valueOf(TEST_LONG);

	static class A {
		Long l;
		Long c;
	}

	static class B {
		String l;
		String c;
	}

	@Test
	void shouldExcludeAllFields() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convert(a)
				.with(ExcludedFields.excludeAll())
				.to(B::new);

		assertThat(b.l, equalTo(null));
		assertThat(b.c, equalTo(null));
	}

	@Test
	void shouldExcludeGivenFields() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convert(a)
				.exclude("l")
				.to(B::new);

		assertThat(b.l, equalTo(null));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

	@Test
	void shouldExcludeGivenFieldsWith() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convert(a)
				.with(ExcludedFields.of("c"))
				.to(B::new);

		assertThat(b.l, equalTo(TEST_STRING_LONG));
		assertThat(b.c, equalTo(null));
	}

	@Test
	void shouldExcludeNoFields() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convert(a)
				.with(ExcludedFields.excludeNone())
				.to(B::new);

		assertThat(b.l, equalTo(TEST_STRING_LONG));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

}
