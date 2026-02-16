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
package org.morphix.convert.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.Converter.convert;
import static org.morphix.convert.FieldHandlerResult.SKIP;
import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.convert.FieldHandlerResult;

/**
 * Test class for {@link AnyToIterable}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToIterableTest {

	private static final Long TEST_LONG = 19L;
	private static final String TEST_STRING_LONG = "19";

	public static class Source {
		String testLong;
	}

	public static class Destination {
		List<Long> testLong;

		public List<Long> getTestLong() {
			return testLong;
		}
	}

	@Test
	void shouldConvertAnyToList() {
		Source s = new Source();
		s.testLong = TEST_STRING_LONG;

		Destination d = convertFrom(s, Destination::new);

		assertThat(d.testLong, hasSize(1));
		assertThat(d.testLong.get(0), equalTo(TEST_LONG));
	}

	@Test
	void shouldConvertAnyToListWithNewApi() {
		Source s = new Source();
		s.testLong = TEST_STRING_LONG;

		Destination d = convert(s).to(Destination::new);

		assertThat(d.testLong, hasSize(1));
		assertThat(d.testLong.get(0), equalTo(TEST_LONG));
	}

	@Test
	void shouldNotConvertNullSource() {
		Source s = new Source();

		Destination d = convertFrom(s, Destination::new);

		assertThat(d.testLong, nullValue());
	}

	public static class A {
		public Integer i;
	}

	public static class B {
		public List<String> i;

		public List<String> getI() {
			return i;
		}
	}

	@Test
	void shouldReturnSkipIfSourceValueIsNull() throws Exception {
		A src = new A();
		B dst = new B();

		Field sField = A.class.getDeclaredField("i");
		Field dField = B.class.getDeclaredField("i");

		FieldHandlerResult result = new AnyToIterable().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIP));
	}

}
