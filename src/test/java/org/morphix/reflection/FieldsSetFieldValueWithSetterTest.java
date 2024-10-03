package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields#get(Object, String)}
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsSetFieldValueWithSetterTest {

	public static class A {
		Integer x;
		Integer y;

		public void setX(@SuppressWarnings("unused") final Integer x) {
			this.x = 4;
		}
	}

	public static class B extends A {
		// empty
	}

	@Test
	void shouldSetFieldValueBySetter() {
		A a = new A();
		Fields.set(a, "x", 2);

		assertThat(a.x, equalTo(4));
	}

	@Test
	void shouldSetFieldValueByField() {
		A a = new A();
		Fields.set(a, "y", 3);

		assertThat(a.y, equalTo(3));
	}

	@Test
	void shouldSetFieldValueBySetterInHierarchy() {
		B a = new B();
		Fields.set(a, "x", 2);

		assertThat(a.x, equalTo(4));
	}

	@Test
	void shouldSetFieldValueByFieldInHierarchy() {
		B a = new B();
		Fields.set(a, "y", 3);

		assertThat(a.y, equalTo(3));
	}

	@Test
	void shouldThrowExceptionIfFieldDoesNotExist() {
		A a = new A();
		ReflectionException e = assertThrows(ReflectionException.class, () -> Fields.set(a, "z", 3));
		assertThat(e.getMessage(), equalTo("Object does not contain a field named: z"));
	}

}
