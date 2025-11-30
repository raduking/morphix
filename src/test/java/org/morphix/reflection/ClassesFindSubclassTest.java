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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class {@link Classes#findSubclass(Class, Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClassesFindSubclassTest {

	static class A {
		// empty
	}

	static class B extends A {
		// empty
	}

	static class C extends B {
		// empty
	}

	@Test
	void shouldFindSubclassForTheGivenChildAndExpectedParent() {
		Class<?> child = Classes.findSubclass(A.class, C.class);
		assertThat(child, equalTo(B.class));
	}

	@Test
	void shouldThrowReflectionExceptionIfTheParentIsObject() {
		ReflectionException e = assertThrows(ReflectionException.class, () -> Classes.findSubclass(String.class, A.class));
		assertThat(e.getMessage(), equalTo("The parent of " + A.class.getCanonicalName() + " is not a " + String.class.getCanonicalName()));
	}

}
