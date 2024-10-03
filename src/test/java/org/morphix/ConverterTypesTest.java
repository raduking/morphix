package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;

import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;

/**
 * Test class for converter with types.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterTypesTest {

	public static class A {
		String b;
	}

	public static class B {
		int b;
	}

	@Test
	void shouldConvertFromType() {
		A a = new A();
		a.b = "13";

		Type type = B.class;

		B b = convertFrom(a, type);

		assertThat(b.b, equalTo(13));
	}

}
