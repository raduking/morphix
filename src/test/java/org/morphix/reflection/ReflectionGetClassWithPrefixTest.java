/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class {@link Reflection#getClassWithPrefix(Class, String)}.
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
		ReflectionException e = assertThrows(ReflectionException.class, () -> Reflection.getClassWithPrefix(Int.class, "P"));
		assertThat(e.getMessage(),
				equalTo("Could not find class with prefix '" + ReflectionGetClassWithPrefixTest.class.getCanonicalName() + "$PInt'"));
	}
}
