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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.morphix.convert.Conversions.copyFrom;
import static org.morphix.lang.function.InstanceFunction.to;

import org.junit.jupiter.api.Test;
import org.morphix.lang.function.InstanceFunction;

/**
 * Tests the {@link Conversions#copyFrom(Object, InstanceFunction)} class.
 *
 * @author Radu Sebastian LAZIN
 */
class CopyFromTest {

	private static final int TEST_INT1 = 13;
	private static final int TEST_INT2 = 17;

	private static final long TEST_LONG1 = 11L;
	private static final long TEST_LONG2 = 22L;

	private static final String TEST_STRING1 = "test11111";
	private static final String TEST_STRING2 = "test22222";

	static class A {
		int i;
		String s;

		static long l = TEST_LONG1;
	}

	static class B {
		int j;
		String s;

		static long l = TEST_LONG2;
	}

	@Test
	void shouldCopyFromAnObjectToAnother() {
		A a = new A();
		a.i = TEST_INT1;
		a.s = TEST_STRING1;

		B b = new B();
		b.j = TEST_INT2;
		b.s = TEST_STRING2;

		B bCopied = copyFrom(a, to(b), false);
		B bCopiedWithoutOverrideFlag = copyFrom(a, to(b));

		assertThat(bCopied.j, equalTo(TEST_INT2));
		assertThat(b.s, equalTo(TEST_STRING1));
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
	void shouldNotCopyStaticFields() {
		A a = new A();
		B b = new B();

		copyFrom(a, to(b), false);
		assertThat(B.l, equalTo(TEST_LONG2));
	}

	@Test
	void shouldCopyFromAnObjectToAnotherIgnoringNulls() {
		A a = new A();
		a.i = TEST_INT1;
		a.s = null;

		A b = new A();
		b.i = TEST_INT2;
		b.s = TEST_STRING2;

		A bCopied = copyFrom(a, to(b), false);

		assertThat(bCopied.i, equalTo(TEST_INT1));
		assertThat(b.s, equalTo(TEST_STRING2));
		assertThat(bCopied, equalTo(b));
	}

	@Test
	void shouldCopyFromAnObjectToAnotherOverridingNulls() {
		A a = new A();
		a.i = TEST_INT1;
		a.s = null;

		A b = new A();
		b.i = TEST_INT2;
		b.s = TEST_STRING2;

		A bCopied = copyFrom(a, to(b), true);

		assertThat(bCopied.i, equalTo(TEST_INT1));
		assertThat(b.s, equalTo(null));
		assertThat(bCopied, equalTo(b));
	}

}
