package org.morphix.reflection;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.junit.jupiter.api.Test;

class AccessSetterTest {

	public static class A {

		public String foo(final String s) {
			return s;
		}

	}

	@Test
	void shouldReturnFalseWhenSetAccessibleFails() throws Exception {
		MethodHandle handle = MethodHandles.lookup().findVirtual(A.class, "foo", MethodType.methodType(String.class, String.class));

		AccessSetter<?> accessSetter = AccessSetter.ofOverride(handle);
		boolean result = accessSetter.setAccessible(null, false);

		assertFalse(result);
	}

}
