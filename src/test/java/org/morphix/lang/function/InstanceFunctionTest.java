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
package org.morphix.lang.function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.lang.function.InstanceFunction.reset;
import static org.morphix.lang.function.InstanceFunction.to;
import static org.morphix.lang.function.InstanceFunction.toEmpty;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link InstanceFunction} class.
 *
 * @author Radu Sebastian LAZIN
 */
class InstanceFunctionTest {

	private static final int SOME_INT = 667;
	private static final char CHAR_X = 'x';
	private static final String STRING_X = String.valueOf(CHAR_X);

	@Test
	void shouldReturnSameInstance() {
		Long expected = 666L;

		Long result = to(expected).instance();

		assertThat(result, equalTo(expected));
	}

	static class A {
		String x;

		A() {
			x = STRING_X;
		}
	}

	static class B {
		byte x1 = 1;
		short x2 = 1;
		int x3 = 1;
		long x4 = 1L;
		float x5 = 1.1f;
		double x6 = 1.1;
		char c = CHAR_X;
		boolean b = true;
	}

	static class C {
		static int x = SOME_INT;
	}

	@Test
	void shouldSetAllMembersToNullWhenCallingEmpty() {
		A a = toEmpty(A::new).instance();

		assertThat(a.x, equalTo(null));

		B b = toEmpty(B::new).instance();

		assertThat(b.x1, equalTo((byte) 0));
		assertThat(b.x2, equalTo((short) 0));
		assertThat(b.x3, equalTo(0));
		assertThat(b.x4, equalTo((long) 0));
		assertThat(b.x5, equalTo((float) 0));
		assertThat(b.x6, equalTo((double) 0));
		assertThat(b.b, equalTo(false));
		assertThat(b.c, equalTo((char) 0));
	}

	@Test
	void shouldResetAllMembersToNullWhenCallingReset() {
		A a = new A();
		reset(a);

		assertThat(a.x, equalTo(null));

		B b = new B();
		reset(b);

		assertThat(b.x1, equalTo((byte) 0));
		assertThat(b.x2, equalTo((short) 0));
		assertThat(b.x3, equalTo(0));
		assertThat(b.x4, equalTo((long) 0));
		assertThat(b.x5, equalTo((float) 0));
		assertThat(b.x6, equalTo((double) 0));
		assertThat(b.b, equalTo(false));
		assertThat(b.c, equalTo((char) 0));
	}

	@Test
	void shouldResetAllMembersToNullWhenCallingReset2() {
		A a = new A();
		a = reset(a).instance();

		assertThat(a.x, equalTo(null));

		B b = new B();
		b = reset(b).instance();

		assertThat(b.x1, equalTo((byte) 0));
		assertThat(b.x2, equalTo((short) 0));
		assertThat(b.x3, equalTo(0));
		assertThat(b.x4, equalTo((long) 0));
		assertThat(b.x5, equalTo((float) 0));
		assertThat(b.x6, equalTo((double) 0));
		assertThat(b.b, equalTo(false));
		assertThat(b.c, equalTo((char) 0));
	}

	@Test
	void shouldNotResetStaticFields() {
		C c = new C();
		reset(c);

		assertThat(C.x, equalTo(SOME_INT));
	}

}
