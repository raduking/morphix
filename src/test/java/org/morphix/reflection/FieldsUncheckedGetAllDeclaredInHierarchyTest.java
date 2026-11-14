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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields.Unchecked#getAllDeclaredInHierarchy(Class)}.
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

	@Test
	void shouldMatchFieldsGetAllDeclaredInHierarchyForNonNullClass() {
		assertThat(Fields.Unchecked.getAllDeclaredInHierarchy(B.class), equalTo(Fields.getAllDeclaredInHierarchy(B.class)));
		assertThat(Fields.Unchecked.getAllDeclaredInHierarchy(C.class), equalTo(Fields.getAllDeclaredInHierarchy(C.class)));
		assertThat(Fields.Unchecked.getAllDeclaredInHierarchy(E.class), equalTo(Fields.getAllDeclaredInHierarchy(E.class)));
		assertThat(Fields.Unchecked.getAllDeclaredInHierarchy(R.class), equalTo(Fields.getAllDeclaredInHierarchy(R.class)));
		assertThat(Fields.Unchecked.getAllDeclaredInHierarchy(F.class), equalTo(Fields.getAllDeclaredInHierarchy(F.class)));
		assertThat(Fields.Unchecked.getAllDeclaredInHierarchy(WithConstant.class),
				equalTo(Fields.getAllDeclaredInHierarchy(WithConstant.class)));
		assertThat(Fields.Unchecked.getAllDeclaredInHierarchy(Object.class), equalTo(Fields.getAllDeclaredInHierarchy(Object.class)));
	}

}
