package org.morphix;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.ConversionFromIterable.convertIterable;
import static org.morphix.Converted.convert;
import static org.morphix.extra.ExpandableFields.expandNone;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.annotation.Expandable;
import org.morphix.function.InstanceFunction;

/**
 * Test class for
 * {@link ConversionFromIterable#convertIterable(Iterable, InstanceFunction, List)}
 *
 * @author Radu Sebastian LAZIN
 */
class FromIterableWithExpandableTest {

	public static class SrcWithExpandable {
		@Expandable
		List<String> strings;

		@Expandable
		List<Integer> integers;

		List<Long> longs;

		List<String> getStrings() {
			return strings;
		}

		List<Integer> getIntegers() {
			return integers;
		}

		List<Long> getLongs() {
			return longs;
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

	@Test
	void shouldNotConvertIterableExpandableFields() {
		List<SrcWithExpandable> srcList = new ArrayList<>();
		int srcSize = 2;
		int size = 2;
		for (int j = 0; j < srcSize; ++j) {
			SrcWithExpandable src = new SrcWithExpandable();
			src.strings = new ArrayList<>();
			src.integers = new ArrayList<>();
			for (int i = 0; i < size; ++i) {
				src.strings.add(String.valueOf(i));
				src.integers.add(i);
			}
			srcList.add(src);
		}

		List<String> expandedFields = List.of("strings");
		List<DstWithExpandable> dstList = convertIterable(srcList, DstWithExpandable::new, expandedFields).toList();

		assertThat(dstList, hasSize(srcSize));
		for (DstWithExpandable dst : dstList) {
			assertThat(dst.strings, hasSize(size));
			assertThat(dst.integers, hasSize(0));
		}
	}

	@Test
	void shouldExpandAllFields() {
		List<SrcWithExpandable> srcList = new ArrayList<>();
		int srcSize = 2;
		int size = 2;
		for (int j = 0; j < srcSize; ++j) {
			SrcWithExpandable src = new SrcWithExpandable();
			src.strings = new ArrayList<>();
			src.integers = new ArrayList<>();
			for (int i = 0; i < size; ++i) {
				src.strings.add(String.valueOf(i));
				src.integers.add(i);
			}
			srcList.add(src);
		}

		List<DstWithExpandable> dstList = convertIterable(srcList, DstWithExpandable::new, (List<String>) null).toList();

		assertThat(dstList, hasSize(srcSize));
		for (DstWithExpandable dst : dstList) {
			assertThat(dst.strings, hasSize(size));
			assertThat(dst.integers, hasSize(size));
		}
	}

	public static class Src {
		@Expandable
		List<String> integers;

		List<String> getIntegers() {
			throw new RuntimeException("getter should not be called");
		}
	}

	public static class Dst {
		List<Integer> integers;

		List<Integer> getIntegers() {
			return integers;
		}
	}

	@Test
	void shouldNotCallGetterMethodWhenExpandableFieldIsNotExpanded() {
		Src src = new Src();
		src.integers = List.of("13", "15");

		Dst dst = convert(src).with(emptyList()).to(Dst::new);

		assertThat(dst.integers, hasSize(0));
	}

	@Test
	void shouldNotCallGetterMethodWhenExpandableFieldIsNotExpandedWithExpandableFields() {
		Src src = new Src();
		src.integers = List.of("13", "15");

		Dst dst = convert(src).with(expandNone()).to(Dst::new);

		assertThat(dst.integers, hasSize(0));
	}

	public static class SrcGetter {
		@Expandable
		List<String> getIntegers() {
			throw new RuntimeException("getter should not be called");
		}
	}

	@Test
	void shouldNotCallGetterMethodWhenExpandableFieldIsNotExpandedOnGetters() {
		SrcGetter src = new SrcGetter();

		Dst dst = convert(src).with(emptyList()).to(Dst::new);

		assertThat(dst.integers, hasSize(0));
	}

}
