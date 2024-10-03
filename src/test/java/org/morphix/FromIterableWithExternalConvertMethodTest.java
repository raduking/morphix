package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.ConversionFromIterable.convertIterable;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.function.ConverterWithInstance;
import org.morphix.function.ExtraConvertFunction;
import org.morphix.function.InstanceFunction;
import org.morphix.function.SimpleConverter;

/**
 * Test class for
 * {@link ConversionFromIterable#convertIterable(Iterable, ConverterWithInstance, InstanceFunction)}
 * {@link ConversionFromIterable#convertIterable(Iterable, SimpleConverter)}
 * {@link ConversionFromIterable#convertIterable(Iterable, InstanceFunction, ExtraConvertFunction)}
 *
 * @author Radu Sebastian LAZIN
 */
class FromIterableWithExternalConvertMethodTest {

	public static final int TEST_INT = 11;

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
	void shouldConvertInnerObjectsInIterablesWithConverterForExtraConversions() {
		List<X> listX = new ArrayList<>();
		for (int i = 0; i < 3; ++i) {
			listX.add(createX(TEST_INT + i));
		}

		Converter<X, Y> converter = new Converter<>() {
			@Serial
			private static final long serialVersionUID = -1094032837101504098L;

			@Override
			public void convert(final X source, final Y destination) {
				destination.a = convertFrom(source.a, B::new);
				destination.a.x++;
			}
		};

		List<Y> listY;

		listY = convertIterable(listX, converter::convert, Y::new).toList();
		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}

		// equivalent construct
		listY = convertIterable(listX, src -> converter.convert(src, Y::new)).toList();
		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}

		// equivalent construct
		listY = convertIterable(listX, Y::new, (source, destination) -> {
			destination.a = convertFrom(source.a, B::new);
			destination.a.x++;
		}).toList();

		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}
	}

}
