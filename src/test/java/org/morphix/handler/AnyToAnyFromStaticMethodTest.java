package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.FieldHandlerResult.SKIP;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

/**
 * Test class {@link AnyToAnyFromStaticMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToAnyFromStaticMethodTest {

	private static final Integer TEST_INTEGER = 11;

	public static class A {
		Integer x;
	}

	public static class B {
		String y;

		public static B fromA(final A a) {
			B b = new B();
			b.y = a.x.toString();
			return b;
		}
	}

	public static class C {
		// empty
	}

	public static class Src {
		String x;
		Long l;
		A a;
		A c;
	}

	public static class Dst {
		Locale.Category x;
		BigInteger l;
		B a;
		C c;
	}

	@Test
	void shouldConvertNonCharSequenceToEnum() throws Exception {
		Field sField = Src.class.getDeclaredField("l");
		Field dField = Dst.class.getDeclaredField("l");

		boolean result = new AnyToAnyFromStaticMethod().condition(of(sField), of(dField));

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnAsHandledEvenIfSourceIsNullWithStaticMethod() throws Exception {
		Src src = new Src();
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("a");
		Field dField = Dst.class.getDeclaredField("a");

		FieldHandlerResult result = new AnyToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(BREAK));
	}

	@Test
	void shouldReturnAsNotHandledEvenIfSourceIsNull() throws Exception {
		Src src = new Src();
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("c");
		Field dField = Dst.class.getDeclaredField("c");

		FieldHandlerResult result = new AnyToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIP));
	}

	@Test
	void shouldReturnAsHandledWithStaticMethod() throws Exception {
		Src src = new Src();
		src.a = new A();
		src.a.x = TEST_INTEGER;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("a");
		Field dField = Dst.class.getDeclaredField("a");

		FieldHandlerResult result = new AnyToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

	@Test
	void shouldReturnAsNotHandledIfNoStaticMethodWasFound() throws Exception {
		Src src = new Src();
		src.c = new A();
		src.c.x = TEST_INTEGER;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("c");
		Field dField = Dst.class.getDeclaredField("c");

		FieldHandlerResult result = new AnyToAnyFromStaticMethod().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIP));
	}

}
