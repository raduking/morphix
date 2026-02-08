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
import static org.morphix.reflection.predicates.MemberPredicates.withAnnotation;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Fields#getAllDeclared(Class)}</li>
 * <li>{@link Fields#getAllDeclared(Object)}</li>
 * <li>{@link Fields#getAllDeclared(Class, Predicate)}</li>
 *</ul>
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsGetAllDeclaredTest {

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

	public record R(int x) {
		// empty
	}

	@Test
	void shouldReturnAllFields() {
		List<Field> fields = Fields.getAllDeclared(B.class);

		int sizeB = B.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeB));
	}

	@Test
	void shouldReturnEmptyListForClassesWithNoFields() {
		List<Field> fields = Fields.getAllDeclared(C.class);

		int sizeC = C.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeC));
	}

	@Test
	void shouldReturnNoFieldsForEmptyEnums() {
		List<Field> fields = Fields.getAllDeclared(E.class);

		int sizeE = E.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeE));
	}

	@Test
	void shouldReturnFieldsForRecords() {
		List<Field> fields = Fields.getAllDeclared(R.class);

		int sizeF = R.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeF));
	}

	@Test
	void shouldReturnEmptyListForNullClass() {
		List<Field> fields = Fields.getAllDeclared((Class<?>) null);

		assertThat(fields, hasSize(0));
	}

	@Test
	void shouldReturnFieldsForObjects() {
		D d = new D();
		List<Field> fields = Fields.getAllDeclared(d);

		int sizeD = D.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeD));
	}

	@Test
	void shouldReturnAllFieldsForClassAsObject() {
		Object cls = B.class;
		List<Field> fields = Fields.getAllDeclared(cls);

		int sizeB = B.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeB));
	}

	@Test
	void shouldReturnEmptyListForNullObject() {
		List<Field> fields = Fields.getAllDeclared((Object) null);

		assertThat(fields, hasSize(0));
	}

	@Test
	void shouldReturnFieldsWithAnnotation() throws Exception {
		Field fx = D.class.getDeclaredField("x");

		List<Field> annotatedFields = Fields.getAllDeclared(D.class, withAnnotation(Deprecated.class));
		assertThat(annotatedFields, hasSize(1));
		assertThat(annotatedFields.get(0), equalTo(fx));
	}

	@Test
	void shouldEmptyListForWhenClassIsNullWithPredicate() {
		List<Field> annotatedFields = Fields.getAllDeclared(null, withAnnotation(Deprecated.class));
		assertThat(annotatedFields, hasSize(0));
	}

	@Test
	void shouldEmptyListForWhenPredicateIsNullWithPredicate() {
		List<Field> annotatedFields = Fields.getAllDeclared(B.class, null);
		assertThat(annotatedFields, hasSize(0));
	}

	@Test
	void shouldReturnFieldsWithAnnotationAndObject() throws Exception {
		Field fx = D.class.getDeclaredField("x");

		Object d = new D();
		List<Field> annotatedFields = Fields.getAllDeclared(d, withAnnotation(Deprecated.class));
		assertThat(annotatedFields, hasSize(1));
		assertThat(annotatedFields.get(0), equalTo(fx));
	}

	@Test
	void shouldReturnFieldsWithAnnotationWithObjectAsClass() throws Exception {
		Field fx = D.class.getDeclaredField("x");

		Object cls = D.class;
		List<Field> annotatedFields = Fields.getAllDeclared(cls, withAnnotation(Deprecated.class));
		assertThat(annotatedFields, hasSize(1));
		assertThat(annotatedFields.get(0), equalTo(fx));
	}

	@Test
	void shouldEmptyListForWhenClassIsNullWithPredicateAndObject() {
		List<Field> annotatedFields = Fields.getAllDeclared((Object) null, withAnnotation(Deprecated.class));
		assertThat(annotatedFields, hasSize(0));
	}

	@Test
	void shouldEmptyListForWhenPredicateIsNullWithPredicateAndObject() {
		Object cls = B.class;
		List<Field> annotatedFields = Fields.getAllDeclared(cls, null);
		assertThat(annotatedFields, hasSize(0));
	}
}
