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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.morphix.convert.Conversions.convertFrom;

import java.time.DateTimeException;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.morphix.convert.ObjectConverterException;

/**
 * Tests conversion from {@link CharSequence} / {@link String} to any class that has a corresponding public static
 * method.
 *
 * @author Radu Sebastian LAZIN
 *
 */
class CharSequenceToAnyTest {

	private static final Long TEST_LONG = 17L;
	private static final String TEST_STRING_LONG = "17";
	private static final String TEST_STRING_1 = "testString1";
	private static final String TEST_STRING_2 = "testC";
	private static final char[] TEST_CHAR_ARRAY = { 't', 'e', 's', 't', 'C' };

	private static final String LOCAL_DATE_STRING = "2016-11-28";
	private static final LocalDate LOCAL_DATE = LocalDate.parse(LOCAL_DATE_STRING);

	public static class Source {
		String lng;
		String date;
		String noConversion;
		String conversion;
	}

	public static class NoConversion {
		String test;
	}

	public static class WillConvert {
		char[] characters;

		public static WillConvert parse(final String s) {
			WillConvert wc = new WillConvert();
			wc.characters = s.toCharArray();
			return wc;
		}
	}

	public static class Destination {
		Long lng;
		LocalDate date;
		NoConversion noConversion;
		WillConvert conversion;
	}

	@Test
	void shouldConvertStringToAny() {
		Source src = new Source();
		src.lng = TEST_STRING_LONG;
		src.date = LOCAL_DATE_STRING;
		src.noConversion = TEST_STRING_1;
		src.conversion = TEST_STRING_2;

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.lng, equalTo(TEST_LONG));
		assertThat(dst.date, equalTo(LOCAL_DATE));
		assertThat(dst.noConversion, equalTo(null));
		assertThat(dst.conversion.characters, equalTo(TEST_CHAR_ARRAY));
	}

	public static class ConvertTryAll {
		char[] characters;

		@SuppressWarnings("unused")
		public static ConvertTryAll valueOf(final String s) {
			return null;
		}

		public static ConvertTryAll parse(final String s) {
			ConvertTryAll ch = new ConvertTryAll();
			ch.characters = s.toCharArray();
			return ch;
		}
	}

	public static class A {
		String convertHard;
	}

	public static class B {
		ConvertTryAll convertHard;
	}

	@Test
	void shouldTryAllPossibleConversionMethods() {
		A a = new A();
		a.convertHard = TEST_STRING_2;

		B b = convertFrom(a, B::new);

		assertThat(b.convertHard.characters, equalTo(TEST_CHAR_ARRAY));
	}

	public static class ConvertFail {
		char[] characters;

		@SuppressWarnings("unused")
		public static ConvertFail parse(final String s) {
			throw new DateTimeException("Failed to parse.");
		}
	}

	public static class C {
		String convertFail;
	}

	public static class D {
		ConvertFail convertFail;
	}

	@Test
	void shouldFailIfParseMethodFails() {
		C c = new C();
		c.convertFail = TEST_STRING_2;

		assertThrows(ObjectConverterException.class, () -> convertFrom(c, D::new));
	}

	public static class E {
		StringBuffer sb;
	}

	public static class F {
		StringBuilder sb;
	}

	@Test
	void shouldNotConvertStringBufferToStringBuilder() {
		E e = new E();
		e.sb = new StringBuffer(TEST_STRING_2);

		F f = convertFrom(e, F::new);

		assertThat(f.sb, equalTo(null));
	}

	@Test
	void shouldSkipNullValues() {
		A a = new A();

		B b = convertFrom(a, B::new);

		assertThat(b.convertHard, equalTo(null));
	}

}
