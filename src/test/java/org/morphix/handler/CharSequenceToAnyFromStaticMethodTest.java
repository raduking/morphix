package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.FieldHandlerResult.SKIP;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

/**
 * Test class for {@link CharSequenceToAnyFromStaticMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
class CharSequenceToAnyFromStaticMethodTest {

	public static final String TEST_STRING = "testString";

	public static class A {
		String x;

		public static A fromString(final String s) {
			A a = new A();
			a.x = s;
			return a;
		}
	}

	public static class Src {
		String a;
		Integer b;
		String s;
		String c;
	}

	public static class Dst {
		A a;
		A b;
		String s;
		C c;
	}

	public static class C {
		// empty
	}

	@Test
	void shouldReturnAsNotHandledEvenIfSourceIsNull() throws Exception {
		Src src = new Src();
		src.a = null;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("a");
		Field dField = Dst.class.getDeclaredField("a");

		FieldHandlerResult result = new CharSequenceToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(BREAK));
	}

	@Test
	void shouldReturnAsHandledWithStaticMethod() throws Exception {
		Src src = new Src();
		src.a = TEST_STRING;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("a");
		Field dField = Dst.class.getDeclaredField("a");

		FieldHandlerResult result = new CharSequenceToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

	@Test
	void shouldReturnAsNotHandledWithStaticMethodIfNoStaticConvertMethodWasFound() throws Exception {
		Src src = new Src();
		src.c = TEST_STRING;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("c");
		Field dField = Dst.class.getDeclaredField("c");

		FieldHandlerResult result = new CharSequenceToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIP));
	}

	@Test
	void shouldReturnAsNotHandledWithStaticMethodIfNoStaticConvertMethodWasFoundAndFieldIsNull() throws Exception {
		Src src = new Src();
		src.c = null;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("c");
		Field dField = Dst.class.getDeclaredField("c");

		FieldHandlerResult result = new CharSequenceToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIP));
	}

}
