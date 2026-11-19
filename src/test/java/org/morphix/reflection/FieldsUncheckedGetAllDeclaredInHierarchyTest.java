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
package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.morphix.reflection.predicates.MemberPredicates.withAnnotation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.morphix.lang.function.Predicates;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Fields.Unchecked#getAllDeclaredInHierarchy(Class)}</li>
 * <li>{@link Fields.Unchecked#getAllDeclaredInHierarchy(Class, Predicate)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsUncheckedGetAllDeclaredInHierarchyTest {

	public enum E {
		// empty enum
	}

	public static class A {
		int x;
	}

	public static class B extends A {
		int y;
	}

	public static class C {
		// empty class
	}

	public static class D {
		@Deprecated
		int x;

		int y;
	}

	public static class F extends B {
		int x;
		int y;
	}

	public interface WithConstant {
		int CONSTANT = 13;
	}

	public record R(int x) {
		// record class
	}

	@Test
	void shouldReturnAllFieldsInHierarchy() {
		List<Field> fields = Fields.Unchecked.getAllDeclaredInHierarchy(B.class);

		int sizeB = B.class.getDeclaredFields().length;
		int sizeA = A.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeA + sizeB));
	}

	@Test
	void shouldReturnFieldsForClassesWithNoFields() {
		List<Field> fields = Fields.Unchecked.getAllDeclaredInHierarchy(C.class);

		assertThat(fields, hasSize(C.class.getDeclaredFields().length));
	}

	@Test
	void shouldReturnEnumClassFieldsListForEmptyEnumsToo() {
		List<Field> fields = Fields.Unchecked.getAllDeclaredInHierarchy(E.class);

		int sizeEnum = Enum.class.getDeclaredFields().length;
		int sizeE = E.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeEnum + sizeE));
	}

	@Test
	void shouldReturnFieldsForRecords() {
		List<Field> fields = Fields.Unchecked.getAllDeclaredInHierarchy(R.class);

		int sizeRecord = Record.class.getDeclaredFields().length;
		int sizeR = R.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeRecord + sizeR));
	}

	@Test
	void shouldThrowNullPointerExceptionForNullClass() {
		assertThrows(NullPointerException.class, () -> Fields.Unchecked.getAllDeclaredInHierarchy((Class<?>) null));
	}

	@Test
	void shouldReturnAllFieldsIncludingTheOnesWithTheSameName() {
		List<Field> result = Fields.Unchecked.getAllDeclaredInHierarchy(F.class);

		List<Field> expected = new ArrayList<>();
		expected.addAll(Fields.Unchecked.getAllDeclared(F.class));
		expected.addAll(Fields.Unchecked.getAllDeclared(B.class));
		expected.addAll(Fields.Unchecked.getAllDeclared(A.class));

		assertThat(result, equalTo(expected));

		for (Iterator<Field> fieldsIt = result.iterator(), expectedIt = expected.iterator(); fieldsIt.hasNext();) {
			Field resultField = fieldsIt.next();
			Field expectedField = expectedIt.next();
			assertThat(resultField, equalTo(expectedField));
		}
	}

	@Test
	void shouldReturnTheOwnFieldsOfAnInterface() {
		List<Field> fields = Fields.Unchecked.getAllDeclaredInHierarchy(WithConstant.class);

		assertThat(fields, hasSize(WithConstant.class.getDeclaredFields().length));
		assertThat(fields.get(0).getName(), equalTo("CONSTANT"));
	}

	@Test
	void shouldReturnNoFieldsForObjectItself() {
		List<Field> fields = Fields.Unchecked.getAllDeclaredInHierarchy(Object.class);

		assertThat(fields, hasSize(0));
	}

	@ParameterizedTest
	@MethodSource("provideClassesForCheckingNonNullClass")
	void shouldMatchFieldsGetAllDeclaredInHierarchyForNonNullClass(final Class<?> cls) {
		assertThat(Fields.Unchecked.getAllDeclaredInHierarchy(cls), equalTo(Fields.getAllDeclaredInHierarchy(cls)));
	}

	private static Stream<Arguments> provideClassesForCheckingNonNullClass() {
		return Stream.of(
				Arguments.of(B.class),
				Arguments.of(C.class),
				Arguments.of(E.class),
				Arguments.of(R.class),
				Arguments.of(F.class),
				Arguments.of(WithConstant.class),
				Arguments.of(Object.class));
	}

	@Test
	void shouldReturnFieldsWithAnnotation() throws Exception {
		Field fx = D.class.getDeclaredField("x");

		List<Field> annotatedFields = Fields.Unchecked.getAllDeclaredInHierarchy(D.class, withAnnotation(Deprecated.class));
		assertThat(annotatedFields, hasSize(1));
		assertThat(annotatedFields.get(0), equalTo(fx));
	}

	@Test
	void shouldReturnTheOwnFieldsOfAnInterfaceWithPredicate() {
		List<Field> fields = Fields.Unchecked.getAllDeclaredInHierarchy(WithConstant.class, field -> true);

		assertThat(fields, hasSize(WithConstant.class.getDeclaredFields().length));
		assertThat(fields.get(0).getName(), equalTo("CONSTANT"));
	}

	@Test
	void shouldThrowNullPointerExceptionForNullPredicate() {
		assertThrows(NullPointerException.class, () -> Fields.Unchecked.getAllDeclaredInHierarchy(D.class, null));
	}

	@Test
	void shouldThrowNullPointerExceptionForNullClassWithPredicate() {
		assertThrows(NullPointerException.class, () -> Fields.Unchecked.getAllDeclaredInHierarchy((Class<?>) null, field -> true));
	}

	@Test
	void shouldApplyPredicateWhenTheClassHasNoSuperclass() {
		List<Field> all = Fields.Unchecked.getAllDeclaredInHierarchy(WithConstant.class);
		List<Field> none = Fields.Unchecked.getAllDeclaredInHierarchy(WithConstant.class, field -> false);

		assertThat(none, hasSize(0));
		assertThat(all, hasSize(WithConstant.class.getDeclaredFields().length));
	}

	@Test
	void shouldMatchFieldsGetAllDeclaredInHierarchyWithPredicate() {
		Predicate<Field> predicate = withAnnotation(Deprecated.class);

		assertThat(Fields.Unchecked.getAllDeclaredInHierarchy(D.class, predicate),
				equalTo(Fields.getAllDeclaredInHierarchy(D.class, predicate)));
	}

	@ParameterizedTest
	@MethodSource("provideClassesForCheckingWithPredicate")
	void shouldMatchFieldsGetAllDeclaredInHierarchyWithPredicate(final Class<?> cls) {
		assertThat(Fields.Unchecked.getAllDeclaredInHierarchy(cls, Predicates.acceptAll()),
				equalTo(Fields.getAllDeclaredInHierarchy(cls, Predicates.acceptAll())));
	}

	private static Stream<Arguments> provideClassesForCheckingWithPredicate() {
		return Stream.of(
				Arguments.of(B.class),
				Arguments.of(WithConstant.class),
				Arguments.of(Object.class));
	}

	static class AA {

		public int x;
		public String s;
		public Long l;
		public char c;
		public Boolean b;
	}

	static class BB extends AA {

		public int x2;
		public String s2;
		public Long l2;
		public char c2;
		public Boolean b2;
	}

	static class CC extends BB {

		public int x3;
		public String s3;
		public Long l3;
		public char c3;
		public Boolean b3;
	}

	static class DD extends CC {

		public int x4;
		public String s4;
		public Long l4;
		public char c4;
		public Boolean b4;
	}

	@Test
	void shouldCorrectlyTraverseABigHierarchy() {
		List<Field> fields = Fields.Unchecked.getAllDeclaredInHierarchy(DD.class);

		int sizeAA = AA.class.getDeclaredFields().length;
		int sizeBB = BB.class.getDeclaredFields().length;
		int sizeCC = CC.class.getDeclaredFields().length;
		int sizeDD = DD.class.getDeclaredFields().length;

		List<Field> expected = new ArrayList<>();
		expected.addAll(Fields.Unchecked.getAllDeclared(DD.class));
		expected.addAll(Fields.Unchecked.getAllDeclared(CC.class));
		expected.addAll(Fields.Unchecked.getAllDeclared(BB.class));
		expected.addAll(Fields.Unchecked.getAllDeclared(AA.class));

		assertThat(fields, hasSize(sizeAA + sizeBB + sizeCC + sizeDD));
		assertThat(fields, equalTo(expected));
	}
}
