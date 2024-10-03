package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

/**
 * Test class for {@link ArrayToArray}.
 *
 * @author Radu Sebastian LAZIN
 */
class ArrayToArrayTest {

	private static final Integer TEST_INTEGER = 10;
	private static final Long TEST_LONG = TEST_INTEGER.longValue();

	public static class Src {
		Integer[] ii;
	}

	public static class Dst {
		Long[] ii;
	}

	@Test
	void shouldSkipNullValues() throws Exception {
		ArrayToArray handler = spy(new ArrayToArray());

		Src src = new Src();
		Dst dst = new Dst();
		Field sii = Src.class.getDeclaredField("ii");
		Field dii = Dst.class.getDeclaredField("ii");

		FieldHandlerResult result = handler.handle(of(sii, src), of(dii, dst));

		assertThat(result, equalTo(BREAK));
	}

	@Test
	void shouldConvertArrays() throws Exception {
		ArrayToArray handler = new ArrayToArray();

		Src src = new Src();
		src.ii = new Integer[] { TEST_INTEGER };
		Dst dst = new Dst();
		Field sii = Src.class.getDeclaredField("ii");
		Field dii = Dst.class.getDeclaredField("ii");

		FieldHandlerResult result = handler.handle(of(sii, src), of(dii, dst));

		assertThat(result, equalTo(CONVERTED));
		assertThat(dst.ii[0], equalTo(TEST_LONG));
	}

}
