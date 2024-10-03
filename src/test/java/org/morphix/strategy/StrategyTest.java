package org.morphix.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.ConverterField;

/**
 * Test class for {@link Strategy}.
 *
 * @author Radu Sebastian LAZIN
 */
class StrategyTest {

	public static class A {
		String x;

		public String getY() {
			return x;
		}
	}

	public static class B extends A {
		int x;

		public int getX() {
			return x;
		}
	}

	public static class C {
		// empty
	}

	public static class TestStrategy implements Strategy {
		@Override
		public <T> ConverterField find(final T source, final String sourceFieldName) {
			return null;
		}
	}

	@Test
	void shouldFindAllFields() {
		TestStrategy strategy = new TestStrategy();
		A a = new A();

		List<ConverterField> fields = strategy.findAll(a);

		List<String> names = fields.stream().map(ConverterField::getName).toList();
		assertThat(names, hasSize(2));
		assertThat(names, containsInAnyOrder("x", "y"));
	}

	@Test
	void shouldFindAllFieldsInHierarchy() {
		TestStrategy strategy = new TestStrategy();
		A a = new B();

		List<ConverterField> fields = strategy.findAll(a);

		List<String> names = fields.stream().map(ConverterField::getName).toList();
		assertThat(names, hasSize(2));
		assertThat(names, containsInAnyOrder("x", "y"));
	}

	@Test
	void shouldNotFindAnyFieldsInEmptyClass() {
		TestStrategy strategy = new TestStrategy();
		C c = new C();

		List<ConverterField> fields = strategy.findAll(c);

		List<String> names = fields.stream().map(ConverterField::getName).toList();
		assertThat(names, hasSize(0));
	}

}
