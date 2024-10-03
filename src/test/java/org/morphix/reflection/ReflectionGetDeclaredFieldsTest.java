package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.reflection.predicates.MemberPredicates.withAnnotation;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields#getDeclaredFields(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionGetDeclaredFieldsTest {

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
		List<Field> fields = Fields.getDeclaredFields(B.class);

		int sizeB = B.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeB));
	}

	@Test
	void shouldReturnEmptyListForClassesWithNoFields() {
		List<Field> fields = Fields.getDeclaredFields(C.class);

		int sizeC = C.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeC));
	}

	@Test
	void shouldReturnNoFieldsForEmptyEnums() {
		List<Field> fields = Fields.getDeclaredFields(E.class);

		int sizeE = E.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeE));
	}

	@Test
	void shouldReturnFieldsForRecords() {
		List<Field> fields = Fields.getDeclaredFields(R.class);

		int sizeF = R.class.getDeclaredFields().length;

		assertThat(fields, hasSize(sizeF));
	}

	@Test
	void shouldReturnFieldsWithAnnotation() throws Exception {
		Field fx = D.class.getDeclaredField("x");

		List<Field> annotatedFields = Fields.getDeclaredFields(D.class, withAnnotation(Deprecated.class));
		assertThat(annotatedFields, hasSize(1));
		assertThat(annotatedFields.get(0), equalTo(fx));
	}

}
