package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.FieldHandlerResult.SKIP;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

/**
 * Test class for {@link AnyToAny}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToAnyTest {

	public static class A {
		String x;
	}

	public static class B {
		Integer x;
	}

	public static class Src {
		A a;
	}

	public static class Dst {
		B a;
	}

	@Test
	void shouldReturnAsNotHandledIfSourceIsNull() throws Exception {
		Src src = new Src();
		src.a = null;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("a");
		Field dField = Dst.class.getDeclaredField("a");

		FieldHandlerResult result = new AnyToAny().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIP));
	}

	@Test
	void shouldReturnAsHandledIfConversionIsPerformed() throws Exception {
		Src src = new Src();
		A a = new A();
		a.x = "1";
		src.a = a;
		Dst dst = new Dst();

		Field sField = Src.class.getDeclaredField("a");
		Field dField = Dst.class.getDeclaredField("a");

		FieldHandlerResult result = new AnyToAny().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

}
