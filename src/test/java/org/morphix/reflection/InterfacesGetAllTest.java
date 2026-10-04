/*
 * Copyright 2026 the original author or authors.
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Interfaces#getAll(Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class InterfacesGetAllTest {

	interface A {
		// empty
	}

	interface B {
		// empty
	}

	interface C extends A, B {
		// empty
	}

	static class NoInterfaces {
		// empty
	}

	static class ImplementsOne implements A {
		// empty
	}

	static class ImplementsTwo implements A, B {
		// empty
	}

	static class ImplementsInherited implements C {
		// empty
	}

	@SuppressWarnings("unused")
	static class Diamond implements A, B, C {
		// empty
	}

	@Test
	void shouldReturnEmptySetForClassWithoutInterfaces() {
		assertThat(Interfaces.getAll(NoInterfaces.class), empty());
	}

	@Test
	void shouldReturnDirectlyImplementedInterface() {
		assertThat(Interfaces.getAll(ImplementsOne.class), contains(A.class));
	}

	@Test
	void shouldReturnAllDirectlyImplementedInterfaces() {
		assertThat(Interfaces.getAll(ImplementsTwo.class), contains(A.class, B.class));
	}

	@Test
	void shouldReturnInheritedInterfaces() {
		assertThat(Interfaces.getAll(ImplementsInherited.class), containsInAnyOrder(A.class, B.class, C.class));
	}

	@Test
	void shouldReturnSuperinterfacesOfAnInterface() {
		Set<Class<?>> result = Interfaces.getAll(C.class);

		assertThat(result, contains(A.class, B.class));
	}

	@Test
	void shouldReturnEmptySetForInterfaceWithoutSuperinterfaces() {
		assertThat(Interfaces.getAll(A.class), empty());
	}

	@Test
	void shouldReturnTheSameInterfaceOnlyOnce() {
		Set<Class<?>> result = Interfaces.getAll(Diamond.class);

		assertThat(result.size(), equalTo(3));
		assertThat(result, containsInAnyOrder(A.class, B.class, C.class));
	}

	@Test
	void shouldReturnEmptySetForObject() {
		assertThat(Interfaces.getAll(Object.class), empty());
	}
}
