package org.morphix.extra;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ExpandableFields}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExpandableFieldsTest {

	@Test
	void shouldConstructNullExpandableFieldsObject() {
		ExpandableFields ef = ExpandableFields.of((String[]) null);

		boolean result = ef.equals(ExpandableFields.expandAll());

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldConstructEmptyListExpandableFieldsObject() {
		ExpandableFields ef = ExpandableFields.of();

		boolean result = ef.equals(ExpandableFields.expandNone());

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnFalseOnNullEquals() {
		ExpandableFields ef = ExpandableFields.of(Collections.singletonList("x"));

		boolean result = ef.equals(null);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseOnOtherObjectEquals() {
		ExpandableFields ef = ExpandableFields.of(Collections.singletonList("x"));
		Object mumu = "mumu";

		boolean result = ef.equals(mumu);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseOnNullEqualsWithVarargs() {
		ExpandableFields ef = ExpandableFields.of("x");

		boolean result = ef.equals(null);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseOnOtherObjectEqualsWithVarargs() {
		ExpandableFields ef = ExpandableFields.of("x");
		Object mumu = "mumu";

		boolean result = ef.equals(mumu);

		assertThat(result, equalTo(false));
	}

}
