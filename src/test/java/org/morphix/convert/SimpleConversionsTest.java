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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.morphix.convert.Conversions.convertFrom;

import org.junit.jupiter.api.Test;

/**
 * Tests the simple and basic conversions.
 *
 * @author Radu Sebastian LAZIN
 */
class SimpleConversionsTest {

	private static final int TEST_PRIMITIVE_INT = 15;
	private static final String TEST_STRING = "testString";
	private static final boolean TEST_PRIMITIVE_BOOLEAN = true;

	public static class Source {
		private String stringField;
		protected int intField;
		public boolean booleanField;
		Long longField;
	}

	public static class Destination {
		public String stringField;
		private int intField;
		boolean booleanField;
		protected Long longField;
	}

	@Test
	void shouldConvertSimpleFields() {
		Source src = new Source();
		src.stringField = TEST_STRING;
		src.intField = TEST_PRIMITIVE_INT;
		src.booleanField = TEST_PRIMITIVE_BOOLEAN;

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.stringField, equalTo(dst.stringField));
		assertThat(dst.intField, equalTo(dst.intField));
		assertThat(dst.intField, equalTo(dst.intField));
		assertThat(dst.longField, equalTo(dst.longField));
	}

	@Test
	void shouldIgnoreFieldVisibility() {
		Source src = new Source();
		src.stringField = "testString";

		Destination dst = convertFrom(src, Destination::new);

		assertThat(src.stringField, equalTo(dst.stringField));
	}

	@Test
	void shouldIgnoreNullFieldsInSource() {
		Source src = new Source();

		Destination dst = convertFrom(src, Destination::new);

		assertNull(dst.stringField);
		assertNull(dst.longField);
	}

	public static class DestinationNoOverwrite {
		String stringField;
		int intField;
		boolean booleanField;
		Long longField;

		public DestinationNoOverwrite() {
			stringField = TEST_STRING;
		}
	}

	@Test
	void shouldNotOverwriteDestinationFieldsWithNull() {
		Source src = new Source();

		DestinationNoOverwrite dst = convertFrom(src, DestinationNoOverwrite::new);

		assertThat(dst.stringField, equalTo(TEST_STRING));
		assertNull(dst.longField);
	}

	public static class SourceStatic {
		public static Long longField = 11L;
	}

	@Test
	void shouldIgnoreStaticFields() {
		SourceStatic src = new SourceStatic();

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.longField, equalTo(null));
	}
}
