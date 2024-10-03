package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.reflection.predicates.MemberPredicates.withAnnotation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields#getDeclaredFieldsInHierarchy(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionGetDeclaredFieldsInHierarchyTest {

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

	@Test
	void shouldReturnAllFieldsInHierarchy() {
		List<Field> fields = Fields.getDeclaredFieldsInHierarchy(B.class);

		int sizeB = B.class.getDeclaredFields().length;
		int sizeA = A.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeA + sizeB));
	}

	@Test
	void shouldReturnEmptyListForClassesWithNoFields() {
		List<Field> fields = Fields.getDeclaredFieldsInHierarchy(C.class);

		int size = C.class.getDeclaredFields().length;

		assertThat(fields, hasSize(size));
	}

	@Test
	void shouldReturnEnumClassFieldsListForEmptyEnumsToo() {
		List<Field> fields = Fields.getDeclaredFieldsInHierarchy(E.class);

		int sizeEnum = Enum.class.getDeclaredFields().length;
		int sizeE = E.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeEnum + sizeE));
	}

	@Test
	void shouldReturnFieldsWithAnnotation() throws Exception {
		Field fx = D.class.getDeclaredField("x");

		List<Field> annotatedFields = Fields.getDeclaredFieldsInHierarchy(D.class, withAnnotation(Deprecated.class));
		assertThat(annotatedFields, hasSize(1));
		assertThat(annotatedFields.get(0), equalTo(fx));
	}

	@Test
	void shouldReturnAllFieldsIncludingTheOnesWithTheSameName() {
		List<Field> result = Fields.getDeclaredFieldsInHierarchy(F.class);

		List<Field> expected = new ArrayList<>();
		expected.addAll(Fields.getDeclaredFields(F.class));
		expected.addAll(Fields.getDeclaredFields(B.class));
		expected.addAll(Fields.getDeclaredFields(A.class));

		assertThat(result, equalTo(expected));

		for (Iterator<Field> fieldsIt = result.iterator(), expectedIt = expected.iterator(); fieldsIt.hasNext();) {
			Field resultField = fieldsIt.next(), expectedField = expectedIt.next();
			assertThat(resultField, equalTo(expectedField));
		}
	}

}
