package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.morphix.Conversion.convertFrom;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.annotation.Expandable;

/**
 * Test class for array to array conversions.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterArrayToArrayTest {

	private static final Long TEST_LONG = 17L;
	private static final String TEST_STRING = "17";

	private static final Integer TEST_INTEGER_1 = 7;
	private static final Integer TEST_INTEGER_2 = 13;

	private static final String TEST_STRING_1 = "7";
	private static final String TEST_STRING_2 = "13";

	public static class A {
		int x;

		A(final int x) {
			this.x = x;
		}
	}

	public static class B {
		String x;
	}

	public static class Source {
		Long id;
		A[] bees;
	}

	public static class Destination {
		String id;
		B[] bees;
	}

	@Test
	void shouldConvertArrays() {
		Source src = new Source();
		src.id = TEST_LONG;
		src.bees = new A[2];
		src.bees[0] = new A(TEST_INTEGER_1);
		src.bees[1] = new A(TEST_INTEGER_2);

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(TEST_STRING));
		assertThat(dst.bees, notNullValue());
		assertThat(dst.bees[0].x, equalTo(TEST_STRING_1));
		assertThat(dst.bees[1].x, equalTo(TEST_STRING_2));
	}

	@Test
	void shouldConvertZeroLengthArrays() {
		Source src = new Source();
		src.bees = new A[] {};

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.bees, notNullValue());
		assertThat(dst.bees.length, equalTo(0));
	}

	public static class Src1 {
		A[] bees;
	}

	public static class Dst1 {
		B bees;
	}

	@Test
	void shouldNotConvertArrayToObject() {
		Src1 src = new Src1();
		src.bees = new A[2];
		src.bees[0] = new A(TEST_INTEGER_1);
		src.bees[1] = new A(TEST_INTEGER_2);

		Dst1 dst = convertFrom(src, Dst1::new);

		assertThat(dst.bees, nullValue());
	}

	@Test
	void shouldNotConvertObjectToArray() {
		Dst1 dst = new Dst1();
		dst.bees = new B();

		Src1 src = convertFrom(dst, Src1::new);

		assertThat(src.bees, nullValue());
	}

	@Test
	void shouldNotConvertNullValue() {
		Src1 src = new Src1();

		Dst1 dst = convertFrom(src, Dst1::new);

		assertThat(dst.bees, nullValue());
	}

	public static class Src2 {
		A[] bees;
	}

	public static class Dst2 {
		A[] bees;
	}

	@Test
	void shouldNotConvertArrayToArrayOfTheSameTypeDirectAssignmentShould() {
		Src2 src = new Src2();
		src.bees = new A[2];
		src.bees[0] = new A(TEST_INTEGER_1);
		src.bees[1] = new A(TEST_INTEGER_2);

		Dst2 dst = convertFrom(src, Dst2::new);

		assertThat(dst.bees, notNullValue());
		assertThat(dst.bees, equalTo(src.bees));
		assertThat(dst.bees[0].x, equalTo(TEST_INTEGER_1));
		assertThat(dst.bees[1].x, equalTo(TEST_INTEGER_2));
	}

	public static class Src3 {
		@Expandable
		A[] bees;
	}

	public static class Dst3 {
		B[] bees;
	}

	@Test
	void shouldNotConvertExpandableArraysButLeaveThemEmptyNotNull() {
		Src3 src = new Src3();
		src.bees = new A[2];
		src.bees[0] = new A(TEST_INTEGER_1);
		src.bees[1] = new A(TEST_INTEGER_2);

		Dst3 dst = convertFrom(src, Dst3::new, List.of("any"));

		assertThat(dst.bees, notNullValue());
		assertThat(dst.bees.length, equalTo(0));
	}

	@Test
	void shouldConvertExpandableArraysIfExpandableFieldsAreNull() {
		Src3 src = new Src3();
		src.bees = new A[2];
		src.bees[0] = new A(TEST_INTEGER_1);
		src.bees[1] = new A(TEST_INTEGER_2);

		Dst3 dst = convertFrom(src, Dst3::new, (List<String>) null);

		assertThat(dst.bees, notNullValue());
	}

	public static class Src4 {
		@Expandable
		A[] bees;

		@Expandable
		A[] cees;

		A[] dees;
	}

	public static class Dst4 {
		B[] bees;
		B[] cees;
		B[] dees;
	}

	@Test
	void shouldConvertExpandableArraysIfNotSpecified() {
		Src4 src = new Src4();
		src.bees = new A[2];
		src.bees[0] = new A(TEST_INTEGER_1);
		src.bees[1] = new A(TEST_INTEGER_2);
		src.cees = new A[2];
		src.cees[0] = new A(TEST_INTEGER_1);
		src.cees[1] = new A(TEST_INTEGER_2);
		src.dees = new A[2];
		src.dees[0] = new A(TEST_INTEGER_1);
		src.dees[1] = new A(TEST_INTEGER_2);

		Dst4 dst = convertFrom(src, Dst4::new, List.of("cees"));

		assertThat(dst.bees.length, equalTo(0));
		assertThat(dst.cees.length, equalTo(2));
		assertThat(dst.dees.length, equalTo(2));
	}

}
