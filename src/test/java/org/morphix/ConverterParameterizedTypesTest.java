package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.morphix.Converted.convert;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.ParameterizedClass;

/**
 * Test class for {@link ParameterizedType}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterParameterizedTypesTest {

	public static class A<T> {

		private T t;

		public T getT() {
			return t;
		}

		public void setT(final T t) {
			this.t = t;
		}
	}

	public static class B<T> {

		private T t;

		public T getT() {
			return t;
		}

		public void setT(final T t) {
			this.t = t;
		}
	}

	@Test
	void shouldConvertParametrizedTypes1() {
		A<Integer> a = new A<>();
		a.setT(13);

		B<String> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t.getClass(), equalTo(String.class));

		assertThat(b.t, equalTo("13"));
	}

	@Test
	void shouldConvertParametrizedTypes2() {
		A<Integer> a = new A<>();
		a.setT(13);

		// this will be handled by DirectAssignment
		B<?> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t.getClass(), equalTo(Integer.class));

		assertThat(b.t, equalTo(13));
	}

	@Test
	void shouldConvertParametrizedTypes3() {
		A<String> a = new A<>();
		a.setT("13");

		B<Integer> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t.getClass(), equalTo(Integer.class));

		assertThat(b.t, equalTo(13));
	}

	@Test
	void shouldConvertParametrizedTypes4() {
		A<A<String>> a = new A<>();
		A<String> a1 = new A<>();
		a1.setT("13");
		a.setT(a1);

		B<B<Integer>> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t.t, notNullValue());
		assertThat(b.t.t.getClass(), equalTo(Integer.class));

		assertThat(b.t.t, equalTo(13));
	}

	@Test
	void shouldConvertParametrizedTypes5() {
		A<List<String>> a = new A<>();
		List<String> l1 = new ArrayList<>();
		l1.add("13");
		a.setT(l1);

		B<List<Integer>> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t.get(0).getClass(), equalTo(Integer.class));

		assertThat(b.t.get(0), equalTo(13));
	}

	@Test
	void shouldConvertParametrizedTypes6() {
		A<List<A<List<String>>>> a = new A<>();

		List<String> l1 = new ArrayList<>();
		l1.add("13");

		A<List<String>> a1 = new A<>();
		a1.setT(l1);

		List<A<List<String>>> l2 = new ArrayList<>();
		l2.add(a1);

		a.setT(l2);

		B<List<B<List<Integer>>>> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b.t.get(0).t.get(0), equalTo(13));
	}

	@Test
	void shouldConvertParametrizedTypesLists() {
		List<String> src = List.of("13");

		List<Integer> dst = convert(src).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(dst.get(0), equalTo(13));
	}

	@Test
	void shouldConvertParameterizedTypesArraysToLists() {
		A<String[]> a = new A<>();
		String[] s1 = new String[] { "13" };
		a.setT(s1);

		B<List<Integer>> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t.get(0).getClass(), equalTo(Integer.class));

		assertThat(b.t.get(0), equalTo(13));
	}

	@Test
	void shouldConvertParameterizedTypesListsToArrays() {
		A<List<String>> a = new A<>();
		a.setT(List.of("13"));

		B<Integer[]> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t[0].getClass(), equalTo(Integer.class));

		assertThat(b.t[0], equalTo(13));
	}

	@Test
	void shouldConvertParameterizedTypesListsToArrays2() {
		A<List<A<String>>> a = new A<>();

		List<A<String>> l1 = new ArrayList<>();
		A<String> a1 = new A<>();
		a1.setT("13");
		l1.add(a1);
		a.setT(l1);

		B<B<Integer>[]> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b.t[0].t, equalTo(13));
	}

	@Test
	void shouldConvertParameterizedTypesListsToArrays3() {
		A<List<A<String>>> a = new A<>();
		A<String> a1 = new A<>();
		a1.setT("13");
		a.setT(List.of(a1));

		B<B<?>[]> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b.t[0].t, equalTo("13"));
	}

	@Test
	void shouldConvertParameterizedTypesListsToArrays4() {
		A<List<A<String>>> a = new A<>();

		List<A<String>> l1 = new ArrayList<>();
		A<String> a1 = new A<>();
		a1.setT("13");
		l1.add(a1);
		a.setT(l1);

		// this will be handled by DirectAssignment
		B<B<?>[]> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b.t[0].t, equalTo("13"));
	}

	@Test
	void shouldNotConvertParameterizedTypesNullListsToArrays() {
		A<List<A<String>>> a = new A<>();

		B<B<Integer>[]> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b.t, nullValue());
	}

	public static class C<T> {

		private List<T> t;

		public List<T> getT() {
			return t;
		}

		public void setT(final List<T> t) {
			this.t = t;
		}
	}

	public static class D<T> {

		private T[] t;

		public T[] getT() {
			return t;
		}

		public void setT(final T[] t) {
			this.t = t;
		}
	}

	@Test
	void shouldConvertParameterizedTypesListsToArraysMembers() {
		C<String> a = new C<>();
		a.setT(List.of("13"));

		D<Integer> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t[0].getClass(), equalTo(Integer.class));

		assertThat(b.t[0], equalTo(13));
	}

	public static class E<T> {

		private T u;

		public T getU() {
			return u;
		}

		public void setU(final T u) {
			this.u = u;
		}
	}

	@Test
	void shouldNotConvertParameterizedTypesForUnknownSourceListsToArrays() {
		E<List<E<String>>> a = new E<>();

		List<E<String>> l1 = new ArrayList<>();
		E<String> a1 = new E<>();
		a1.setU("13");
		l1.add(a1);
		a.setU(l1);

		B<B<Integer>[]> b = convert(a).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(b.t, nullValue());
	}

	public static class F<T> {

		private List<T> t;

		public List<T> getT() {
			return t;
		}

		public void setT(final List<T> t) {
			this.t = t;
		}
	}

	public static class G<T> {

		private List<T> t;

		public List<T> getT() {
			return t;
		}

		public void setT(final List<T> t) {
			this.t = t;
		}
	}

	@Test
	void shouldConvertParameterizedTypesListsToListsMembers() {
		F<String> f = new F<>();
		f.setT(List.of("13"));

		G<Integer> g = convert(f).to(new ParameterizedClass<>() {
			// empty
		});

		assertThat(g, notNullValue());
		assertThat(g.t, notNullValue());
		assertThat(g.t.get(0).getClass(), equalTo(Integer.class));

		assertThat(g.t.get(0), equalTo(13));
	}

}
