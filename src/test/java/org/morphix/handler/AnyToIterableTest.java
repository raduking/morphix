package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.SKIP;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

/**
 * Test class for {@link AnyToIterable}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToIterableTest {

	public static class A {
		public Integer i;
	}

	public static class B {
		public List<String> i;

		public List<String> getI() {
			return i;
		}
	}

	@Test
	void shouldReturnSkipIfSourceValueIsNull() throws Exception {
		A src = new A();
		B dst = new B();

		Field sField = A.class.getDeclaredField("i");
		Field dField = B.class.getDeclaredField("i");

		FieldHandlerResult result = new AnyToIterable().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIP));
	}

}
