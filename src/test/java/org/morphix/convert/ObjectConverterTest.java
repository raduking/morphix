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
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.Conversions.convertFromIterable;
import static org.morphix.lang.function.MethodCaller.call;

import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ObjectConverter}.
 *
 * @author Radu Sebastian LAZIN
 */
class ObjectConverterTest {

	private static final int TEST_PRIMITIVE_INT = 666;
	private static final long TEST_PRIMITIVE_LONG = 13L;
	private static final Long TEST_LONG = TEST_PRIMITIVE_LONG;
	private static final boolean TEST_PRIMITIVE_BOOLEAN = true;

	private static final String TEST_STRING_1 = "testString1";
	private static final String TEST_STRING_2 = "testString2";
	private static final String TEST_STRING_3 = "testString3";
	private static final String TEST_EMPTY_STRING = "";
	private static final String LOCAL_DATE_STRING = "2025-02-17";

	private static final LocalDate LOCAL_DATE = LocalDate.parse(LOCAL_DATE_STRING);
	private static final int ZERO = 0;
	private static final int SIZE = 10;

	static class Src {
		public boolean booleanField;
		protected int intField;
		Long longField;
		@SuppressWarnings("unused")
		private String stringField;
	}

	static class Dst {
		public String stringField;
		protected Long longField;
		boolean booleanField;
		private int intField;
	}

	@Test
	void shouldConvertSimpleFieldsWithBaseClassImplementation() {
		ObjectConverter<Src, Dst> converterObject = new ObjectConverter<>() {
			@Serial
			private static final long serialVersionUID = 3949852030965235014L;

			@Override
			public Dst instance() {
				return new Dst();
			}

			@Override
			public void convert(final Src src, final Dst dst) {
				// empty
			}
		};

		Src src = new Src();
		src.stringField = TEST_STRING_1;
		src.intField = TEST_PRIMITIVE_INT;
		src.booleanField = TEST_PRIMITIVE_BOOLEAN;

		Dst dst = converterObject.convert(src);
		assertThat(dst.stringField, equalTo(dst.stringField));
		assertThat(dst.intField, equalTo(dst.intField));
		assertThat(dst.intField, equalTo(dst.intField));
		assertThat(dst.longField, equalTo(dst.longField));
	}

	@Test
	void shouldThrowWhenInstanceMethodNotImplementedWithBaseClassImplementation() {
		ObjectConverter<Src, Dst> converterObject = new ObjectConverter<>() {
			@Serial
			private static final long serialVersionUID = -7116664872731319296L;
			// converter with no instance method defined
		};

		Src src = new Src();
		assertThrows(ObjectConverterException.class, () -> converterObject.convert(src));
	}

	public static class Src1 {
		String stringField;
	}

	public static class Dst1 {
		private String stringField;

		public void setStringField(final String stringField) {
			this.stringField = stringField;
		}
	}

	@Test
	void shouldCallSetterMethod() throws Exception {
		Dst1 dst = new Dst1();

		call(dst::setStringField, () -> TEST_STRING_1);

		assertThat(dst.stringField, equalTo(TEST_STRING_1));

		Src1 src = new Src1();
		src.stringField = TEST_STRING_1;

		// syntax for mandatory fields
		dst = convertFrom(src, Dst1::new, (s, d) -> {
			try {
				call(d::setStringField, () -> TEST_STRING_2);
			} catch (Exception e) {
				// empty, you can escalate exception
			}
		});
		assertThat(dst.stringField, equalTo(TEST_STRING_2));

		List<Exception> exceptions = new ArrayList<>();
		// syntax for optional fields
		dst = convertFrom(src, Dst1::new, (s, d) -> {
			call(d::setStringField, () -> TEST_STRING_3, exceptions);
		});
		assertThat(dst.stringField, equalTo(TEST_STRING_3));
		assertThat(exceptions, hasSize(ZERO));
	}

	@Test
	void shouldThrowExceptionOnCallWhenLambdaThrows() {
		Dst1 dst = new Dst1();

		assertThrows(ClassNotFoundException.class, () -> call(dst::setStringField, () -> {
			if (TEST_EMPTY_STRING.isEmpty()) {
				throw new ClassNotFoundException();
			}
			return TEST_STRING_1;
		}));
	}

	@Test
	void shouldThrowExceptionOnCallWhenLambdaThrowsAndExceptionsAreEscalated() {
		Dst1 dst = new Dst1();
		dst.stringField = TEST_STRING_1;

		List<Exception> exceptions = new ArrayList<>();
		call(dst::setStringField, () -> {
			if (TEST_EMPTY_STRING.isEmpty()) {
				throw new ClassNotFoundException();
			}
			return TEST_STRING_2;
		}, exceptions);

		assertThat(dst.stringField, equalTo(TEST_STRING_1));
	}

	@Test
	void shouldCreateDestinationInstanceWithClass() {
		Src1 src = new Src1();
		src.stringField = TEST_STRING_1;

		Dst1 dst = convertFrom(src, Dst1.class);

		assertThat(dst.stringField, equalTo(TEST_STRING_1));
	}

	public static class Src2 {
		Long lng;
		String date;
		String noConversion;
	}

	public static class NoConversion {
		String test;
	}

	public static class Dst2 {
		Long lng;
		LocalDate date;
		NoConversion noConversion;
	}

	@Test
	void shouldUseInstanceFunctionOnAllElementsOnIterablesWithSuppliedConverter() {
		List<Src2> src2List = new ArrayList<>(SIZE);
		for (int i = 0; i < SIZE; ++i) {
			Src2 src = new Src2();
			src.lng = TEST_LONG;
			src.date = LOCAL_DATE_STRING;
			src.noConversion = TEST_STRING_2;
			src2List.add(src);
		}

		List<Dst2> result = convertFromIterable(src2List,
				ConverterFactory.<Src2, Dst2>newObjectConverter(Configuration.defaultConfiguration())::convert,
				Dst2::new);

		assertThat(result, hasSize(equalTo(src2List.size())));

		for (int i = 0; i < SIZE; ++i) {
			Dst2 dst = result.get(i);

			assertThat(dst.lng, equalTo(TEST_LONG));
			assertThat(dst.date, equalTo(LOCAL_DATE));
			assertThat(dst.noConversion, equalTo(null));
		}
	}

	@Test
	void shouldUseInstanceFunctionOnAllElementsOnIterables() {
		List<Src2> src2List = new ArrayList<>(SIZE);
		for (int i = 0; i < SIZE; ++i) {
			Src2 src = new Src2();
			src.lng = TEST_LONG;
			src.date = LOCAL_DATE_STRING;
			src.noConversion = TEST_STRING_2;
			src2List.add(src);
		}

		List<Dst2> result = convertFromIterable(src2List, Dst2::new);

		assertThat(result, hasSize(equalTo(src2List.size())));

		for (int i = 0; i < SIZE; ++i) {
			Dst2 dst = result.get(i);

			assertThat(dst.lng, equalTo(TEST_LONG));
			assertThat(dst.date, equalTo(LOCAL_DATE));
			assertThat(dst.noConversion, equalTo(null));
		}
	}

	private static Dst2 convertMethod(final Src2 src) {
		return convertFrom(src, Dst2::new, (s, d) -> {
			NoConversion nc = new NoConversion();
			nc.test = s.noConversion;
			d.noConversion = nc;
		});
	}

	@Test
	void shouldCallConvertMethodOnIterables() {
		List<Src2> src2List = new ArrayList<>(SIZE);
		for (int i = 0; i < SIZE; ++i) {
			Src2 src = new Src2();
			src.lng = TEST_LONG;
			src.date = LOCAL_DATE_STRING;
			src.noConversion = TEST_STRING_2;
			src2List.add(src);
		}

		Iterable<Src2> iterable = src2List;
		List<Dst2> result = convertFromIterable(iterable, ObjectConverterTest::convertMethod);

		assertThat(result, hasSize(equalTo(src2List.size())));

		for (int i = 0; i < SIZE; ++i) {
			Dst2 dst = result.get(i);

			assertThat(dst.lng, equalTo(TEST_LONG));
			assertThat(dst.date, equalTo(LOCAL_DATE));
			assertThat(dst.noConversion.test, equalTo(TEST_STRING_2));
		}
	}

}
