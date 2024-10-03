package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.ConversionFromIterable.convertIterable;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.function.InstanceFunction;
import org.morphix.function.SimpleConverter;

/**
 * Test class for
 * {@link ConversionFromIterable#convertIterable(Iterable, InstanceFunction, SimpleConverter)}
 *
 * @author Radu Sebastian LAZIN
 */
class FromIterableWithConvertMethodTest {

	private static final String PREFIX = "X=";
	private static final int TEST_INT = 0;

	public static class C {
		int x;
		C c;

		@Override
		public String toString() {
			return x + "->" + c;
		}
	}

	public static class D {
		String x;
		D c;

		@Override
		public String toString() {
			return x + "->" + c;
		}
	}

	public static class DConverter {
		D convert(final C c) {
			return convertFrom(c, D::new, (src, dst) -> {
				dst.x = PREFIX + src.x;
			}, this::convert);
		}
	}

	public static class Src {
		C c;
	}

	public static class Dst {
		D c;
	}

	@Test
	void shouldConvertInnerObjects() {
		final int depth = 10;
		Src src = new Src();
		src.c = createC(0, depth);

		DConverter dConverter = new DConverter();

		Dst dst = convertFrom(src, Dst::new, dConverter::convert);

		D d = dst.c;
		for (int i = 0; i < depth; ++i) {
			assertThat(d.x, equalTo(PREFIX + (TEST_INT + i)));
			d = d.c;
		}
		assertThat(d.x, equalTo(PREFIX + (TEST_INT + depth)));
		assertThat(d.c, equalTo(null));
	}

	@Test
	void shouldConvertJustInnerObjects() {
		final int depth = 2;
		C c = createC(0, depth);

		DConverter dConverter = new DConverter();

		D d = convertFrom(c, D::new, dConverter::convert);

		assertThat(c.toString(), equalTo("0->1->2->null"));
		assertThat(d.toString(), equalTo("0->X=1->X=2->null"));
		assertThat(d.x, equalTo("0"));
		d = d.c;
		for (int i = 1; i < depth; ++i) {
			assertThat(d.x, equalTo(PREFIX + (TEST_INT + i)));
			d = d.c;
		}
		assertThat(d.x, equalTo(PREFIX + (TEST_INT + depth)));
		assertThat(d.c, equalTo(null));
	}

	@Test
	void shouldConvertInnerObjectsForIterables() {
		final int depth = 10;
		Src src = new Src();
		src.c = createC(0, depth);

		DConverter dConverter = new DConverter();

		List<Dst> dstList = convertIterable(List.of(src), Dst::new, dConverter::convert).toList();

		D d = dstList.get(0).c;
		for (int i = 0; i < depth; ++i) {
			assertThat(d.x, equalTo(PREFIX + (TEST_INT + i)));
			d = d.c;
		}
		assertThat(d.x, equalTo(PREFIX + (TEST_INT + depth)));
		assertThat(d.c, equalTo(null));
	}

	public static class SrcIterable {
		List<C> cees;

		public List<C> getCees() {
			return cees;
		}
	}

	public static class DstIterable {
		List<D> cees;

		public List<D> getCees() {
			return cees;
		}
	}

	@Test
	void shouldConvertInnerObjectsForIterablesInMembers() {
		final int depth = 10;
		final int size = 10;
		SrcIterable src = new SrcIterable();
		src.cees = new ArrayList<>();
		for (int i = 0; i < size; ++i) {
			src.cees.add(createC(0, depth));
		}

		DConverter dConverter = new DConverter();

		DstIterable dst = convertFrom(src, DstIterable::new, dConverter::convert);

		for (D d : dst.cees) {
			D di = d;
			for (int i = 0; i < depth; ++i) {
				assertThat(di.x, equalTo(PREFIX + (TEST_INT + i)));
				di = di.c;
			}
		}
	}

	private static C createC(final int x, final int depth) {
		C c = new C();
		c.x = TEST_INT + x;
		if (x == depth) {
			return c;
		}
		c.c = createC(x + 1, depth);
		return c;
	}

}
