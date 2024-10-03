package org.morphix.examples;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;

import org.junit.jupiter.api.Test;

/**
 * Converter example test using a lambda for extra conversions where the fields
 * don't match.
 *
 * @author Radu Sebastian LAZIN
 */
class ExampleLambdaTest {

	public static class A {
		int x;
		int y;
	}

	public static class B {
		String x;
		String z;
	}

	@Test
	void example() {
		A src = new A();
		src.x = 17;
		src.y = 13;

		B dst = convertFrom(src, B::new, (s, d) -> {
			d.z = String.valueOf(s.y);
		});

		assertThat(dst.x, equalTo("17"));
		assertThat(dst.z, equalTo("13"));
	}
}
