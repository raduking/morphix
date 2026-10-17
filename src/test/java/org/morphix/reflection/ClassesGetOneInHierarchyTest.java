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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.morphix.reflection.predicates.ClassPredicates.isClass;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Classes#getOneInHierarchy(Class, java.util.function.Predicate)}</li>
 * <li>{@link Classes#getOneInHierarchy(Object, java.util.function.Predicate)}</li>
 * <li>{@link Classes#getOneInHierarchy(Class, String)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class ClassesGetOneInHierarchyTest {

	public static class A {
		// empty class
	}

	public static class B extends A {
		// empty class
	}

	public static class C extends B {
		// empty class
	}

	@Test
	void shouldReturnTheClassItselfWhenItMatches() {
		Class<?> cls = Classes.getOneInHierarchy(C.class, isClass(C.class));

		assertThat(cls, equalTo(C.class));
	}

	@Test
	void shouldReturnTheSuperclassWhenItMatches() {
		Class<?> cls = Classes.getOneInHierarchy(C.class, isClass(A.class));

		assertThat(cls, equalTo(A.class));
	}

	@Test
	void shouldReturnObjectClassWhenItMatches() {
		Class<?> cls = Classes.getOneInHierarchy(C.class, isClass(Object.class));

		assertThat(cls, equalTo(Object.class));
	}

	@Test
	void shouldReturnNullWhenNoClassMatches() {
		Class<?> cls = Classes.getOneInHierarchy(C.class, isClass(String.class));

		assertThat(cls, nullValue());
	}

	@Test
	void shouldReturnNullWhenClassIsNull() {
		Class<?> cls = Classes.getOneInHierarchy((Class<?>) null, isClass(A.class));

		assertThat(cls, nullValue());
	}

	@Test
	void shouldReturnNullWhenPredicateIsNull() {
		Predicate<Class<?>> predicate = null;

		Class<?> cls = Classes.getOneInHierarchy(C.class, predicate);

		assertThat(cls, nullValue());
	}

	@Test
	void shouldReturnNullForAnInterfaceWhenNoClassMatches() {
		Class<?> cls = Classes.getOneInHierarchy(CharSequence.class, isClass(Object.class));

		assertThat(cls, nullValue());
	}

	@Test
	void shouldUseTheClassOfTheObject() {
		Class<?> cls = Classes.getOneInHierarchy(new C(), isClass(B.class));

		assertThat(cls, equalTo(B.class));
	}

	@Test
	void shouldUseTheClassObjectDirectly() {
		Class<?> cls = Classes.getOneInHierarchy((Object) C.class, isClass(C.class));

		assertThat(cls, equalTo(C.class));
	}

	@Test
	void shouldReturnNullWhenObjectIsNull() {
		Class<?> cls = Classes.getOneInHierarchy((Object) null, isClass(A.class));

		assertThat(cls, nullValue());
	}

	@Test
	void shouldReturnNullWhenObjectPredicateIsNull() {
		Class<?> cls = Classes.getOneInHierarchy(new C(), null);

		assertThat(cls, nullValue());
	}

	@Test
	void shouldReturnTheClassItselfWhenCanonicalNameMatches() {
		Class<?> cls = Classes.getOneInHierarchy(C.class, C.class.getCanonicalName());

		assertThat(cls, equalTo(C.class));
	}

	@Test
	void shouldReturnTheSuperclassWhenCanonicalNameMatches() {
		Class<?> cls = Classes.getOneInHierarchy(C.class, A.class.getCanonicalName());

		assertThat(cls, equalTo(A.class));
	}

	@Test
	void shouldReturnObjectClassWhenCanonicalNameMatches() {
		Class<?> cls = Classes.getOneInHierarchy(C.class, Object.class.getCanonicalName());

		assertThat(cls, equalTo(Object.class));
	}

	@Test
	void shouldReturnNullWhenNoCanonicalNameMatches() {
		Class<?> cls = Classes.getOneInHierarchy(C.class, String.class.getCanonicalName());

		assertThat(cls, nullValue());
	}

	@Test
	void shouldNotMatchTheSimpleName() {
		Class<?> cls = Classes.getOneInHierarchy(C.class, C.class.getSimpleName());

		assertThat(cls, nullValue());
	}

	@Test
	void shouldReturnNullWhenClassIsNullForCanonicalName() {
		Class<?> cls = Classes.getOneInHierarchy((Class<?>) null, C.class.getCanonicalName());

		assertThat(cls, nullValue());
	}

	@Test
	void shouldReturnNullWhenCanonicalNameIsNull() {
		String canonicalName = null;

		Class<?> cls = Classes.getOneInHierarchy(C.class, canonicalName);

		assertThat(cls, nullValue());
	}
}
