package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Converted.convert;

import org.junit.jupiter.api.Test;
import org.morphix.extra.ExcludedFields;

/**
 * Test class for excluded fields.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterExcludedFieldsTest {

	private static final Long TEST_LONG = 19L;
	private static final String TEST_STRING_LONG = "19";

	public static class A {
		Long l;

		Long c;
	}

	public static class B {
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
