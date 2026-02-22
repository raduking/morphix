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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.morphix.convert.FieldHandlerContext;
import org.morphix.convert.FieldHandlerResult;

/**
 * Test class for {@link DirectAssignment}.
 *
 * @author Radu Sebastian LAZIN
 */
class DirectAssignmentTest {

	private static final Long TEST_LONG = 1234321L;
	private static final int TEST_INT = 11;

	public static class A {
		int i;
	}

	public static class Source {
		Long id;
		A a;
	}

	public static class Destination {
		Long id;
		A a;
	}

	@Test
	void shouldConvertSimpleAssignments() {
		Source src = new Source();
		src.id = TEST_LONG;
		A a = new A();
		a.i = TEST_INT;
		src.a = a;

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(TEST_LONG));
		assertThat(dst.a.i, equalTo(TEST_INT));
	}

	public static class B {
		int i;
	}

	public static class DestinationB {
		Long id;
		B a;
	}

	@Test
	void shouldNotConvertUnassignableFields() {
		Source src = new Source();
		src.id = TEST_LONG;
		A a = new A();
		a.i = TEST_INT;
		src.a = a;

		DestinationB dst = convertFrom(src, DestinationB::new);

		assertThat(dst.id, equalTo(TEST_LONG));
		// the introduction of 'any to any' conversion makes this test
		// deprecated
		//
		// assertThat(dst.a, equalTo(null));
	}

	public static class Source1 {
		List<A> ia;

		public List<A> getIa() {
			return ia;
		}
	}

	public static class Destination1 {
		List<A> ia;

		public List<A> getIa() {
			return ia;
		}
	}

	@Test
	void shouldNotDirectAssignIterables() {
		Source1 src = new Source1();
		A a = new A();
		a.i = TEST_INT;
		src.ia = List.of(a);

		Destination1 dst = convertFrom(src, Destination1::new);

		assertNotSame(dst.ia, src.ia);
	}

	public static class Source2 {
		List<A> ia;

		public List<A> getIa() {
			return ia;
		}
	}

	public static class Destination2 {
		Object ia;
	}

	@Test
	void shouldDirectAssignIterablesToObject() {
		Source2 src = new Source2();
		A a = new A();
		a.i = TEST_INT;
		src.ia = List.of(a);

		Destination2 dst = convertFrom(src, Destination2::new);

		assertNotNull(dst.ia);
	}

	@Test
	void shouldDirectAssignIterablesToObjectWithSimpleConverter() {
		Source2 src = new Source2();
		A a = new A();
		a.i = TEST_INT;
		src.ia = List.of(a);

		Destination2 dst = convertFrom(src, Destination2::new, (final String s) -> Integer.parseInt(s));

		assertNotNull(dst.ia);
	}

	@Test
	void shouldDirectAssignIterablesToObjectWithSimpleConverterAndClassSyntax() {
		Source2 src = new Source2();
		A a = new A();
		a.i = TEST_INT;
		src.ia = List.of(a);

		Destination2 dst = convertFrom(src, Destination2.class, (final String s) -> Integer.parseInt(s));

		assertNotNull(dst.ia);
	}

	public static class Source3 {
		List<A> ia;

		public List<A> getIa() {
			return ia;
		}
	}

	public static class Destination3 {
		Set<A> ia;

		public Set<A> getIa() {
			return ia;
		}
	}

	@Test
	void shouldNotDirectAssignListToSet() {
		Source3 src = new Source3();
		A a = new A();
		a.i = TEST_INT;
		src.ia = List.of(a);

		Destination3 dst = convertFrom(src, Destination3::new);

		assertTrue(dst.ia instanceof HashSet);
	}

	public static class AA {
		public Integer i;
	}

	public static class BB {
		public Integer i;
	}

	@Test
	void shouldReturnHandledIfValueIsNull() throws Exception {
		AA src = new AA();
		BB dst = new BB();

		Field sField = AA.class.getDeclaredField("i");
		Field dField = BB.class.getDeclaredField("i");

		FieldHandlerResult result = new DirectAssignment().handle(of(sField, src), of(dField, dst), new FieldHandlerContext());

		assertThat(result, equalTo(CONVERTED));
	}

}
