package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.InstanceCreator;

/**
 * Conversion should work with classes without default constructors.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterAnyToAnyWithoutDefaultConstructorTest {

	private static final Integer TEST_INTEGER = 11;
	private static final String TEST_STRING = TEST_INTEGER.toString();

	public static class A {
		String x;
	}

	public static class B {
		Integer x;

		public B(final Integer x) {
			this.x = x;
		}
	}

	public static class Src {
		A a;
	}

	public static class Dst {
		B a;
	}

	@BeforeEach
	public void setUp() {
		InstanceCreator instanceCreator = InstanceCreator.getInstance();
		assumeTrue(instanceCreator.isUsable());
	}

	@Test
	void shouldConvertObjects() {
		Src src = new Src();
		A a = new A();
		a.x = TEST_STRING;
		src.a = a;

		Dst dst = Conversion.convertFrom(src, Dst::new);

		assertThat(dst.a.x, equalTo(TEST_INTEGER));
	}

}
