package org.morphix;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.Conversion.convertFrom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.annotation.Expandable;
import org.morphix.extra.SimpleConverters;
import org.morphix.function.ExtraConvertFunction;
import org.morphix.function.InstanceFunction;
import org.morphix.function.SimpleConverter;

/**
 * Test class for
 * {@link Conversion#convertFrom(Object, InstanceFunction, List, SimpleConverter)}
 *
 * @author Radu Sebastian LAZIN
 */
class FromIterableWithSimpleConvertMethodAndExpandableFieldTest {

	private static final String PREFIX = "Converted ";

	public static class A {
		int x;

		A(final int x) {
			this.x = x;
		}
	}

	public static class B {
		String x;
	}

	public static class SrcWithExpandable {

		@Expandable
		List<A> expandAs;

		@Expandable
		List<A> notExpandAs;

		List<A> getExpandAs() {
			return expandAs;
		}

		List<A> getNotExpandAs() {
			return notExpandAs;
		}
	}

	public static class DstWithExpandable {

		List<B> expandAs;

		List<B> notExpandAs;

		List<B> getExpandAs() {
			return expandAs;
		}

		List<B> getNotExpandAs() {
			return notExpandAs;
		}
	}

	private static B convertAtoB(final A a) {
		B b = new B();
		b.x = PREFIX + a.x;
		return b;
	}

	@Test
	void shouldConvertExpandableFieldsAndSimpleConvertFunction() {
		SrcWithExpandable src = new SrcWithExpandable();
		src.expandAs = new ArrayList<>();
		src.notExpandAs = new ArrayList<>();
		for (int i = 0; i < 5; ++i) {
			src.expandAs.add(new A(i));
			src.notExpandAs.add(new A(i));
		}

		List<String> expandedFields = Collections.singletonList("expandAs");
		DstWithExpandable dst = convertFrom(src, DstWithExpandable::new, expandedFields,
				FromIterableWithSimpleConvertMethodAndExpandableFieldTest::convertAtoB);

		for (int i = 0; i < 5; ++i) {
			assertThat(dst.expandAs.get(i).x, equalTo(PREFIX + src.expandAs.get(i).x));
		}
		assertThat(dst.notExpandAs, hasSize(0));
	}

	@Test
	void shouldConvertWithNoExtraConvertFunctionExpandableFieldsAndSimpleConverter() {
		SrcWithExpandable src = new SrcWithExpandable();
		src.expandAs = new ArrayList<>();
		src.notExpandAs = new ArrayList<>();
		for (int i = 0; i < 5; ++i) {
			src.expandAs.add(new A(i));
			src.notExpandAs.add(new A(i));
		}

		List<String> expandedFields = Collections.singletonList("expandAs");
		DstWithExpandable dst = convertFrom(src, DstWithExpandable::new, ExtraConvertFunction.empty(), expandedFields,
				SimpleConverters.of(FromIterableWithSimpleConvertMethodAndExpandableFieldTest::convertAtoB));

		for (int i = 0; i < 5; ++i) {
			assertThat(dst.expandAs.get(i).x, equalTo(PREFIX + src.expandAs.get(i).x));
		}
		assertThat(dst.notExpandAs, hasSize(0));
	}

	@Test
	void shouldConvertAllExpandableFieldsAndSimpleConvertFunction() {
		SrcWithExpandable src = new SrcWithExpandable();
		src.expandAs = new ArrayList<>();
		src.notExpandAs = new ArrayList<>();
		for (int i = 0; i < 5; ++i) {
			src.expandAs.add(new A(i));
			src.notExpandAs.add(new A(i + 5));
		}

		List<String> expandedFields = List.of("expandAs", "notExpandAs");
		DstWithExpandable dst = convertFrom(src, DstWithExpandable::new, expandedFields,
				FromIterableWithSimpleConvertMethodAndExpandableFieldTest::convertAtoB);

		for (int i = 0; i < 5; ++i) {
			assertThat(dst.expandAs.get(i).x, equalTo(PREFIX + src.expandAs.get(i).x));
			assertThat(dst.notExpandAs.get(i).x, equalTo(PREFIX + (src.notExpandAs.get(i).x)));
		}
	}

}
