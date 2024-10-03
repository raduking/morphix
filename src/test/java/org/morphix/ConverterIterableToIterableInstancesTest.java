package org.morphix;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.morphix.Conversion.convertFrom;

import java.io.Serial;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests conversions from {@link Iterable} to {@link Iterable}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterIterableToIterableInstancesTest {

	private static final Long TEST_LONG = 17L;
	private static final String TEST_STRING = "17";

	private static final Integer TEST_INTEGER_1 = 7;
	private static final Integer TEST_INTEGER_2 = 13;

	private static final String TEST_STRING_1 = "7";
	private static final String TEST_STRING_2 = "13";

	public static class A {
		int x;
	}

	public static class B {
		String x;

		@Override
		public boolean equals(final Object obj) {
			// basic equals implementation
			if (null == obj)
				return false;
			return Objects.equals(x, ((B) obj).x);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

	public static class Source {
		Long id;
		Collection<A> bees;
	}

	public static class Destination {
		String id;
		ArrayList<B> bees;

		public ArrayList<B> getBees() {
			return bees;
		}
	}

	@Test
	void shouldAutoConvertCollectionToArrayList() {
		Source src = new Source();
		src.id = TEST_LONG;

		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		src.bees = List.of(a1, a2);

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(TEST_STRING));

		B b1 = new B();
		b1.x = TEST_STRING_1;
		B b2 = new B();
		b2.x = TEST_STRING_2;
		List<B> expectedBees = List.of(b1, b2);

		assertThat(dst.bees, equalTo(expectedBees));
	}

	public static class Source1 {
		Long id;
		Collection<A> bees;
	}

	public static class Destination1 {
		String id;
		Set<B> bees;

		public Set<B> getBees() {
			return bees;
		}
	}

	@Test
	void shouldAutoConvertCollectionToSet() {
		Source1 src = new Source1();
		src.id = TEST_LONG;

		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		src.bees = List.of(a1, a2);

		Destination1 dst = convertFrom(src, Destination1::new);

		assertThat(dst.id, equalTo(TEST_STRING));

		B b1 = new B();
		b1.x = TEST_STRING_1;
		B b2 = new B();
		b2.x = TEST_STRING_2;

		assertThat(dst.bees, containsInAnyOrder(b1, b2));
	}

	public static class Destination2 {
		String id;
		Collection<B> bees;

		public Collection<B> getBees() {
			return bees;
		}
	}

	@Test
	void shouldAutoConvertCollectionToCollection() {
		Source1 src = new Source1();
		src.id = TEST_LONG;

		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		src.bees = List.of(a1, a2);

		Destination2 dst = convertFrom(src, Destination2::new);

		assertThat(dst.id, equalTo(TEST_STRING));

		B b1 = new B();
		b1.x = TEST_STRING_1;
		B b2 = new B();
		b2.x = TEST_STRING_2;

		assertThat(dst.bees, containsInAnyOrder(b1, b2));
	}

	public static class Destination3 {
		String id;
		AbstractList<B> bees;

		public AbstractList<B> getBees() {
			return bees;
		}
	}

	@Test
	void shouldNotAutoConvertCollectionToAbstractList() {
		Source1 src = new Source1();
		src.id = TEST_LONG;

		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		src.bees = List.of(a1, a2);

		Destination3 dst = convertFrom(src, Destination3::new);

		assertThat(dst.id, equalTo(TEST_STRING));

		assertThat(dst.bees, equalTo(null));
	}

	public static class CustomList<T> extends ArrayList<T> {
		@Serial
		private static final long serialVersionUID = -228497812386157390L;

		@SuppressWarnings("unused")
		public CustomList(final String s) {
			// empty
		}
	}

	public static class Destination4 {
		String id;
		CustomList<B> bees;

		public CustomList<B> getBees() {
			return bees;
		}
	}

	@Test
	void shouldNotAutoConvertCollectionToIterablesWithNoDefaultConstructor() {
		Source1 src = new Source1();
		src.id = TEST_LONG;

		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		src.bees = List.of(a1, a2);

		Destination4 dst = convertFrom(src, Destination4::new);

		assertThat(dst.id, equalTo(TEST_STRING));

		assertThat(dst.bees, equalTo(null));
	}

	public static class Src {
		List<String> a;

		public void setA(final List<String> a) {
			this.a = a;
		}
	}

	public static class Dst {
		List<Integer> a;

		public List<Integer> getA() {
			return a == null ? emptyList() : a;
		}
	}

	@Test
	void shouldConvertIfDestinationCollectionIsEmptyList() {
		Src src = new Src();
		src.a = singletonList("1");

		Dst dst = convertFrom(src, Dst::new);

		assertThat(dst.getA(), contains(1));
	}
}
