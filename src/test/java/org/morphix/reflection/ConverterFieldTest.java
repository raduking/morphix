package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ConverterField}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterFieldTest {

	private static final String EOL = System.lineSeparator();

	@Test
	void shouldReturnObjectClassIfNoFieldIsSet() {
		ConverterField sfo = of(null);

		Class<?> result = sfo.toClass();

		assertThat(result, equalTo(Object.class));
	}

	public static class A {
		Long l;
	}

	@Test
	void shouldBuildStringWithToString() throws Exception {
		A a = new A();
		a.l = 11L;

		ConverterField fop = of(A.class.getDeclaredField("l"), a);
		String result = fop.toString();

		assertThat(result, equalTo("Field: l" + EOL
				+ "Type: class java.lang.Long" + EOL
				+ "Value: 11" + EOL
				+ "Object: " + a));
	}

	@Test
	void shouldBuildStringWithToStringWithNoField() {
		A a = new A();
		a.l = 11L;

		ConverterField fop = of((Field) null, a);
		String result = fop.toString();

		assertThat(result, equalTo("Object: " + a));
	}

	@Test
	void shouldBuildStringWithToStringWithNoObject() throws Exception {
		A a = new A();
		a.l = 11L;

		ConverterField fop = of(A.class.getDeclaredField("l"));
		String result = fop.toString();

		assertThat(result, equalTo("Field: l" + EOL
				+ "Type: class java.lang.Long"));
	}

}
