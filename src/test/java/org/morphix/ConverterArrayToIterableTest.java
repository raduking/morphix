package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.Conversion.convertFrom;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.morphix.annotation.Expandable;

/**
 * Tests conversions from array to {@link Iterable}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterArrayToIterableTest {

	private static final Long TEST_LONG = 17L;
	private static final String TEST_STRING = "17";

	private static final Integer TEST_INTEGER_1 = 7;
	private static final Integer TEST_INTEGER_2 = 13;

	private static final String TEST_STRING_1 = "7";
	private static final String TEST_STRING_2 = "13";

	public static class A {
		int x;
	}

	public static class B {
		String x;

		@Override
		public boolean equals(final Object obj) {
			// basic equals implementation
			if (null == obj)
				return false;
			return Objects.equals(x, ((B) obj).x);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

	public static class Source {
		Long id;
		A[] bees;
	}

	public static class Destination {
		String id;
		List<B> bees;

		public List<B> getBees() {
			return bees;
		}
	}

	@Test
	void shouldAutoConvertLists() {
		Source src = new Source();
		src.id = TEST_LONG;

		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		src.bees = new A[] { a1, a2 };

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(TEST_STRING));

		B b1 = new B();
		b1.x = TEST_STRING_1;
		B b2 = new B();
		b2.x = TEST_STRING_2;
		List<B> expectedBees = List.of(b1, b2);

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
		String[] strings;

		String[] getStrings() {
			return strings;
		}
	}

	public static class DstWithExpandable {
		List<Integer> strings;

		List<Integer> getStrings() {
			return strings;
		}
	}

	@Test
	void shouldConvertExpandableFieldsIfExpandableFieldsIsNull() {
		SrcWithExpandable src = new SrcWithExpandable();
		int size = 2;
		src.strings = new String[2];
		for (int i = 0; i < size; ++i) {
			src.strings[i] = String.valueOf(i);
		}

		DstWithExpandable dst = convertFrom(src, DstWithExpandable::new, (List<String>) null);

		assertThat(dst.strings, hasSize(size));

		for (int i = 0; i < size; ++i) {
			assertThat(dst.strings.get(i), equalTo(i));
		}
	}

	@Test
	void shouldNotConvertExpandableFieldsIfTheyAreSpecified() {
		SrcWithExpandable src = new SrcWithExpandable();
		int size = 2;
		src.strings = new String[2];
		for (int i = 0; i < size; ++i) {
			src.strings[i] = String.valueOf(i);
		}

		List<String> expandedFields = Collections.emptyList();
		DstWithExpandable dst = convertFrom(src, DstWithExpandable::new, expandedFields);

		assertThat(dst.strings, hasSize(0));
	}

	@Test
	void shouldNotConvertNullValues() {
		Source src = new Source();

		Destination dst = convertFrom(src, Destination::new, Collections.emptyList());

		assertThat(dst.bees, nullValue());
	}

	public static class DestinationNoGetter {
		List<B> bees;
	}

	@Test
	void shouldNotConvertIterableIfDestinationDoesNotHaveGetterMethodOnList() {
		Source src = new Source();

		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		src.bees = new A[] { a1, a2 };

		DestinationNoGetter dst = convertFrom(src, DestinationNoGetter::new);

		assertThat(dst.bees, equalTo(null));
	}

}
