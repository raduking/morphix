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
package org.morphix.convert.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.FieldHandlerResult.HANDLED;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.convert.annotation.Expandable;
import org.morphix.reflection.ExtendedField;

/**
 * Test class for {@link IterableToArray}.
 *
 * @author Radu Sebastian LAZIN
 */
class IterableToArrayTest {

	private static final Long TEST_LONG = 17L;
	private static final String TEST_STRING = String.valueOf(TEST_LONG);

	private static final Integer TEST_INTEGER_1 = 7;
	private static final Integer TEST_INTEGER_2 = 13;

	private static final String TEST_STRING_1 = String.valueOf(TEST_INTEGER_1);
	private static final String TEST_STRING_2 = String.valueOf(TEST_INTEGER_2);

	public static class A {
		int x;
	}

	public static class B {
		String x;

		@Override
		public boolean equals(final Object obj) {
			// basic equals implementation
			if (null == obj) {
				return false;
			}
			return Objects.equals(x, ((B) obj).x);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

	public static class Source {
		Long id;
		List<A> bees;
	}

	public static class Destination {
		String id;
		B[] bees;
	}

	@Test
	void shouldAutoConvertLists() {
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
		B[] expectedBees = new B[] { b1, b2 };

		assertThat(dst.bees, equalTo(expectedBees));
	}

	@Test
	void shouldIgnoreNullsInSource() {
		Source src = new Source();

		Destination dst = convertFrom(src, Destination::new);
		assertThat(dst.bees, equalTo(null));
	}

	public static class SrcWithExpandable {
		@Expandable
		List<String> strings;

		List<String> getStrings() {
			return strings;
		}
	}

	public static class DstWithExpandable {
		Integer[] strings;

		Integer[] getStrings() {
			return strings;
		}
	}

	@Test
	void shouldConvertExpandableFieldsIfExpandableFieldsIsNull() {
		SrcWithExpandable src = new SrcWithExpandable();
		src.strings = new ArrayList<>();
		int size = 2;
		for (int i = 0; i < size; ++i) {
			src.strings.add(String.valueOf(i));
		}

		DstWithExpandable dst = convertFrom(src, DstWithExpandable::new, (List<String>) null);

		assertThat(dst.strings.length, equalTo(size));

		for (int i = 0; i < size; ++i) {
			assertThat(dst.strings[i], equalTo(i));
		}
	}

	@Test
	void shouldNotConvertExpandableFieldsIfTheyAreSpecified() {
		SrcWithExpandable src = new SrcWithExpandable();
		src.strings = new ArrayList<>();
		int size = 2;
		for (int i = 0; i < size; ++i) {
			src.strings.add(String.valueOf(i));
		}

		List<String> expandedFields = Collections.emptyList();
		DstWithExpandable dst = convertFrom(src, DstWithExpandable::new, expandedFields);

		assertThat(dst.strings.length, equalTo(0));
	}

	@Test
	void shouldNotConvertNullValues() {
		Source src = new Source();

		Destination dst = convertFrom(src, Destination::new, Collections.emptyList());

		assertThat(dst.bees, nullValue());
	}

	static class AA {

		List<?> x;

		private List<String> y;

		public List<String> getY() {
			return y;
		}

	}

	static class BB {

		String[] y;

		public String[] getY() {
			return y;
		}

	}

	@Test
	void shouldReturnHandledIfSourceIsNull() throws Exception {
		IterableToArray handler = spy(new IterableToArray());

		AA a = new AA();
		AA b = new AA();

		ExtendedField scf = ExtendedField.of(AA.class.getDeclaredField("x"), a);
		ExtendedField dcf = ExtendedField.of(AA.class.getDeclaredField("x"), b);

		FieldHandlerResult result = handler.handle(scf, dcf);

		assertThat(result, equalTo(HANDLED));
	}

	@Test
	void shouldReturnHandledIfSourceIsNotNullAndIterableElementTypeIsNull() throws Exception {
		IterableToArray handler = new IterableToArray();

		AA a = new AA();
		a.x = new ArrayList<>();
		AA b = new AA();

		ExtendedField scf = ExtendedField.of(AA.class.getDeclaredField("x"), a);
		ExtendedField dcf = ExtendedField.of(AA.class.getDeclaredField("x"), b);

		FieldHandlerResult result = handler.handle(scf, dcf);

		assertThat(result, equalTo(HANDLED));
	}

	@Test
	void shouldReturnHandledIfDestinationHasElements() throws Exception {
		IterableToArray handler = new IterableToArray();

		AA a = new AA();
		a.x = new ArrayList<>();
		BB b = new BB();
		b.y = new String[] { "cucu" };

		ExtendedField scf = ExtendedField.of(AA.class.getDeclaredField("x"), a);
		ExtendedField dcf = ExtendedField.of(BB.class.getDeclaredField("y"), b);

		FieldHandlerResult result = handler.handle(scf, dcf);

		assertThat(result, equalTo(HANDLED));
	}

	@Test
	void shouldReturnHandledIfConversionIsSuccessful() throws Exception {
		IterableToArray handler = new IterableToArray();

		AA a = new AA();
		a.y = new ArrayList<>();
		BB b = new BB();

		ExtendedField scf = ExtendedField.of(AA.class.getDeclaredMethod("getY"), a);
		ExtendedField dcf = ExtendedField.of(BB.class.getDeclaredMethod("getY"), b);

		FieldHandlerResult result = handler.handle(scf, dcf);

		assertThat(result, equalTo(CONVERTED));
	}

}
