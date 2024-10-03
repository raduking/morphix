package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

class IterableToIterableTest {

	static class A {

		List<?> x;

		private List<String> y;

		public List<String> getY() {
			return y;
		}

	}

	static class B {

		List<String> y;

		public List<String> getY() {
			return y;
		}

	}

	@Test
	void shouldReturnHandledIfSourceIsNull() throws Exception {
		IterableToIterable handler = new IterableToIterable();

		A a = new A();
		A b = new A();

		ConverterField scf = ConverterField.of(A.class.getDeclaredField("x"), a);
		ConverterField dcf = ConverterField.of(A.class.getDeclaredField("x"), b);

		FieldHandlerResult result = handler.handle(scf, dcf);

		assertThat(result, equalTo(BREAK));
	}

	@Test
	void shouldReturnHandledIfSourceIsNotNullAndIterableElementTypeIsNull() throws Exception {
		IterableToIterable handler = new IterableToIterable();

		A a = new A();
		a.x = new ArrayList<>();
		A b = new A();

		ConverterField scf = ConverterField.of(A.class.getDeclaredField("x"), a);
		ConverterField dcf = ConverterField.of(A.class.getDeclaredField("x"), b);

		FieldHandlerResult result = handler.handle(scf, dcf);

		assertThat(result, equalTo(BREAK));
	}

	@Test
	void shouldReturnHandledIfConversionIsSuccessful() throws Exception {
		IterableToIterable handler = new IterableToIterable();

		A a = new A();
		a.y = new ArrayList<>();
		B b = new B();

		ConverterField scf = ConverterField.of(A.class.getDeclaredMethod("getY"), a);
		ConverterField dcf = ConverterField.of(B.class.getDeclaredMethod("getY"), b);

		FieldHandlerResult result = handler.handle(scf, dcf);

		assertThat(result, equalTo(CONVERTED));
	}

}
