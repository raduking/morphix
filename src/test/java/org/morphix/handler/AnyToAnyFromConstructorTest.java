package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.FieldHandlerResult.SKIP;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;

/**
 * Test class for {@link AnyToAnyFromConstructor}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToAnyFromConstructorTest {

	private AnyToAnyFromConstructor victim = new AnyToAnyFromConstructor();

	public static class A {
		// empty
	}

	public static class B {
		public B(@SuppressWarnings("unused") final A a) {
			// empty
		}
	}

	public static class C {
		// empty
	}

	public static class D<T> {
		public D(@SuppressWarnings("unused") final T t) {
			// empty
		}
	}

	public static class Src {
		public A a;
		public A b;
		public A c;
	}

	public static class Dst {
		public B a;
		public C b;
		public D<A> c;
	}

	@Test
	void shouldReturnTrueOnConditionWhenTheConstructorIsAvailable() throws Exception {
		ConverterField sfo = ConverterField.of(Src.class.getDeclaredField("a"));
		ConverterField dfo = ConverterField.of(Dst.class.getDeclaredField("a"));

		boolean result = victim.condition(sfo, dfo);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnConditionWhenTheConstructorIsAvailable() throws Exception {
		ConverterField sfo = ConverterField.of(Src.class.getDeclaredField("b"));
		ConverterField dfo = ConverterField.of(Dst.class.getDeclaredField("b"));

		boolean result = victim.condition(sfo, dfo);

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueOnHandleWhenConstructorIsAvailable() throws Exception {
		Src src = new Src();
		src.a = new A();
		Dst dst = new Dst();
		ConverterField sfo = ConverterField.of(Src.class.getDeclaredField("a"), src);
		ConverterField dfo = ConverterField.of(Dst.class.getDeclaredField("a"), dst);

		FieldHandlerResult result = victim.handle(sfo, dfo);

		assertThat(result, equalTo(CONVERTED));
		assertThat(dst.a, notNullValue());
	}

	@Test
	void shouldReturnFalseOnHandleWhenConstructorIsAvailable() throws Exception {
		Src src = new Src();
		src.b = new A();
		Dst dst = new Dst();
		ConverterField sfo = ConverterField.of(Src.class.getDeclaredField("b"), src);
		ConverterField dfo = ConverterField.of(Dst.class.getDeclaredField("b"), dst);

		FieldHandlerResult result = victim.handle(sfo, dfo);

		assertThat(result, equalTo(SKIP));
		assertThat(dst.a, nullValue());
	}

	@Test
	void shouldReturnFalseOnConditionWhenGenericConstructorIsAvailable() throws Exception {
		// this test needs revision when we are able to determine the exact
		// generic types of the classes involved
		ConverterField sfo = ConverterField.of(Src.class.getDeclaredField("c"));
		ConverterField dfo = ConverterField.of(Dst.class.getDeclaredField("c"));

		boolean result = victim.condition(sfo, dfo);

		assertFalse(result);
	}

}
