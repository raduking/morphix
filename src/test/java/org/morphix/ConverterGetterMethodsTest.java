package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.morphix.Conversion.convertFrom;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.ReflectionException;

/**
 * Tests calling of getter methods on fields if they are available in source.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterGetterMethodsTest {

	private static final Long TEST_LONG_1 = 1234321L;
	private static final Long TEST_LONG_2 = 4321234L;

	private static final String TEST_STRING_LONG_2 = "4321234";
	private static final String TEST_EXCEPTION_MESSAGE = "This is a test exception message.";

	public static class Source {
		Long id;

		public Long getId() {
			return TEST_LONG_2;
		}
	}

	public static class Destination {
		String id;
	}

	@Test
	void shouldCallGetterMethodIfAvailable() {
		Source src = new Source();
		src.id = TEST_LONG_1;

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(TEST_STRING_LONG_2));
	}

	public static class SourceEx {
		Long id;

		public Long getId() throws NoSuchMethodException {
			throw new NoSuchMethodException(TEST_EXCEPTION_MESSAGE);
		}
	}

	@Test
	void shouldEscalateExceptionIfGetterThrows() {
		SourceEx src = new SourceEx();

		assertThrows(ReflectionException.class, () -> convertFrom(src, Destination::new));
	}

	public static class SourceDerived extends Source {
		// empty
	}

	@Test
	void shouldCallGetterFromSuperclass() {
		SourceDerived src = new SourceDerived();
		src.id = TEST_LONG_1;

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(TEST_STRING_LONG_2));
	}

}
