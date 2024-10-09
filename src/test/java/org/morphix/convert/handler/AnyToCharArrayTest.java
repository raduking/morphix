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
package org.morphix.convert.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.convert.DefaultFieldHandlers;
import org.morphix.convert.FieldHandlerResult;

/**
 * Tests conversions from any to char array (char[]).
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToCharArrayTest {

	private static final Long TEST_LONG = 1234321L;
	private static final char[] TEST_CHAR_ARRAY_LONG = { '1', '2', '3', '4', '3', '2', '1' };

	private static final boolean TEST_BOOLEAN = true;
	private static final char[] TEST_CHAR_ARRAY_BOOLEAN = { 't', 'r', 'u', 'e' };

	private static final char[] TEST_CHAR_ARRAY_BOOLEAN_DEFAULT = { 'f', 'a', 'l', 's', 'e' };

	public static class Source {
		Long id;
		boolean good;
	}

	public static class Destination {
		char[] id;
		char[] good;
	}

	@Test
	void shouldConvertAnyToCharArray() {
		Source src = new Source();
		src.id = TEST_LONG;
		src.good = TEST_BOOLEAN;

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(TEST_CHAR_ARRAY_LONG));
		assertThat(dst.good, equalTo(TEST_CHAR_ARRAY_BOOLEAN));
	}

	@Test
	void shouldSkipNullValues() {
		Source src = new Source();

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(null));
		assertThat(dst.good, equalTo(TEST_CHAR_ARRAY_BOOLEAN_DEFAULT));
	}

	@Test
	void shouldConsiderTheFieldHandledEvenIfTheSourceHasNullValueWithDefaultFieldHandler() throws Exception {
		Source src = new Source();
		Destination dst = new Destination();

		Field sField = Source.class.getDeclaredField("id");
		Field dField = Destination.class.getDeclaredField("id");

		FieldHandlerResult result = DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_CHAR_ARRAY.handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

	@Test
	void shouldConsiderTheFieldHandledEvenIfTheSourceHasNullValue() throws Exception {
		Source src = new Source();
		Destination dst = new Destination();

		Field sField = Source.class.getDeclaredField("id");
		Field dField = Destination.class.getDeclaredField("id");

		FieldHandlerResult result = new AnyToCharArray().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

}
