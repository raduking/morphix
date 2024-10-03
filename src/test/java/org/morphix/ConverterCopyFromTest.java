package org.morphix;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.morphix.Conversion.copyFrom;
import static org.morphix.function.InstanceFunction.to;

import org.junit.jupiter.api.Test;
import org.morphix.function.InstanceFunction;

/**
 * Tests the {@link Conversion#copyFrom(Object, InstanceFunction)} class.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterCopyFromTest {

	private static final int INT_13 = 13;
	private static final int INT_17 = 17;
	private static final String TEST_STRING_1 = "test11111";
	private static final String TEST_STRING_2 = "test22222";

	public static class A {
		int i;
		String s;

		static long l = 11L;
	}

	public static class B {
		int j;
		String s;

		static long l = 22L;
	}

	@Test
	void shouldCopyFromAnObjectToAnother() {
		A a = new A();
		a.i = INT_13;
		a.s = TEST_STRING_1;

		B b = new B();
		b.j = INT_17;
		b.s = TEST_STRING_2;

		B bCopied = copyFrom(a, to(b), false);
		B bCopiedWithoutOverrideFlag = copyFrom(a, to(b));

		assertThat(bCopied.j, equalTo(INT_17));
		assertThat(b.s, equalTo(TEST_STRING_1));
		assertThat(bCopied, equalTo(b));
		assertThat(bCopied, equalTo(bCopiedWithoutOverrideFlag));
	}

	@Test
	void shouldNotCopyFromAnObjectToNull() {
		B b = new B();
		A a = null;

		A result = copyFrom(b, to(a));

		assertThat(result, is(equalTo(null)));

		result = copyFrom(b, null, false);
		B bCopiedWithoutOverrideFlag = copyFrom(b, null);

		assertThat(result, is(equalTo(null)));
		assertThat(result, equalTo(bCopiedWithoutOverrideFlag));
	}

	@Test
	void shouldNotCopyIfSourceIsNull() {
		B b = new B();

		B result = copyFrom(null, to(b), false);
		B bCopiedWithoutOverrideFlag = copyFrom(null, to(b));

		assertThat(result, is(equalTo(b)));
		assertThat(result, equalTo(bCopiedWithoutOverrideFlag));
	}

	@Test
	void shouldCopyFromAnObjectToAnotherIgnoringNulls() {
		A a = new A();
		a.i = INT_13;
		a.s = null;

		A b = new A();
		b.i = INT_17;
		b.s = TEST_STRING_2;

		A bCopied = copyFrom(a, to(b), false);

		assertThat(bCopied.i, equalTo(INT_13));
		assertThat(b.s, equalTo(TEST_STRING_2));
		assertThat(bCopied, equalTo(b));
	}

	@Test
	void shouldCopyFromAnObjectToAnotherOverridingNulls() {
		A a = new A();
		a.i = INT_13;
		a.s = null;

		A b = new A();
		b.i = INT_17;
		b.s = TEST_STRING_2;

		A bCopied = copyFrom(a, to(b), true);

		assertThat(bCopied.i, equalTo(INT_13));
		assertThat(b.s, equalTo(null));
		assertThat(bCopied, equalTo(b));
	}

	@Test
	void shouldNotCopyStaticFields() {
		A a = new A();
		B b = new B();

		copyFrom(a, to(b), false);
		assertThat(B.l, equalTo(22L));
	}

}
