package org.morphix.extra;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ExcludedFields}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExcludedFieldsTest {

	@Test
	void shouldNotExcludeAnyField1() {
		ExcludedFields e = ExcludedFields.of((List<String>) null);
		String str = e.toString();

		assertThat(e, equalTo(ExcludedFields.excludeNone()));
		assertThat(str, equalTo("No excluded fields"));
	}

	@Test
	void shouldNotExcludeAnyField2() {
		ExcludedFields e = ExcludedFields.excludeNone();
		String str = e.toString();

		assertThat(e, equalTo(ExcludedFields.of((List<String>) null)));
		assertThat(str, equalTo("No excluded fields"));
	}

	@Test
	void shouldNotExcludeAnyField3() {
		ExcludedFields e = ExcludedFields.exclude((String[]) null);
		String str = e.toString();

		assertThat(e, equalTo(ExcludedFields.excludeNone()));
		assertThat(str, equalTo("No excluded fields"));
	}

	@Test
	void shouldNotExcludeAnyField4() {
		ExcludedFields e = ExcludedFields.excludeNone();
		String str = e.toString();

		assertThat(e, equalTo(ExcludedFields.exclude((String[]) null)));
		assertThat(str, equalTo("No excluded fields"));
	}

	@Test
	void shouldExcludeAllFields1() {
		ExcludedFields e = ExcludedFields.of(Collections.emptyList());
		String str = e.toString();

		assertThat(e, equalTo(ExcludedFields.excludeAll()));
		assertThat(str, equalTo("All fields are excluded"));
	}

	@Test
	void shouldExcludeAllFields2() {
		ExcludedFields e = ExcludedFields.excludeAll();
		String str = e.toString();

		assertThat(e, equalTo(ExcludedFields.of(Collections.emptyList())));
		assertThat(str, equalTo("All fields are excluded"));
	}

	@Test
	void shouldExcludeAllFields3() {
		ExcludedFields e = ExcludedFields.exclude();
		String str = e.toString();

		assertThat(e, equalTo(ExcludedFields.excludeAll()));
		assertThat(str, equalTo("All fields are excluded"));
	}

	@Test
	void shouldExcludeAllFields4() {
		ExcludedFields e = ExcludedFields.excludeAll();
		String str = e.toString();

		assertThat(e, equalTo(ExcludedFields.exclude()));
		assertThat(str, equalTo("All fields are excluded"));
	}

	@Test
	void shouldExcludeSpecificFields1() {
		ExcludedFields e = ExcludedFields.of("a");
		String str = e.toString();

		assertThat(str, equalTo("Excluded fields: [a]"));
	}

	@Test
	void shouldExcludeSpecificFields2() {
		ExcludedFields e = ExcludedFields.of(List.of("a"));
		String str = e.toString();

		assertThat(str, equalTo("Excluded fields: [a]"));
	}

	@Test
	void shouldReturnTrueOnIdentityOnEquals() {
		ExcludedFields e = ExcludedFields.exclude("a");

		assertThat(e.equals(e), equalTo(true));
	}

	@Test
	void shouldReturnFalseWhenComparingNullOnEquals() {
		ExcludedFields e = ExcludedFields.exclude("a");

		assertThat(e.equals(null), equalTo(false));
	}

	@Test
	void shouldReturnFalseWhenComparingDifferentClassOnEquals() {
		ExcludedFields e = ExcludedFields.exclude("b");

		assertThat(e.equals("b"), equalTo(false));
	}

	@Test
	void shouldReturnTrueWhenComparingEqualObjectsEquals() {
		ExcludedFields e1 = ExcludedFields.exclude("c");
		ExcludedFields e2 = ExcludedFields.exclude("c");

		assertThat(e1.equals(e2), equalTo(true));
	}

	@Test
	void shouldReturnFalseWhenComparingNotEqualObjectsEquals() {
		ExcludedFields e1 = ExcludedFields.exclude("c");
		ExcludedFields e2 = ExcludedFields.exclude("d");

		assertThat(e1.equals(e2), equalTo(false));
	}

	@Test
	void shouldBuildHashCodeWithTheInnerList() {
		List<String> names = List.of("b");
		ExcludedFields e = ExcludedFields.of(names);

		int hc = e.hashCode();

		assertThat(hc, equalTo(Objects.hash(names)));
	}

	@Test
	void shouldExcludeAllFields() {
		ExcludedFields e = ExcludedFields.excludeAll();

		boolean result = e.shouldExcludeAllFields();

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldNotExcludeAllFields1() {
		ExcludedFields e = ExcludedFields.exclude((String[]) null);

		boolean result = e.shouldExcludeAllFields();

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldNotExcludeAllFields2() {
		ExcludedFields e = ExcludedFields.exclude("a");

		boolean result = e.shouldExcludeAllFields();

		assertThat(result, equalTo(false));
	}

}
