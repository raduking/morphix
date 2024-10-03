package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.Conversion.convertFromIterable;
import static org.morphix.MethodCaller.call;

import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Converter}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterTest {

	private static final int TEST_PRIMITIVE_INT = 15;
	private static final long TEST_PRIMITIVE_LONG = 17L;
	private static final Long TEST_LONG = TEST_PRIMITIVE_LONG;
	private static final boolean TEST_PRIMITIVE_BOOLEAN = true;

	private static final String TEST_STRING = "testString";
	private static final String TEST_STRING_2 = "testString2";
	private static final String TEST_STRING_3 = "testString3";
	private static final String TEST_EMPTY_STRING = "";
	private static final String LOCAL_DATE_STRING = "2016-11-28";
	private static final LocalDate LOCAL_DATE = LocalDate.parse(LOCAL_DATE_STRING);
	private static final int ZERO = 0;

	public static class Source {
		public boolean booleanField;
		protected int intField;
		Long longField;
		@SuppressWarnings("unused")
		private String stringField;
	}

	public static class Destination {
		public String stringField;
		protected Long longField;
		boolean booleanField;
		private int intField;
	}

	@Test
	void shouldConvertSimpleFieldsWithBaseClassImplementation() {
		Converter<Source, Destination> converterObject = new Converter<>() {
			@Serial
			private static final long serialVersionUID = 6125710739771191085L;

			@Override
			public Destination instance() {
				return new Destination();
			}

			@Override
			public void convert(final Source src, final Destination dst) {
				// empty
			}
		};

		Source src = new Source();
		src.stringField = TEST_STRING;
		src.intField = TEST_PRIMITIVE_INT;
		src.booleanField = TEST_PRIMITIVE_BOOLEAN;

		Destination dst = converterObject.convert(src);
		assertThat(dst.stringField, equalTo(dst.stringField));
		assertThat(dst.intField, equalTo(dst.intField));
		assertThat(dst.intField, equalTo(dst.intField));
		assertThat(dst.longField, equalTo(dst.longField));
	}

	@Test
	void shouldThrowWhenInstanceMethodNotImplementedWithBaseClassImplementation() {
		Converter<Source, Destination> converterObject = new Converter<>() {
			@Serial
			private static final long serialVersionUID = 3181797337647731146L;
			// converter with no instance method defined
		};

		Source src = new Source();
		assertThrows(ConverterException.class, () -> converterObject.convert(src));
	}

	public static class Source6 {
		String stringField;
	}

	public static class Destination6 {
		private String stringField;

		public void setStringField(final String stringField) {
			this.stringField = stringField;
		}
	}

	@Test
	void shouldCallSetterMethod() throws Exception {
		Destination6 dst = new Destination6();

		call(dst::setStringField, () -> TEST_STRING);

		assertThat(dst.stringField, equalTo(TEST_STRING));

		Source6 src = new Source6();
		src.stringField = TEST_STRING;

		// syntax for mandatory fields
		dst = convertFrom(src, Destination6::new, (s, d) -> {
			try {
				call(d::setStringField, () -> TEST_STRING_2);
			} catch (Exception e) {
				// empty, you can escalate exception
			}
		});
		assertThat(dst.stringField, equalTo(TEST_STRING_2));

		List<Exception> exceptions = new ArrayList<>();
		// syntax for optional fields
		dst = convertFrom(src, Destination6::new, (s, d) -> {
			call(d::setStringField, () -> TEST_STRING_3, exceptions);
		});
		assertThat(dst.stringField, equalTo(TEST_STRING_3));
		assertThat(exceptions, hasSize(ZERO));
	}

	@Test
	void shouldThrowExceptionOnCallWhenLambdaThrows() {
		Destination6 dst = new Destination6();

		assertThrows(ClassNotFoundException.class, () ->
				call(dst::setStringField, () -> {
					if (TEST_EMPTY_STRING.isEmpty()) {
						throw new ClassNotFoundException();
					}
					return TEST_STRING;
				}));
	}

	@Test
	void shouldThrowExceptionOnCallWhenLambdaThrowsAndExceptionsAreEscalated() {
		Destination6 dst = new Destination6();
		dst.stringField = TEST_STRING;

		List<Exception> exceptions = new ArrayList<>();
		call(dst::setStringField, () -> {
			if (TEST_EMPTY_STRING.isEmpty()) {
				throw new ClassNotFoundException();
			}
			return TEST_STRING_2;
		}, exceptions);

		assertThat(dst.stringField, equalTo(TEST_STRING));
	}

	@Test
	void shouldCreateDestinationInstanceWithClass() {
		Source6 src = new Source6();
		src.stringField = TEST_STRING;

		Destination6 dst = convertFrom(src, Destination6.class);

		assertThat(dst.stringField, equalTo(TEST_STRING));
	}

	public static class Source7 {
		Long lng;
		String date;
		String noConversion;
	}

	public static class NoConversion {
		String test;
	}

	public static class Destination7 {
		Long lng;
		LocalDate date;
		NoConversion noConversion;
	}

	@Test
	void shouldUseInstanceFunctionOnAllElementsOnIterablesWithSuppliedConverter() {
		final int listSize = 7;
		List<Source7> src7List = new ArrayList<>(listSize);
		for (int i = 0; i < listSize; ++i) {
			Source7 src = new Source7();
			src.lng = TEST_LONG;
			src.date = LOCAL_DATE_STRING;
			src.noConversion = TEST_STRING_2;
			src7List.add(src);
		}

		List<Destination7> result = convertFromIterable(src7List,
				ConverterBuilder.<Source7, Destination7>newConverter(Configuration.defaultConfiguration())::convert,
				Destination7::new);

		assertThat(result, hasSize(equalTo(src7List.size())));

		for (int i = 0; i < listSize; ++i) {
			Destination7 dst = result.get(i);

			assertThat(dst.lng, equalTo(TEST_LONG));
			assertThat(dst.date, equalTo(LOCAL_DATE));
			assertThat(dst.noConversion, equalTo(null));
		}
	}

	@Test
	void shouldUseInstanceFunctionOnAllElementsOnIterables() {
		final int listSize = 7;
		List<Source7> src7List = new ArrayList<>(listSize);
		for (int i = 0; i < listSize; ++i) {
			Source7 src = new Source7();
			src.lng = TEST_LONG;
			src.date = LOCAL_DATE_STRING;
			src.noConversion = TEST_STRING_2;
			src7List.add(src);
		}

		List<Destination7> result = convertFromIterable(src7List, Destination7::new);

		assertThat(result, hasSize(equalTo(src7List.size())));

		for (int i = 0; i < listSize; ++i) {
			Destination7 dst = result.get(i);

			assertThat(dst.lng, equalTo(TEST_LONG));
			assertThat(dst.date, equalTo(LOCAL_DATE));
			assertThat(dst.noConversion, equalTo(null));
		}
	}

	private static Destination7 convertMethod(final Source7 src) {
		return convertFrom(src, Destination7::new, (s, d) -> {
			NoConversion nc = new NoConversion();
			nc.test = s.noConversion;
			d.noConversion = nc;
		});
	}

	@Test
	void shouldCallConvertMethodOnIterables() {
		final int listSize = 7;
		List<Source7> src7List = new ArrayList<>(listSize);
		for (int i = 0; i < listSize; ++i) {
			Source7 src = new Source7();
			src.lng = TEST_LONG;
			src.date = LOCAL_DATE_STRING;
			src.noConversion = TEST_STRING_2;
			src7List.add(src);
		}

		Iterable<Source7> iterable = src7List;
		List<Destination7> result = convertFromIterable(iterable, ConverterTest::convertMethod);

		assertThat(result, hasSize(equalTo(src7List.size())));

		for (int i = 0; i < listSize; ++i) {
			Destination7 dst = result.get(i);

			assertThat(dst.lng, equalTo(TEST_LONG));
			assertThat(dst.date, equalTo(LOCAL_DATE));
			assertThat(dst.noConversion.test, equalTo(TEST_STRING_2));
		}
	}

}
