package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.Conversion.convertFromIterable;
import static org.morphix.function.InstanceFunction.to;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for any to any conversions.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterAnyToAnyTest {

	public static final int TEST_INT = 11;
	public static final String TEST_STRING_1 = "aaa";
	public static final String TEST_STRING_2 = "bbb";

	public static class A {
		int x;
	}

	public static class B {
		int x;
	}

	public static class X {
		A a;
	}

	public static class Y {
		B a;
	}

	private static X createX(final int i) {
		A a = new A();
		a.x = i;
		X x = new X();
		x.a = a;
		return x;
	}

	@Test
	void shouldConvertInnerObjects() {
		X x = createX(TEST_INT);

		Y y = convertFrom(x, Y::new);

		assertThat(y.a.x, equalTo(TEST_INT));
	}

	@Test
	void shouldConvertInnerObjectsInIterables() {
		List<X> listX = new ArrayList<>();
		for (int i = 0; i < 3; ++i) {
			listX.add(createX(TEST_INT + i));
		}

		List<Y> listY = convertFromIterable(listX, Y::new);

		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i));
		}
	}

	@Test
	void shouldConvertInnerObjectsInIterablesWithConverterForExtraConversions() {
		List<X> listX = new ArrayList<>();
		for (int i = 0; i < 3; ++i) {
			listX.add(createX(TEST_INT + i));
		}

		Converter<X, Y> converter = new Converter<>() {
			@Serial
			private static final long serialVersionUID = -5878809366862342206L;

			@Override
			public void convert(final X source, final Y destination) {
				destination.a = convertFrom(source.a, B::new);
				destination.a.x++;
			}
		};

		List<Y> listY;

		listY = convertFromIterable(listX, converter::convert, Y::new);
		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}

		// equivalent construct
		listY = convertFromIterable(listX, src -> converter.convert(src, Y::new));
		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}

		// equivalent construct
		listY = convertFromIterable(listX, Y::new, (source, destination) -> {
			destination.a = convertFrom(source.a, B::new);
			destination.a.x++;
		});

		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}
	}

	@Test
	void shouldSkipNullSourceValues() {
		X x = new X();
		x.a = null;

		Y y = convertFrom(x, Y::new);

		assertThat(y.a, equalTo(null));
	}

	public static class S1 {
		Iterable<String> a;
	}

	public static class D1 {
		A a;
	}

	@Test
	void shouldSkipIterablesInSource() {
		S1 src = new S1();
		src.a = List.of(TEST_STRING_1, TEST_STRING_2);

		D1 dst = convertFrom(src, D1::new);

		assertThat(dst.a, equalTo(null));
	}

	public static class S2 {
		A a;
	}

	public static class D2 {
		Iterable<String> a;
	}

	@Test
	void shouldSkipIterablesInDestination() {
		S2 src = new S2();
		src.a = new A();

		D2 dst = convertFrom(src, D2::new);

		assertThat(dst.a, equalTo(null));
	}

	@Test
	void shouldConvertToDestinationIfDestinationHasNonNullObjects() {
		X x = new X();
		x.a = new A();
		x.a.x = TEST_INT;

		Y y = new Y();
		y.a = new B();
		y.a.x = 1;

		convertFrom(x, to(y));

		assertThat(y.a.x, equalTo(TEST_INT));
	}
}
