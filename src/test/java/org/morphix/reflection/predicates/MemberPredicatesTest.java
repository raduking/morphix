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
package org.morphix.reflection.predicates;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.morphix.reflection.predicates.MemberPredicates.hasName;
import static org.morphix.reflection.predicates.MemberPredicates.nameIn;
import static org.morphix.reflection.predicates.MemberPredicates.withAllModifiers;
import static org.morphix.reflection.predicates.MemberPredicates.withAnyModifiers;
import static org.morphix.reflection.predicates.MemberPredicates.withModifiers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.MemberAccessor;
import org.morphix.reflection.Methods;

/**
 * Test class for {@link MemberPredicates}.
 *
 * @author Radu Sebastian LAZIN
 */
class MemberPredicatesTest {

	@Test
	void shouldThrowExceptionIfThisClassIsInstantiatedWithDefaultConstructor() throws Exception {
		Throwable targetException = null;
		Constructor<MemberPredicates> defaultConstructor = MemberPredicates.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<MemberPredicates>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
			targetException = e.getTargetException();
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}

	@Test
	void shouldReturnTheCorrectAnyModifiersPredicate() {
		Method staticMethod = Methods.Safe.getOneDeclaredInHierarchy("fooStatic", A.class);

		boolean result = withAnyModifiers(Modifier::isStatic, Modifier::isPrivate).test(staticMethod);

		assertTrue(result);
	}

	@Test
	void shouldReturnTheCorrectAllModifiersPredicate() {
		Method staticMethod = Methods.Safe.getOneDeclaredInHierarchy("fooStatic", A.class);

		boolean result = withAllModifiers(Modifier::isStatic, Modifier::isPublic).test(staticMethod);

		assertTrue(result);
	}

	@Test
	void shouldReturnTheCorrectAllModifiersPredicateAlias() {
		Method staticMethod = Methods.Safe.getOneDeclaredInHierarchy("fooStatic", A.class);

		boolean result = withModifiers(Modifier::isStatic, Modifier::isPublic).test(staticMethod);

		assertTrue(result);
	}

	@Test
	void shouldReturnTheCorrectMethodsForPublicPredicate() {
		List<Method> methods = Methods.getAllDeclaredInHierarchy(A.class, withAllModifiers(Modifier::isStatic, Modifier::isPublic));

		assertThat(methods, hasSize(1));
		assertThat(methods.get(0).getName(), equalTo("fooStatic"));
	}

	@Test
	void shouldReturnTheCorrectMethodsForNamePredicate() {
		List<Method> methods = Methods.getAllDeclaredInHierarchy(A.class, hasName("foo"));

		assertThat(methods, hasSize(1));
		assertThat(methods.get(0).getName(), equalTo("foo"));
	}

	@Test
	void shouldReturnMethodsWithNames() {
		List<Method> methods = Methods.getAllDeclaredInHierarchy(A.class, nameIn(List.of("foo")));

		assertThat(methods, hasSize(1));
		assertThat(methods.get(0).getName(), equalTo("foo"));
	}

	@Test
	void shouldReturnTheCorrectPredicateForAbstractMethod() {
		Method abstractMethod = Methods.Safe.getOneDeclaredInHierarchy("fooAbstract", B.class);

		boolean result = withModifiers(Modifier::isAbstract).test(abstractMethod);
		assertTrue(result);

		result = MemberPredicates.isAbstract().test(abstractMethod);
		assertTrue(result);
	}

	@Test
	void shouldReturnTheCorrectPredicateForNonAbstractMethod() {
		Method abstractMethod = Methods.Safe.getOneDeclaredInHierarchy("fooNotAbstract", B.class);

		boolean result = withModifiers(Modifier::isAbstract).test(abstractMethod);

		assertFalse(result);
	}

	public static class A {

		public static void fooStatic() {
			// empty
		}

		public void foo() {
			// empty
		}

	}

	public abstract static class B {

		public abstract void fooAbstract();

		public void fooNotAbstract() {
			// empty
		}

	}
}
