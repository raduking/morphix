package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.morphix.Conversion.convertFrom;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.handler.PrimitiveAssignment;

/**
 * Tests conversions of primitives to their respective java classes.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterPrimitivesTest {

	private static final int TEST_PRIMITIVE_INT = 15;
	private static final Integer TEST_INTEGER = Integer.valueOf(TEST_PRIMITIVE_INT);

	private static final long TEST_PRIMITIVE_LONG = 17L;
	private static final Long TEST_LONG = Long.valueOf(TEST_PRIMITIVE_LONG);

	private static final char TEST_PRIMITIVE_CHAR = 'x';
	private static final Character TEST_CHARACTER = Character.valueOf(TEST_PRIMITIVE_CHAR);

	private static final short TEST_PRIMITIVE_SHORT = 13;
	private static final Short TEST_SHORT = Short.valueOf(TEST_PRIMITIVE_SHORT);

	private static final byte TEST_PRIMITIVE_BYTE = 123;
	private static final Byte TEST_BYTE = Byte.valueOf(TEST_PRIMITIVE_BYTE);

	private static final boolean TEST_PRIMITIVE_BOOLEAN = true;
	private static final Boolean TEST_BOOLEAN = Boolean.valueOf(TEST_PRIMITIVE_BOOLEAN);

	private static final float TEST_PRIMITIVE_FLOAT = 2.3f;
	private static final Float TEST_FLOAT = Float.valueOf(TEST_PRIMITIVE_FLOAT);

	private static final double TEST_PRIMITIVE_DOUBLE = 2.5;
	private static final Double TEST_DOUBLE = Double.valueOf(TEST_PRIMITIVE_DOUBLE);

	public static class A {
		int testInt;
		long testLong;
		char testChar;
		short testShort;
		byte testByte;
		boolean testBoolean;
		float testFloat;
		double testDouble;
	}

	public static class B {
		Integer testInt;
		Long testLong;
		Character testChar;
		Short testShort;
		Byte testByte;
		Boolean testBoolean;
		Float testFloat;
		Double testDouble;
	}

	@Test
	void shouldConvertPrimitiveFieldsToNonPrimitives() {
		A src = new A();
		src.testInt = TEST_PRIMITIVE_INT;
		src.testLong = TEST_PRIMITIVE_LONG;
		src.testChar = TEST_PRIMITIVE_CHAR;
		src.testShort = TEST_PRIMITIVE_SHORT;
		src.testByte = TEST_PRIMITIVE_BYTE;
		src.testBoolean = TEST_PRIMITIVE_BOOLEAN;
		src.testFloat = TEST_PRIMITIVE_FLOAT;
		src.testDouble = TEST_PRIMITIVE_DOUBLE;

		B dst = convertFrom(src, B::new);

		assertThat(dst.testInt, equalTo(TEST_INTEGER));
		assertThat(dst.testLong, equalTo(TEST_LONG));
		assertThat(dst.testChar, equalTo(TEST_CHARACTER));
		assertThat(dst.testShort, equalTo(TEST_SHORT));
		assertThat(dst.testByte, equalTo(TEST_BYTE));
		assertThat(dst.testBoolean, equalTo(TEST_BOOLEAN));
		assertThat(dst.testFloat, equalTo(TEST_FLOAT));
		assertThat(dst.testDouble, equalTo(TEST_DOUBLE));
	}

	@Test
	void shouldConvertNonPrimitivesToPrimitives() {
		B src = new B();
		src.testInt = TEST_INTEGER;
		src.testLong = TEST_LONG;
		src.testChar = TEST_CHARACTER;
		src.testShort = TEST_SHORT;
		src.testByte = TEST_BYTE;
		src.testBoolean = TEST_BOOLEAN;
		src.testFloat = TEST_FLOAT;
		src.testDouble = TEST_DOUBLE;

		A dst = convertFrom(src, A::new);

		assertThat(dst.testInt, equalTo(TEST_PRIMITIVE_INT));
		assertThat(dst.testLong, equalTo(TEST_PRIMITIVE_LONG));
		assertThat(dst.testChar, equalTo(TEST_PRIMITIVE_CHAR));
		assertThat(dst.testShort, equalTo(TEST_PRIMITIVE_SHORT));
		assertThat(dst.testByte, equalTo(TEST_PRIMITIVE_BYTE));
		assertThat(dst.testBoolean, equalTo(TEST_PRIMITIVE_BOOLEAN));
		assertThat(dst.testFloat, equalTo(TEST_PRIMITIVE_FLOAT));
		assertThat(dst.testDouble, equalTo(TEST_PRIMITIVE_DOUBLE));
	}

	@Test
	void shouldNotConvertNullsToPrimitives() {
		Converter<B, A> converter = spy(ConverterBuilder.newConverter(Configuration.defaultConfiguration()));
		PrimitiveAssignment primitiveAssignment = new PrimitiveAssignment();
		doReturn(List.of(primitiveAssignment)).when(converter).getFieldHandlers();

		B source = new B();
		A dst = converter.convert(source, A::new);

		assertThat(dst.testInt, equalTo(0));
		assertThat(dst.testLong, equalTo(0L));
		assertThat(dst.testChar, equalTo((char) 0));
		assertThat(dst.testShort, equalTo((short) 0));
		assertThat(dst.testByte, equalTo((byte) 0));
		assertThat(dst.testBoolean, equalTo(false));
		assertThat(dst.testFloat, equalTo(0.0f));
		assertThat(dst.testDouble, equalTo(0.0d));
	}
}
