/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.convert;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.morphix.convert.Converter.convert;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Test class for {@link ParameterizedType}.
 *
 * @author Radu Sebastian LAZIN
 */
class ParameterizedTypesTest {

	private static final int TEST_INT = 17;
	private static final String TEST_STRING = String.valueOf(TEST_INT);

	static class A<T> {

		private T t;

		public T getT() {
			return t;
		}

		public void setT(final T t) {
			this.t = t;
		}
	}

	static class B<T> {

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
		a.setT(TEST_INT);

		B<String> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t.getClass(), equalTo(String.class));

		assertThat(b.t, equalTo(TEST_STRING));
	}

	@Test
	void shouldConvertParametrizedTypes2() {
		A<Integer> a = new A<>();
		a.setT(TEST_INT);

		// this will be handled by DirectAssignment
		B<?> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t.getClass(), equalTo(Integer.class));

		assertThat(b.t, equalTo(TEST_INT));
	}

	@Test
	void shouldConvertParametrizedTypes3() {
		A<String> a = new A<>();
		a.setT(TEST_STRING);

		B<Integer> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t.getClass(), equalTo(Integer.class));

		assertThat(b.t, equalTo(TEST_INT));
	}

	@Test
	void shouldConvertParametrizedTypes4() {
		A<A<String>> a = new A<>();
		A<String> a1 = new A<>();
		a1.setT(TEST_STRING);
		a.setT(a1);

		B<B<Integer>> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t.t, notNullValue());
		assertThat(b.t.t.getClass(), equalTo(Integer.class));

		assertThat(b.t.t, equalTo(TEST_INT));
	}

	@Test
	void shouldConvertParametrizedTypes5() {
		A<List<String>> a = new A<>();
		List<String> l1 = new ArrayList<>();
		l1.add(TEST_STRING);
		a.setT(l1);

		B<List<Integer>> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t.get(0).getClass(), equalTo(Integer.class));

		assertThat(b.t.get(0), equalTo(TEST_INT));
	}

	@Test
	void shouldConvertParametrizedTypes6() {
		A<List<A<List<String>>>> a = new A<>();

		List<String> l1 = new ArrayList<>();
		l1.add(TEST_STRING);

		A<List<String>> a1 = new A<>();
		a1.setT(l1);

		List<A<List<String>>> l2 = new ArrayList<>();
		l2.add(a1);

		a.setT(l2);

		B<List<B<List<Integer>>>> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b.t.get(0).t.get(0), equalTo(TEST_INT));
	}

	@Test
	void shouldConvertParametrizedTypesLists() {
		List<String> src = List.of(TEST_STRING);

		List<Integer> dst = convert(src).to(new GenericClass<>() {
			// empty
		});

		assertThat(dst.get(0), equalTo(TEST_INT));
	}

	@Test
	void shouldConvertParameterizedTypesArraysToLists() {
		A<String[]> a = new A<>();
		String[] s1 = new String[] { TEST_STRING };
		a.setT(s1);

		B<List<Integer>> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t.get(0).getClass(), equalTo(Integer.class));

		assertThat(b.t.get(0), equalTo(TEST_INT));
	}

	@Test
	void shouldConvertParameterizedTypesListsToArrays() {
		A<List<String>> a = new A<>();
		a.setT(List.of(TEST_STRING));

		B<Integer[]> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t[0].getClass(), equalTo(Integer.class));

		assertThat(b.t[0], equalTo(TEST_INT));
	}

	@Test
	void shouldConvertParameterizedTypesListsToArrays2() {
		A<List<A<String>>> a = new A<>();

		List<A<String>> l1 = new ArrayList<>();
		A<String> a1 = new A<>();
		a1.setT(TEST_STRING);
		l1.add(a1);
		a.setT(l1);

		B<B<Integer>[]> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b.t[0].t, equalTo(TEST_INT));
	}

	@Test
	void shouldConvertParameterizedTypesListsToArrays3() {
		A<List<A<String>>> a = new A<>();
		A<String> a1 = new A<>();
		a1.setT(TEST_STRING);
		a.setT(List.of(a1));

		B<B<?>[]> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b.t[0].t, equalTo(TEST_STRING));
	}

	@Test
	void shouldConvertParameterizedTypesListsToArrays4() {
		A<List<A<String>>> a = new A<>();

		List<A<String>> l1 = new ArrayList<>();
		A<String> a1 = new A<>();
		a1.setT(TEST_STRING);
		l1.add(a1);
		a.setT(l1);

		// this will be handled by DirectAssignment
		B<B<?>[]> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b.t[0].t, equalTo(TEST_STRING));
	}

	@Test
	void shouldNotConvertParameterizedTypesNullListsToArrays() {
		A<List<A<String>>> a = new A<>();

		B<B<Integer>[]> b = convert(a).to(new GenericClass<>() {
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
		a.setT(List.of(TEST_STRING));

		D<Integer> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b, notNullValue());
		assertThat(b.t, notNullValue());
		assertThat(b.t[0].getClass(), equalTo(Integer.class));

		assertThat(b.t[0], equalTo(TEST_INT));
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
		a1.setU(TEST_STRING);
		l1.add(a1);
		a.setU(l1);

		B<B<Integer>[]> b = convert(a).to(new GenericClass<>() {
			// empty
		});

		assertThat(b.t, nullValue());
	}

	static class F<T> {

		private List<T> t;

		public List<T> getT() {
			return t;
		}

		public void setT(final List<T> t) {
			this.t = t;
		}
	}

	static class G<T> {

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
		f.setT(List.of(TEST_STRING));

		G<Integer> g = convert(f).to(new GenericClass<>() {
			// empty
		});

		assertThat(g, notNullValue());
		assertThat(g.t, notNullValue());
		assertThat(g.t.get(0).getClass(), equalTo(Integer.class));

		assertThat(g.t.get(0), equalTo(TEST_INT));
	}

}
