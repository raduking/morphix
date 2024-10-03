package org.morphix.examples;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;

import org.junit.jupiter.api.Test;
import org.morphix.annotation.Src;

/**
 * Converter example test using a {@link Src} annotation for conversions where
 * the fields don't match.
 *
 * @author Radu Sebastian LAZIN
 */
class ExampleSrcTest {

	public static class A {
		int x;
		int y;
	}

	public static class B {
		String x;

		@Src("y")
		String z;
	}

	@Test
	void example() {
		A src = new A();
		src.x = 17;
		src.y = 13;

		B dst = convertFrom(src, B::new);

		assertThat(dst.x, equalTo("17"));
		assertThat(dst.z, equalTo("13"));
	}
}
