package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

/**
 * Test class for {@link AnyFromMap}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyFromMapTest {

	@Test
	void shouldReturnConvertedOnHandleIfSourceIsNull() {
		FieldHandlerResult result = new AnyFromMap().handle(of((Field) null), of((Field) null));

		assertThat(result, equalTo(CONVERTED));
	}

}
