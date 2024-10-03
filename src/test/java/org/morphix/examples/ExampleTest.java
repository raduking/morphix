package org.morphix.examples;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Converter example test.
 *
 * @author Radu Sebastian LAZIN
 */
class ExampleTest {

	public static class A {
		int x;
	}

	public static class B {
		String x;
	}

	public static class Source {
		Long id;
		List<A> bees;
	}

	public static class Destination {
		String id;
		List<B> bees;

		/**
		 * You have to supply a getter for iterables otherwise the type cannot
		 * be inferred.
		 *
		 * @return list of bees
		 */
		public List<B> getBees() {
			return bees;
		}
	}

	@Test
	void example() {
		Source src = new Source();
		src.id = 11L;

		A a1 = new A();
		a1.x = 17;
		A a2 = new A();
		a2.x = 13;
		src.bees = List.of(a1, a2);

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo("11"));

		assertThat(dst.bees.get(1).x, equalTo("13"));
	}
}
