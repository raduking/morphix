package org.morphix;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.Converted.convert;
import static org.morphix.function.InstanceFunction.to;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.annotation.Expandable;
import org.morphix.handler.ExpandableFieldHandler;

/**
 * Test class for {@link ExpandableFieldHandler}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterExpandableFieldHandlerTest {

	private static final Long TEST_LONG = 19L;
	private static final String TEST_STRING_LONG = "19";
	private static final String PREFIX = "x";

	public static class A {
		@Expandable
		Long l;

		Long c;
	}

	public static class B {
		String l;

		String c;
	}

	@Test
	void shouldConvertAllExpandableFields() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convertFrom(a, B::new, (List<String>) null);

		assertThat(b.l, equalTo(TEST_STRING_LONG));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

	@Test
	void shouldConvertAllExpandableFieldsWithExtraConvertFunction() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convertFrom(a, B::new, (s, d) -> {
			d.c = PREFIX + d.c;
		}, (List<String>) null);

		assertThat(b.l, equalTo(TEST_STRING_LONG));
		assertThat(b.c, equalTo(PREFIX + TEST_STRING_LONG));
	}

	@Test
	void shouldConvertAllExpandableFieldsWithConvertMethod() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convertFrom(a, B::new, (List<String>) null, (final Integer l) -> PREFIX + l);

		assertThat(b.l, equalTo(TEST_STRING_LONG));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

	@Test
	void shouldIgnoreExpandableFields() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convertFrom(a, B::new, emptyList());

		assertThat(b.l, equalTo(null));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

	@Test
	void shouldIgnoreExpandableFieldsWithConvertMethod() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convertFrom(a, B::new, emptyList(), (final Integer l) -> PREFIX + l);

		assertThat(b.l, equalTo(null));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

	@Test
	void shouldIgnoreExpandableFieldsWithConvertMethodAndExistingInstance() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = new B();
		b.l = "bubu";

		b = convertFrom(a, to(b), emptyList(), (final Integer l) -> PREFIX + l);

		assertThat(b.l, equalTo(null));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

	@Test
	void shouldExpandSpecifiedFields() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convertFrom(a, B::new, singletonList("l"));

		assertThat(b.l, equalTo(TEST_STRING_LONG));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

	@Test
	void shouldExpandSpecifiedFieldsWithConvertMethod() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convertFrom(a, B::new, singletonList("l"), (final Integer l) -> PREFIX + l);

		assertThat(b.l, equalTo(TEST_STRING_LONG));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

	@Test
	void shouldExpandSpecifiedFields2WithConvertMethod() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convertFrom(a, B::new, List.of("l", "c"), (final Integer l) -> PREFIX + l);

		assertThat(b.l, equalTo(TEST_STRING_LONG));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

	@Test
	void shouldExpandableFieldsWithConvertedSyntax() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convert(a)
				.with(singletonList("l"))
				.to(B::new);

		assertThat(b.l, equalTo(TEST_STRING_LONG));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

	@Test
	void shouldExpandableFieldsWithConvertedArraySyntax() {
		A a = new A();
		a.l = TEST_LONG;
		a.c = TEST_LONG;

		B b = convert(a)
				.with("l")
				.to(B::new);

		assertThat(b.l, equalTo(TEST_STRING_LONG));
		assertThat(b.c, equalTo(TEST_STRING_LONG));
	}

}
