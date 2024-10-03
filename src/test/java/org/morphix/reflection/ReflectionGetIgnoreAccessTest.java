package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.B;

/**
 * Test class for {@link Fields#getIgnoreAccess(Object, Field)}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ReflectionGetIgnoreAccessTest {

	public static final String TEST_STRING = "testString";
	public static final Long TEST_LONG = 17L;
	public static final Integer TEST_INTEGER = 13;

	@Test
	void shouldGetTheFieldValue() throws Exception {
		B b = new B();
		b.s = TEST_STRING;

		String s = Fields.getIgnoreAccess(b, B.class.getDeclaredField("s"));

		assertThat(s, equalTo(TEST_STRING));
	}

	@Test
	void shouldAccessAllFields() throws Exception {
		B b = new B();
		b.s = TEST_STRING;
		b.setL(TEST_LONG);
		b.setI(TEST_INTEGER);

		String s = Fields.getIgnoreAccess(b, B.class.getDeclaredField("s"));
		Long l = Fields.getIgnoreAccess(b, B.class.getDeclaredField("l"));
		Integer i = Fields.getIgnoreAccess(b, B.class.getDeclaredField("i"));

		assertThat(s, equalTo(TEST_STRING));
		assertThat(l, equalTo(TEST_LONG));
		assertThat(i, equalTo(TEST_INTEGER));
	}

	@Test
	void shouldThrowExceptionOnInvalidField() throws Exception {
		Object o = new Object();
		Field s = B.class.getDeclaredField("s");
		assertThrows(ReflectionException.class, () -> Fields.getIgnoreAccess(o, s));
	}

	@Test
	void shouldAccessAllFieldsOnCallWithFieldName() {
		B b = new B();
		b.s = TEST_STRING;
		b.setL(TEST_LONG);
		b.setI(TEST_INTEGER);

		String s = Fields.getIgnoreAccess(b, "s");
		Long l = Fields.getIgnoreAccess(b, "l");
		Integer i = Fields.getIgnoreAccess(b, "i");

		assertThat(s, equalTo(TEST_STRING));
		assertThat(l, equalTo(TEST_LONG));
		assertThat(i, equalTo(TEST_INTEGER));
	}

	@Test
	void shouldThrowExceptionOnNonExistentField() {
		Object o = new Object();
		assertThrows(ReflectionException.class, () -> Fields.getIgnoreAccess(o, "$NonExistentField$"));
	}

	@Test
	void shouldKeepAccessModifiersUnchangedAfterCall() throws Exception {
		B b = new B();
		b.setI(TEST_INTEGER);

		Field field = B.class.getDeclaredField("i");
		Integer i = Fields.getIgnoreAccess(b, field);

		assertThat(i, equalTo(TEST_INTEGER));

		assertThat(field.canAccess(b), equalTo(false));
	}

}
