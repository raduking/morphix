package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

/**
 * Test class for
 * {@link Reflection#setFieldValue(Object, java.lang.reflect.Field, Object)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionSetFieldValueTest {

	public static class A {

		String a;
		String b;

		public void setA(final String a) {
			this.a = a;
		}
	}

	@Test
	void shouldUseSetterWhenSettingField() {
		A a = new A();

		Reflection.setFieldValue(a, "a", String.class, "b");

		assertThat(a.a, equalTo("b"));
	}

	@Test
	void shouldSetFieldDirectlyWhenNoSetterIsFound() {
		A a = new A();

		Reflection.setFieldValue(a, "b", String.class, "c");

		assertThat(a.b, equalTo("c"));
	}

	@Test
	void shouldNotSetFieldDirectlyWhenFieldTypeIsWrong() {
		A a = new A();

		Reflection.setFieldValue(a, "b", Integer.class, "c");

		assertThat(a.b, equalTo(null));
	}

}
