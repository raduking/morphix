package org.morphix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.morphix.Conversion.convertFrom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.morphix.annotation.Expandable;
import org.morphix.function.ExtraConvertFunction;
import org.morphix.function.InstanceFunction;

/**
 * Test class for
 * {@link Conversion#convertFrom(Object, InstanceFunction, ExtraConvertFunction, List)}
 *
 * @author Doru Muresan
 */
class FromIterableWithExtraConvertFunctionAndExpandableFieldTest {

	private static final String PREFIX = "Converted ";

	private SrcWithExpandable src;

	@BeforeEach
	public void setUp() {
		src = new SrcWithExpandable();

		src.setStrings(new ArrayList<>());
		src.getStrings().add(String.valueOf(1234));
		src.getStrings().add(String.valueOf(1235));

		src.setIntegers(new ArrayList<>());
		src.getIntegers().add(4321);
		src.getIntegers().add(4322);

		src.setLongs(new ArrayList<>());
		src.getLongs().add(23L);
	}

	@Test
	void shouldConvertExpandableFieldsAndExtraConvertFunction() {
		List<String> expandedFields = Collections.singletonList("integers");
		DstWithExpandable dst = convertFrom(src, DstWithExpandable::new, (final SrcWithExpandable s, final DstWithExpandable d) -> {
			for (Integer integerValue : s.getIntegers()) {
				d.getIntegers().add(PREFIX + integerValue);
			}
			for (String stringValue : s.getStrings()) {
				d.getStrings().add(Integer.parseInt(stringValue) + 1000);
			}
		}, expandedFields);

		assertNotNull(dst);
		assertEquals(4, dst.getIntegers().size());
		assertEquals(2, dst.getStrings().size());
		assertEquals(1, dst.getLongs().size());

		long convertedWithExtraFunctionCount = dst.getIntegers().stream().filter(e -> e.startsWith(PREFIX)).count();
		assertEquals(2, convertedWithExtraFunctionCount);
	}

	@Test
	void shouldConvertWithNoExpandableFields() {
		DstWithExpandable dst = convertFrom(src, DstWithExpandable::new, (final SrcWithExpandable s, final DstWithExpandable d) -> {
			for (Long longValue : s.getLongs()) {
				d.getLongs().add(PREFIX + longValue);
			}
		}, (List<String>) null);

		assertNotNull(dst);
		assertEquals(2, dst.getIntegers().size());
		assertEquals(2, dst.getStrings().size());
		assertEquals(2, dst.getLongs().size());

		long convertedWithExtraFunctionCount = dst.getLongs().stream().filter(e -> e.startsWith(PREFIX)).count();
		assertEquals(1, convertedWithExtraFunctionCount);
	}

	public static class SrcWithExpandable {

		@Expandable
		List<String> strings;

		@Expandable
		List<Integer> integers;

		List<Long> longs;

		List<String> getStrings() {
			return strings;
		}

		void setStrings(final List<String> strings) {
			this.strings = strings;
		}

		List<Integer> getIntegers() {
			return integers;
		}

		void setIntegers(final List<Integer> integers) {
			this.integers = integers;
		}

		List<Long> getLongs() {
			return longs;
		}

		void setLongs(final List<Long> longs) {
			this.longs = longs;
		}
	}

	public static class DstWithExpandable {

		List<Integer> strings;

		List<String> integers;

		List<String> longs;

		List<Integer> getStrings() {
			return strings;
		}

		List<String> getIntegers() {
			return integers;
		}

		List<String> getLongs() {
			return longs;
		}
	}

}
