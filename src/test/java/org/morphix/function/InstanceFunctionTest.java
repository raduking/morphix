package org.morphix.function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.function.InstanceFunction.reset;
import static org.morphix.function.InstanceFunction.to;
import static org.morphix.function.InstanceFunction.toEmpty;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link InstanceFunction} class.
 *
 * @author Radu Sebastian LAZIN
 */
class InstanceFunctionTest {

	@Test
	void shouldReturnSameInstance() {
		Long expected = 13L;

		Long result = to(expected).instance();

		assertThat(result, equalTo(expected));
	}

	static class A {
		String x;

		A() {
			x = "x";
		}
	}

	static class B {
		byte x1 = 1;
		short x2 = 1;
		int x3 = 1;
		long x4 = 1L;
		float x5 = 1.1f;
		double x6 = 1.1;
		char c = 'x';
		boolean b = true;
	}

	static class C {
		static int x = 10;
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

		assertThat(C.x, equalTo(10));
	}

}
