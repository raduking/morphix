package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class {@link Reflection#getClassWithPrefix(Class, String)}
 *
 * @author Radu Sebastian LAZIN
 */
class ReflectionGetClassWithPrefixTest {

	public static class Int {
		// empty
	}

	public static class XInt {
		// empty
	}

	@Test
	void shouldReturnClassWithPrefix() {
		Class<XInt> xIntClass = Reflection.getClassWithPrefix(Int.class, "X");
		assertThat(xIntClass, equalTo(XInt.class));
	}

	@Test
	void shouldThrowExceptionIfClassWithPrefixDoesNotExist() {
		assertThrows(ReflectionException.class, () -> Reflection.getClassWithPrefix(Int.class, "P"));
	}
}
